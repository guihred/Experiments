package contest;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static utils.RunnableEx.run;
import static utils.RunnableEx.runNewThread;
import static utils.SupplierEx.getIgnore;
import static utils.SupplierEx.orElse;

import extract.PdfUtils;
import java.io.File;
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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.*;

public class QuadrixCrawler extends Application {
    private static final Logger LOG = HasLogging.log();
    @FXML
    private ListView<String> vagasList;
    @FXML
    private TreeView<Map.Entry<String, String>> treeBuilder;
    @FXML
    private TableView<Concurso> concursosTable;
    @FXML
    private TableColumn<Concurso, Object> tableColumn4;

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();
    private Set<String> links = new HashSet<>();

    public void initialize() {
        Map.Entry<String, String> simpleEntry = new SimpleEntry<>("", IadesHelper.addQuadrixDomain("encerrados.aspx"));
        treeBuilder.setRoot(new TreeItem<>(simpleEntry));
        Property<Concurso> concurso = new SimpleObjectProperty<>();

        SimpleTreeViewBuilder.onSelect(treeBuilder, t -> getNewLinks(t, treeBuilder));
        vagasList.setItems(FXCollections.observableArrayList());

        tableColumn4.setCellFactory(SimpleTableViewBuilder.newCellFactory(QuadrixCrawler::addClasses));
        SimpleListViewBuilder.onSelect(vagasList,
            (old, value) -> runNewThread(() -> IadesHelper.saveConcurso(concurso, vagasList, value)));
        concursosTable.setItems(concursos);
        SimpleTableViewBuilder.onSelect(concursosTable, (old, value) -> {
            if (value != null) {
                concurso.setValue(value);
                value.getVagas().sort(String.CASE_INSENSITIVE_ORDER);
                vagasList.setItems(value.getVagas());
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CrawlerTask.insertProxyConfig();
        CommonsFX.loadFXML("Quadrix Crawler", "QuadrixCrawler.fxml", this, primaryStage);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
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
            run(() -> IadesHelper.getFilesFromPage(url));
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
            findVagas(linksFound, e2, concursos);

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
            String url1 = IadesHelper.addQuadrixDomain(url);
            SupplierEx.get(() -> CrawlerTask.getFile(key, url1));
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        HasLogging.log();
        int level = tree.getTreeItemLevel(newValue);
        SimpleStringProperty domain = new SimpleStringProperty(IadesHelper.QUADRIX_DOMAIN);
        CompletableFuture.supplyAsync(SupplierEx.makeSupplier(() -> {
            URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(IadesHelper.addQuadrixDomain(url)));
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            LOG.info("GETTING {} level {}", url, level);
            return CrawlerTask.getDocument(url2.toExternalForm(), IadesHelper.COOKIES);
        })).thenApply(doc -> getLinks(doc, entry, domain, level)).thenAccept(l -> {
            LOG.info("Links {}", l);
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });
        ForkJoinPool.commonPool().awaitQuiescence(500, TimeUnit.SECONDS);
    }

    public static void addClasses(Concurso con, TableCell<Concurso, Object> cell) {
        cell.setText(con.getNome());
        cell.getStyleClass().removeAll("amarelo", "vermelho");
        if (con.getVagas().isEmpty()) {
            cell.getStyleClass().add("vermelho");
            con.getVagas().addListener((Observable c) -> {
                ObservableList<?> observableList = (ObservableList<?>) c;
                if (con.getNome().equals(cell.getText()) && !observableList.isEmpty()) {
                    cell.getStyleClass().remove("vermelho");
                    if (IadesHelper.hasTI(observableList)) {
                        cell.getStyleClass().add("amarelo");
                    }
                }
            });
        } else if (IadesHelper.hasTI(con.getVagas())) {
            cell.getStyleClass().add("amarelo");
        }
    }

    public static void findVagas(List<Map.Entry<String, String>> linksFound, Concurso e2,
        ObservableList<Concurso> concursos) {
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

    public static void main(String[] args) {
        launch(args);
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

    private static void getVagas(Concurso e2, Entry<String, String> resultado) {
        IadesHelper.getFilesFromPage(resultado).forEach(ConsumerEx.ignore(f -> extrairVagas(e2, f)));
    }

}
