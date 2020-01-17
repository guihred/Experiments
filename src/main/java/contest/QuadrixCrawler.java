
package contest;

import static contest.IadesHelper.containsNumber;
import static contest.IadesHelper.getAnswers;
import static contest.IadesHelper.getContestQuestions;
import static contest.IadesHelper.getPDF;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static simplebuilder.SimpleDialogBuilder.bindWindow;
import static utils.CrawlerTask.executeRequest;
import static utils.ResourceFXUtils.toExternalForm;
import static utils.RunnableEx.run;
import static utils.RunnableEx.runNewThread;
import static utils.StringSigaUtils.decodificar;
import static utils.SupplierEx.getIgnore;
import static utils.SupplierEx.orElse;

import contest.db.ContestQuestion;
import contest.db.Organization;
import extract.PdfUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.*;

public class QuadrixCrawler extends Application {

    public static final Logger LOG = HasLogging.log();
    private static final String DOMAIN = "http://www.quadrix.org.br";

    private static Map<String, String> cookies = new HashMap<>();

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();

    private Set<String> links = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Quadrix Crawler");
        CrawlerTask.insertProxyConfig();
        Parent node = createSplitTreeListDemoNode();
        Scene value = new Scene(node);
        value.getStylesheets().add(toExternalForm("filesComparator.css"));
        primaryStage.setScene(value);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());

        primaryStage.show();
    }

    private Parent createSplitTreeListDemoNode() {
        SimpleTreeViewBuilder<Map.Entry<String, String>> root = new SimpleTreeViewBuilder<Map.Entry<String, String>>()
            .root(new AbstractMap.SimpleEntry<>("", addDomain("encerrados.aspx")));
        TreeView<Map.Entry<String, String>> treeBuilder = root.build();
        Property<Concurso> concurso = new SimpleObjectProperty<>();
        root.onSelect(t -> getNewLinks(t, treeBuilder));
        SimpleListViewBuilder<String> listBuilder = new SimpleListViewBuilder<>();
        listBuilder.items(FXCollections.observableArrayList())
            .onSelect((old, value) -> runNewThread(() -> saveConcurso(concurso, listBuilder, value)));
        SimpleTableViewBuilder<Concurso> tableBuilder = new SimpleTableViewBuilder<Concurso>().items(concursos)
            .addColumn("Nome", (con, cell) -> {
                cell.setText(con.getNome());
                cell.getStyleClass().removeAll("amarelo", "vermelho");
                if (con.getVagas().isEmpty()) {
                    cell.getStyleClass().add("vermelho");
                    con.getVagas().addListener((Observable c) -> {
                        ObservableList<?> observableList = (ObservableList<?>) c;
                        if (con.getNome().equals(cell.getText()) && !observableList.isEmpty()) {
                            cell.getStyleClass().remove("vermelho");
                            if (hasTI(observableList)) {
                                cell.getStyleClass().add("amarelo");
                            }
                        }
                    });
                } else if (hasTI(con.getVagas())) {
                    cell.getStyleClass().add("amarelo");
                }
            }).onSelect((old, value) -> {
                if (value != null) {
                    concurso.setValue(value);
                    value.getVagas().sort(String.CASE_INSENSITIVE_ORDER);
                    listBuilder.items(value.getVagas());
                }
            }).prefWidthColumns(1).minWidth(200);

        return new VBox(new SplitPane(treeBuilder, tableBuilder.build(), listBuilder.build()));
    }

    private void findVagas(List<Map.Entry<String, String>> linksFound, Concurso e2) {
        runNewThread(() -> {
            if (e2.getVagas().isEmpty()) {
                List<Entry<String, String>> collect = linksFound.stream()
                    .filter(t -> containsIgnoreCase(t.getKey(), "Resultado")).collect(Collectors.toList());
                for (Entry<String, String> entry : collect) {
                    getVagas(e2, entry);
                    if (!e2.getVagas().isEmpty()) {
                        break;
                    }
                }
                if (e2.getVagas().isEmpty()) {
                    Platform.runLater(() -> concursos.remove(e2));
                }
            }
        });
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url,
        SimpleStringProperty domain, int level) {
        Elements select = doc.select("a");
        List<SimpleEntry<String, String>> allLinks = select.stream()
            .map(l -> new AbstractMap.SimpleEntry<>(l.text(), IadesHelper.addDomain(domain, l.attr("href"))))
            .filter(t -> !"#".equals(t.getValue()) && isNotBlank(t.getKey())).filter(t -> links.add(t.getValue()))
            .collect(Collectors.toList());
        List<Map.Entry<String, String>> linksFound = allLinks.stream()
            .filter(t -> level < 2 || containsIgnoreCase(t.getKey(), "aplicada")
                || containsIgnoreCase(t.getKey(), "Gabarito Definitivo") || t.getKey().contains("Caderno"))
            .distinct().collect(Collectors.toList());
        if (level == 2) {
            run(() -> getFilesFromPage(url));
        }
        if (level == 1 && !linksFound.isEmpty()) {
            Concurso e2 = new Concurso();
            e2.setUrl(url.getValue());
            String key = url.getKey();
            String[] split = key.split("-");
            e2.setNome(split[0]);
            e2.setLinksFound(linksFound);
            linksFound.stream()
                .filter(e -> containsIgnoreCase(e.getKey(), "Caderno de prova -") || e.getKey().matches("\\d+ *- *.+"))
                .map(s -> s.getKey().split("- *")[1]).forEach(e -> {
                    if (!e2.getVagas().contains(e)) {
                        e2.getVagas().add(e);
                    }
                });
            findVagas(linksFound, e2);

            concursos.add(e2);
        }
        return linksFound;
    }

    private void getNewLinks(TreeItem<Map.Entry<String, String>> newValue, TreeView<Map.Entry<String, String>> tree) {
        if (newValue == null) {
            return;
        }

        Entry<String, String> entry = newValue.getValue();
        String key = entry.getKey();
        String url = entry.getValue();

        if (IadesHelper.hasFileExtension(key)) {
            String url1 = addDomain(url);
            SupplierEx.get(() -> CrawlerTask.getFile(key, url1));
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        int level = tree.getTreeItemLevel(newValue);
        SimpleStringProperty domain = new SimpleStringProperty(DOMAIN);
        CompletableFuture.supplyAsync(SupplierEx.makeSupplier(() -> {
            URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(addDomain(url)));
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            LOG.info("GETTING {} level {}", url, level);
            return CrawlerTask.getDocument(url2.toExternalForm(), cookies);
        })).thenApply(doc -> getLinks(doc, entry, domain, level)).thenAccept(l -> {
            LOG.info("Links {}", l);
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });
        ForkJoinPool.commonPool().awaitQuiescence(500, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void saveQuestions(Property<Concurso> concurso, String vaga,
        ObservableList<Entry<String, String>> linksFound, String number, ContestReader entities) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();
        File gabaritoFile = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).map(QuadrixCrawler::getFilesFromPage)
            .filter(l -> !l.isEmpty()).map(l -> l.get(0)).findFirst().orElse(null);
        if (gabaritoFile == null) {
            LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(Collectors.toList());
        String[] split = Objects.toString(vaga, "").split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabaritoFile, linesRead);
            return;
        }
        String answers = getAnswers(entities, linesRead, findFirst.get());
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
    }

    private static String addDomain(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        return DOMAIN + "/" + url;
    }

    private static void addVaga(Concurso e2, String vaga) {
        if (isNotBlank(vaga) && !vaga.matches("\\d+:") && !e2.getVagas().contains(vaga)) {
            Platform.runLater(() -> {
                if (!e2.getVagas().contains(vaga)) {
                    e2.getVagas().add(vaga);
                }
            });
        }
    }

    private static void extrairVagas(Concurso e2, File f) {
        for (String string : PdfUtils.getAllLines(f)) {
            if (string.matches("(?i).+\\(código \\d+\\).*")) {
                String split = string.split(" *\\(")[0];
                addVaga(e2, split);
                continue;
            }
            String[] split = string.split(" ");
            String regex = "\\d{3}\\.\\d+\\/\\d";
            List<String> asList = Arrays.asList(split);
            Optional<String> findFirst = asList.stream().filter(e -> e.matches(regex)).findFirst();
            if (findFirst.isPresent()) {
                int indexOf = asList.indexOf(findFirst.get());
                String collect = Stream.of(split).skip(1).limit(indexOf - 1L).collect(Collectors.joining(" "));
                String vaga = collect.split(" *-")[0];
                if (!vaga.matches(".*" + regex + ".*")) {
                    addVaga(e2, vaga);
                }
            }
        }
        RunnableEx.run(() -> Files.deleteIfExists(f.toPath()));
    }

    private static File getFileFromPage(String text, String url3) throws IOException {
        // PDFs are redirected to an html visualization page
        if (!text.endsWith(".pdf")) {
            return CrawlerTask.getFile(text, url3);
        }
        Response executeRequest = executeRequest(url3, cookies);
        String fileParameter = decodificar(executeRequest.url().getQuery().split("=")[1]);
        return SupplierEx
            .makeSupplier(() -> CrawlerTask.getFile(text, fileParameter), e -> LOG.info("{} Failed", fileParameter))
            .get();
    }

    private static List<File> getFilesFromPage(Entry<String, String> link) {
        String url = link.getValue();
        URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(addDomain(url)));
        Document document = SupplierEx.getIgnore(() -> CrawlerTask.getDocument(url2.toExternalForm(), cookies));
        if (document == null) {
            return Collections.emptyList();
        }
        Elements select = document.select("a");
        return select.stream().filter(e -> IadesHelper.hasFileExtension(e.text()))
            .map(FunctionEx.ignore(e -> getFileFromPage(e.text(), addDomain(e.attr("href"))))).filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static void getVagas(Concurso e2, Entry<String, String> resultado) {
        getFilesFromPage(resultado).forEach(ConsumerEx.ignore(f -> extrairVagas(e2, f)));
    }

    private static boolean hasTI(ObservableList<?> observableList) {
        List<String> keys = Arrays.asList("Informação", "Sistema", "Tecnologia", "Informatica");
        return observableList.stream().anyMatch(e -> keys.stream().anyMatch(m -> containsIgnoreCase(e.toString(), m)));
    }

    private static void saveConcurso(Property<Concurso> concurso, SimpleListViewBuilder<String> listBuilder,
        String value) {
        if (value == null) {
            return;
        }
        Concurso value1 = concurso.getValue();
        ObservableList<Entry<String, String>> linksFound = value1.getLinksFound();
        String number = Objects.toString(value) + "";
        List<Entry<String, String>> collect = linksFound.stream()
            .filter(e -> e.getKey().contains("Provas") || e.getKey().contains(number))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).collect(Collectors.toList());
        File file = collect.stream().map(QuadrixCrawler::getFilesFromPage).filter(e -> !e.isEmpty()).map(e -> e.get(0))
            .findFirst().orElse(null);
        if (file == null) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", collect, value1, value);
            return;
        }
        File file2 = getPDF(value, file);

        getContestQuestions(file2, Organization.QUADRIX, entities -> {
            saveQuestions(concurso, value, linksFound, number, entities);
            Platform
                .runLater(() -> new ContestApplication(entities).start(bindWindow(new Stage(), listBuilder.build())));
        });
    }

}
