package contest;

import static contest.SanarHelper.*;
import static utils.CommonsFX.onCloseWindow;
import static utils.ex.SupplierEx.getIgnore;
import static utils.ex.SupplierEx.orElse;

import extract.SongUtils;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ConsoleUtils;
import utils.ExtractUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public class SanarCrawler extends Application {

    private static final Logger LOG = HasLogging.log();
    @FXML
    private ListView<Map.Entry<String, String>> vagasList;

    @FXML
    private TreeView<Map.Entry<String, String>> treeBuilder;
    @FXML
    private TableView<Concurso> concursosTable;

    @FXML
    private TableColumn<Concurso, Object> tableColumn4;

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();

    private Set<String> links = new HashSet<>();

    private Map<String, String> cookies = SanarHelper.getCookies();

    private SimpleStringProperty domain = new SimpleStringProperty(E_SANAR_DOMAIN);

    private PhantomJSUtils phantomJSUtils;

    public void initialize() {

        Map.Entry<String, String> simpleEntry = new SimpleEntry<>("Area do aluno", SanarHelper.PREFIX + "");

        tableColumn4.setCellFactory(SimpleTableViewBuilder.newCellFactory(SanarHelper::concursoCellFactory));
        SimpleTreeViewBuilder.of(treeBuilder).root(simpleEntry)
                .cellFactory((m, cell) -> RunnableEx.runIf(m, c -> adjustStyleClass(cell, c)))
                .onSelect(t -> getNewLinks(t, treeBuilder));
        SimpleListViewBuilder.of(vagasList).items(FXCollections.observableSet(new LinkedHashSet<>())).copiable()
                .cellFactory(SimpleListViewBuilder
                        .newCellFactory((m, cell) -> RunnableEx.runIf(m, c -> vagasStyleClass(cell, c))))
                .onSelect((old, val) -> RunnableEx.runIf(val, url -> downloadVideo(url.getKey(), url.getValue())));
        SimpleTableViewBuilder.of(concursosTable).items(concursos).equalColumns()
                .onSelect((old, value) -> selectConcurso(value));
        phantomJSUtils = new PhantomJSUtils();
    }

    @Override
    public void start(Stage primaryStage) {
        ExtractUtils.insertProxyConfig();
        CommonsFX.loadFXML("E-Sanar Crawler", "QuadrixCrawler.fxml", this, primaryStage);
        onCloseWindow(primaryStage, phantomJSUtils::quit);
    }

    private synchronized Document getDocument(String url) {
        URL url2 = getIgnore(() -> new URL(url));
        domain.set(url2.getProtocol() + "://" + url2.getHost());
        LOG.info("Loading {}", url);
        return phantomJSUtils.render(url, cookies, 15);
    }

    private List<Entry<String, String>> getLinks(Entry<String, String> entry, int level, Document doc) {
        List<SimpleEntry<String, String>> allLinks = doc.select("tr").stream()
                .map(FunctionEx.ignore(l -> new AbstractMap.SimpleEntry<>(l.text(),
                        ExtractUtils.addDomain(domain, l.select("a").get(1).attr("abs:href")))))
                .filter(Objects::nonNull).filter(t -> !"#".equals(t.getValue())).filter(t -> links.add(t.getValue()))
                .collect(Collectors.toList());
        List<Map.Entry<String, String>> linksFound =
                allLinks.stream().filter(t -> isValidLink(level, t)).distinct().collect(Collectors.toList());
        if (level == 1) {
            RunnableEx.run(() -> {
                getFilesFromPage(entry);
                nextInTree();
            });
        }
        if (level == 0) {
            List<SimpleEntry<String, String>> collec = doc.select("tr").stream()
                    .map(FunctionEx.ignore(l -> new AbstractMap.SimpleEntry<>(l.text(),
                            ExtractUtils.addDomain(E_SANAR_DOMAIN, l.select("a").get(0).attr("href")))))
                    .filter(Objects::nonNull).filter(e -> !e.getKey().startsWith("Material Complementar"))
                    .collect(Collectors.toList());
            for (SimpleEntry<String, String> url : collec) {
                Concurso e2 = new Concurso();
                e2.setUrl(url.getValue());
                String key = url.getKey();
                e2.setNome(key);
                concursos.add(e2);
            }

        }
        return linksFound;
    }

    private void getNewLinks(TreeItem<Map.Entry<String, String>> newValue, TreeView<Map.Entry<String, String>> tree) {
        if (newValue == null) {
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        int level = tree.getTreeItemLevel(newValue);
        Entry<String, String> entry = newValue.getValue();
        RunnableEx.runNewThread(() -> {
            if (level == 1) {
                getFilesFromPage(entry);
                return Collections.<Entry<String, String>>emptyList();
            }
            String url = entry.getValue();
            URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(QuadrixHelper.addQuadrixDomain(url)));
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            String url1 = url2.toExternalForm();
            LOG.info("Loading {}", url1);
            Document doc = SanarHelper.downloadMainPage(url1);
            return getLinks(entry, level, doc);
        }, l -> CommonsFX.runInPlatform(() -> {
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        }));

    }

    private List<Entry<String, String>> getVimeoLinks(Entry<String, String> entry, int level, Document doc) {
        List<SimpleEntry<String, String>> allLinks = doc.select("button[data-tipo=VIDEO]").stream()
                .map(FunctionEx.ignore(l -> new AbstractMap.SimpleEntry<>(l.attr("data-nome"),
                        "https://player.vimeo.com/video/" + l.attr("data-src"))))
                .filter(Objects::nonNull).filter(t -> !"#".equals(t.getValue())).filter(t -> links.add(t.getValue()))
                .collect(Collectors.toList());
        List<Map.Entry<String, String>> linksFound =
                allLinks.stream().filter(t -> isValidLink(level, t) || isVimeoValidLink(level, t)).distinct()
                        .collect(Collectors.toList());
        if (level == 1) {
            RunnableEx.run(() -> {
                getFilesFromPage(entry);
                nextInTree();
            });
        }
        if (level == 0) {
            for (SimpleEntry<String, String> url : allLinks) {
                Concurso e2 = new Concurso();
                e2.setUrl(url.getValue());
                String key = url.getKey();
                e2.setNome(key);
                concursos.add(e2);
            }

        }
        return linksFound;
    }

    private void nextInTree() {
        CommonsFX.runInPlatform(() -> {
            int selectedIndex = treeBuilder.getSelectionModel().getSelectedIndex();
            treeBuilder.getSelectionModel().select(selectedIndex + 1);
        });
    }

    private void selectConcurso(Concurso value) {
        if (value == null) {
            return;
        }
        String finalName = fixName(value.getNome()).replaceAll("\\.\\w$", "");
        if (existCopy(finalName) || !value.getVagas().isEmpty()) {
            selectNextLater();
            return;
        }
        String url = value.getUrl();
        String nome = value.getNome();
        File file = toVideoFileName(nome);
        if (url.contains("player.vimeo.com/video/")) {
            LOG.info("DOWNLOADING {} ...", file.getName());
            RunnableEx.runNewThread(() -> {
                return SongUtils.downloadYoutubeVideo(url, file);
            }, l -> {
                if (l.stream().anyMatch(s -> s.contains("100%"))) {
                    LOG.info("DOWNLOADED {} SUCCESSFULLY", file.getName());
                }

                selectNext();
            });
            return;
        }

        RunnableEx.runNewThread(() -> getDocument(value.getUrl()).head().select("link[rel=preload]").stream()
                .map(e -> e.attr("href")).findFirst().orElse(null), link -> {
                    if (link == null) {
                        CommonsFX.runInPlatform(() -> concursos.remove(value));
                        return;
                    }
                    startDownloadInNewThread(value, link);
                    selectNext();
                });
    }

    private void selectNext() {
        if (ConsoleUtils.countActiveProcesses() < 10) {
            CommonsFX.runInPlatformSync(() -> {
                int row = concursosTable.getSelectionModel().getSelectedIndex();
                concursosTable.getSelectionModel().select((row + 1) % concursos.size());
            });
        }
    }

    private void selectNextLater() {
        if (ConsoleUtils.countActiveProcesses() < 6) {
            CommonsFX.runInPlatform(() -> {
                int row = concursosTable.getSelectionModel().getSelectedIndex();
                concursosTable.getSelectionModel().select(row + 1);
            });
        }
    }

    private void startDownloadInNewThread(Concurso value, String link) {
        SimpleEntry<String, String> e = new SimpleEntry<>(value.getNome(), link);
        CommonsFX.runInPlatformSync(() -> {
            value.getVagas().add(link);
            value.getLinksFound().add(e);
            vagasList.getItems().addAll(value.getLinksFound().stream().filter(m -> !vagasList.getItems().contains(m))
                    .collect(Collectors.toList()));
        });
        RunnableEx.runNewThread(() -> downloadVideo(value.getNome(), link), d -> d.addListener((ob, old, val) -> {
            if (val.intValue() == 1) {
                LOG.info("DOWNLOADED {} ", value.getNome());
                selectNext();
            }
        }));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
