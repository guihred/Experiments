
package contest.db;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import extract.UnRar;
import extract.UnZip;
import japstudy.db.HibernateUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import pdfreader.PdfUtils;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SupplierEx;

public class IadesCrawler extends Application {

    private static final String DOMAIN = "http://www.iades.com.br";
    private static final Logger LOG = HasLogging.log();

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("IADES Crawler");
        primaryStage.setScene(new Scene(createSplitTreeListDemoNode()));
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
    }

    private Parent createSplitTreeListDemoNode() {
        SimpleTreeViewBuilder<Map.Entry<String, String>> root = new SimpleTreeViewBuilder<Map.Entry<String, String>>()
            .root(new AbstractMap.SimpleEntry<>("", DOMAIN + "/inscricao"));
        TreeView<Map.Entry<String, String>> build = root.build();
        Set<String> arrayList = new HashSet<>();
        Property<Concurso> concurso = new SimpleObjectProperty<>();
        root.onSelect(t -> getNewLinks(t, arrayList, build));
        ListView<String> vagasView = new ListView<>();
        vagasView.setItems(FXCollections.observableArrayList());
        vagasView.getSelectionModel().selectedItemProperty()
            .addListener((ob, old, value) -> saveContestValues(concurso, value));

        TableView<Concurso> tableView = new SimpleTableViewBuilder<Concurso>().items(concursos).addColumns("nome")
            .onSelect((old, value) -> {
                concurso.setValue(value);
                vagasView.setItems(value.getVagas());
            }).prefWidthColumns(1).minWidth(200).build();

        return new VBox(new SplitPane(build, tableView, vagasView));
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url,
        SimpleStringProperty domain, Set<String> links, int level) {
        List<Map.Entry<String, String>> linksFound = doc.select("a").stream()
            .map(l -> new AbstractMap.SimpleEntry<>(l.text(), addDomain(domain, l.attr("href"))))
            .filter(t -> level < 2 || t.getKey().contains("Provas") || t.getKey().contains("Gabarito"))
            .filter(t -> links.add(t.getValue())).distinct().collect(Collectors.toList());
        if (level == 2 && !linksFound.isEmpty()) {
            Concurso e2 = new Concurso();
            e2.setUrl(url.getValue());
            String key = url.getKey();
            String[] split = key.split("Processo|Inscriç|Concurso|Vestibular");
            e2.setNome(split[0]);
            e2.setLinksFound(linksFound);
            doc.select("fieldset").stream().filter(e -> e.select("legend").text().contains("Vagas"))
                .flatMap(e -> e.select("b").stream()).forEach(e -> e2.getVagas().add(e.text()));
            concursos.add(e2);
        }

        return linksFound;
    }

    private void getNewLinks(TreeItem<Map.Entry<String, String>> newValue, Set<String> links,
        TreeView<Map.Entry<String, String>> build) {
        Entry<String, String> entry = newValue.getValue();
        String url = entry.getValue();

        if (url.endsWith(".pdf") || url.endsWith(".zip") || url.endsWith(".rar")) {
            extractURL(url);
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        int level = build.getTreeItemLevel(newValue);
        SimpleStringProperty domain = new SimpleStringProperty(DOMAIN);
        CompletableFuture.supplyAsync(SupplierEx.makeSupplier(() -> {
            URL url2 = new URL(url);
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            LOG.info("GETTING {} level {}", url, level);
            return CrawlerTask.getDocument(url, CrawlerTask.getEncodedAuthorization());
        })).thenApply(doc -> getLinks(doc, entry, domain, links, level)).thenAccept(l -> {
            LOG.info("Links {}", l);
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }

    private static String addDomain(SimpleStringProperty domain, String l) {
        return l.startsWith("/") ? domain.get() + l : l;
    }

    private static boolean extracted(String number, Path path) {
        return path.toFile().getName().contains(number) && path.toFile().getName().endsWith(".pdf");
    }

    private static File extractURL(String url) {
        return SupplierEx.get(() -> {
            String file = new URL(url).getFile();
            String[] split = file.split("/");
            String out = split[split.length - 1];
            File outFile = ResourceFXUtils.getOutFile(out);
            InputStream input = new URL(url).openConnection().getInputStream();
            try (FileOutputStream output = new FileOutputStream(outFile)) {
                IOUtils.copy(input, output);
            }
            if (url.endsWith(".zip")) {
                UnZip.extractZippedFiles(outFile);
            }
            if (url.endsWith(".rar")) {
                UnRar.extractRarFiles(outFile);
            }
            LOG.info("FILE {} SAVED", out);
            return outFile;
        });
    }

    private static String getAnswers(ContestReader entities, List<String> linesRead, Optional<String> findFirst) {
        int indexOf = linesRead.indexOf(findFirst.get());
        List<String> subList = linesRead.subList(indexOf , linesRead.size()-1);
        List<String> answersList = subList.stream().filter(StringUtils::isNotBlank).filter(s -> s.matches("[\\sA-E#]+"))
            .collect(Collectors.toList());
        String answers = "";
        for (String string : answersList) {
            String split = Stream.of(string.split("")).filter(s -> s.matches("[A-E#]")).collect(Collectors.joining());
            answers += split;
            if (answers.length() >= entities.getListQuestions().size()) {
                return answers;
            }
        }
        return answers;
    }

    private static Path getFirstPDF(File file, String number) throws IOException {
        try (Stream<Path> find = Files.find(file.toPath(), 3,
            (path, info) -> extracted(number, path));) {
            return find.findFirst().orElse(null);
        }
    }

    private static void saveContestValues(Property<Concurso> concurso, String vaga) {
        if (vaga == null) {
            return;
        }

        Concurso value = concurso.getValue();
        ObservableList<Entry<String, String>> linksFound = value.getLinksFound();
        String number = Objects.toString(vaga).replaceAll("\\D", "");
        Entry<String, String> orElse = linksFound.stream().filter(e -> e.getKey().contains("Provas"))
            .sorted(Comparator.comparing(e -> !e.getKey().contains(number))).findFirst().orElse(null);
        if (orElse == null) {
            LOG.info("NO LINK FOR Provas found {} - {}", vaga, value);
            return;
        }
        File file = extractURL(orElse.getValue());
        if (file == null) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", orElse, value, vaga);
            return;
        }
        File file3 = new File(file.getParentFile(), file.getName().replaceAll("\\.\\w+", ""));
        
        Path path = SupplierEx.get(() -> getFirstPDF(file3, number));
        if (path== null ) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", orElse, value, vaga);
            return;
        }
        File file2 = path.toFile();
        ContestReader.getContestQuestions(file2,
            entities -> saveQuestions(concurso, vaga, linksFound, number, entities));
    }

    private static void saveQuestions(Property<Concurso> concurso, String vaga,
        ObservableList<Entry<String, String>> linksFound, String number, ContestReader entities) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();
        Entry<String, String> gabarito = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> !e.getKey().contains(number))).findFirst().orElse(null);
        if (gabarito == null) {
            LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        File gabaritoFile = extractURL(gabarito.getValue());
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(Collectors.toList());
        String[] split = Objects.toString(vaga).split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) && containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            LOG.info("COULDN'T FIND {} {}", vaga, linesRead);
            return;
        }
        String answers = getAnswers(entities, linesRead, findFirst);
        if (answers.length() != entities.getListQuestions().size()) {
            LOG.info("QUESTIONS DON'T MATCH {} {}", answers.length(), entities.getListQuestions().size());
            return;
        }
        ObservableList<ContestQuestion> listQuestions = entities.getListQuestions();
        for (int i = 0; i < listQuestions.size(); i++) {
            ContestQuestion contestQuestion = listQuestions.get(i);
            contestQuestion.setAnswer(answers.charAt(i));
        }
        entities.saveAll();
        Platform.runLater(() -> new ContestApplication(entities).start(new Stage()));
    }

}
