package contest;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static utils.CommonsFX.onCloseWindow;
import static utils.ex.SupplierEx.getIgnore;
import static utils.ex.SupplierEx.orElse;

import extract.JsoupUtils;
import extract.PhantomJSUtils;
import extract.SongUtils;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
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
import utils.ResourceFXUtils;
import utils.ex.FunctionEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class SanarCrawler extends Application {

    private static final String E_SANAR_DOMAIN = "https://www.e-sanar.com.br";
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

    private Map<String, String> cookies = new HashMap<>();

    private SimpleStringProperty domain = new SimpleStringProperty(E_SANAR_DOMAIN);

    private PhantomJSUtils phantomJSUtils;

    public void initialize() {
        cookies.put("ESANARSESSID", "m5otcssorltp87a8s62fmoqtc5");
        cookies.put("intercom-id-sju7o7kl", "1d4fda3e-8f5f-4a98-b129-1f73eb81f9e5");
        cookies.put("intercom-session-sju7o7kl",
                "djUydEdkVGYzd1ZjT3puZmFTdzFxTnEycXgrTnhVZGlOZmN5cUU3dDdsTTNINGswQ0ZaUXdPSWF3SDNwbGpGbi0tYk0xNXlNQmlxR2VBc2Z6cC85MDhBQT09--ab8477dcfb63ba1b62642cac6f64a572f3389b6f");
        cookies.put("muxData", "mux_viewer_id=e3480ca3-e1f1-44ee-b6b5-d8d7655e92c7&msn=0.12306064932172656");

        Map.Entry<String, String> simpleEntry = new SimpleEntry<>("Area do aluno",
                "https://www.e-sanar.com.br/aluno/curso/292,preparatorio-para-farmacia.html");
        treeBuilder.setRoot(new TreeItem<>(simpleEntry));

        tableColumn4.setCellFactory(SimpleTableViewBuilder.newCellFactory(SanarCrawler::concursoCellFactory));
        SimpleTreeViewBuilder.onSelect(treeBuilder, t -> getNewLinks(t, treeBuilder));
        SimpleListViewBuilder.of(vagasList).items(FXCollections.observableSet(new LinkedHashSet<>())).copiable()
                .cellFactory(SimpleListViewBuilder.newCellFactory((m, cell) -> {
                    RunnableEx.runIf(m, c -> {
                        String con = c.getKey();
                        cell.setText(c + "");
                        cell.getStyleClass().removeAll(IadesHelper.AMARELO);
                        if (toVideoFileName(con).exists()) {
                            cell.getStyleClass().add(IadesHelper.AMARELO);
                        }
                    });
                })).onSelect((old, val) -> RunnableEx.runIf(val, (url) -> downloadVideo(url.getKey(), url.getValue())));
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

    private DoubleProperty downloadVideo(String nome, String url) {
        File file = toVideoFileName(nome);
        if (file.exists()) {
            LOG.info("FILE {} EXISTS", file);
            return null;
        }
        LOG.info("DOWNLOADING {} ...", file.getName());
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0");

        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Origin", "https://www.e-sanar.com.br");
        headers.put("DNT", "1");
        headers.put("Connection", "keep-alive");
        headers.put("Referer",
                "https://www.e-sanar.com.br/aluno/area-do-aluno/assunto/292,preparatorio-para-farmacia/59602,apresentacao-planner-de-estudo-para-concursos-em-farmacia.html");
        List<String> list = SupplierEx.get(() -> PhantomJSUtils.makeGet(url, headers));
        String downloadURL = list.stream().filter(e -> e.startsWith("https")).findFirst().orElse("");
        DoubleProperty downloadVideo = SongUtils.downloadVideo(downloadURL, file);
        downloadVideo.addListener((ob, old, val) -> {
            if (val.intValue() == 1) {
                LOG.info("DOWNLOADED {} ", file.getName());
                selectNext();
            }
        });
        return downloadVideo;
    }

    private synchronized Document getDocument(String url) {
        URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(QuadrixHelper.addQuadrixDomain(url)));
        domain.set(url2.getProtocol() + "://" + url2.getHost());
        LOG.info("Loading {}", url);
        return phantomJSUtils.render(url, cookies);
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
            RunnableEx.run(() -> getFilesFromPage(entry));
        }
        if (level == 0) {
            List<SimpleEntry<String, String>> collec = doc.select("tr").stream()
                    .map(FunctionEx.ignore(l -> new AbstractMap.SimpleEntry<>(l.text(),
                            ExtractUtils.addDomain(domain, l.select("a").get(0).attr("abs:href")))))
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

        Entry<String, String> entry = newValue.getValue();
        String key = entry.getKey();
        String url = entry.getValue();

        if (IadesHelper.hasFileExtension(key)) {
            String url1 = QuadrixHelper.addQuadrixDomain(url);
            SupplierEx.get(() -> ExtractUtils.getFile(key, url1));
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        int level = tree.getTreeItemLevel(newValue);
        RunnableEx.runNewThread(() -> {
            URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(QuadrixHelper.addQuadrixDomain(url)));
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            Document doc = JsoupUtils.getDocument(url2.toExternalForm(), cookies);
            return getLinks(entry, level, doc);
        }, l -> CommonsFX.runInPlatform(() -> {
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        }));

    }

    private void selectConcurso(Concurso value) {
        if (value != null) {
            String finalName = fixName(value.getNome()).replaceAll("\\.\\w$", "");
            File file = ResourceFXUtils.getOutFile("videos/" + finalName + ".mp4");
            if (!file.exists() && value.getVagas().isEmpty()) {
                value.getVagas().sort(String.CASE_INSENSITIVE_ORDER);
                RunnableEx.runNewThread(() -> {
                    Document document = getDocument(value.getUrl());
                    return document.head().select("link[rel=preload]").stream().map(e -> e.attr("href")).findFirst()
                            .orElse(null);
                }, link -> {
                    if (link != null) {
                        SimpleEntry<String, String> e = new SimpleEntry<>(value.getNome(), link);
                        CommonsFX.runInPlatformSync(() -> {
                            value.getVagas().add(link);
                            value.getLinksFound().add(e);
                            vagasList.getItems().addAll(value.getLinksFound().stream()
                                    .filter(m -> !vagasList.getItems().contains(m)).collect(Collectors.toList()));
                        });
                        RunnableEx.runNewThread(() -> downloadVideo(value.getNome(), link));
                        selectNext();
                        return;
                    }
                    CommonsFX.runInPlatform(() -> concursos.remove(value));
                });
            } else {
                if (ConsoleUtils.countActiveProcesses() < 6) {
                    CommonsFX.runInPlatform(() -> {
                        int row = concursosTable.getSelectionModel().getSelectedIndex();
                        concursosTable.getSelectionModel().select((row + 1) % concursos.size());
                    });
                }
            }
            vagasList.getItems().addAll(value.getLinksFound());
        }
    }

    private void selectNext() {
        if (ConsoleUtils.countActiveProcesses() < 10) {
            CommonsFX.runInPlatformSync(() -> {
                int row = concursosTable.getSelectionModel().getSelectedIndex();
                concursosTable.getSelectionModel().select((row + 1) % concursos.size());
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void concursoCellFactory(Concurso t, TableCell<Concurso, Object> u) {
        u.setText(t.getNome());
        u.getStyleClass().removeAll(IadesHelper.AMARELO, IadesHelper.VERMELHO);
        if (toVideoFileName(t.getNome()).exists()) {
            u.getStyleClass().add(IadesHelper.AMARELO);
            return;
        }
        if (t.getVagas().isEmpty()) {
            u.getStyleClass().add(IadesHelper.VERMELHO);
            t.getVagas().addListener((Observable c) -> {
                List<?> vagasList1 = (List<?>) c;
                if (t.getNome().equals(u.getText()) && !vagasList1.isEmpty()) {
                    u.getStyleClass().remove(IadesHelper.VERMELHO);
                    if (toVideoFileName(t.getNome()).exists()) {
                        u.getStyleClass().add(IadesHelper.AMARELO);
                    }
                }
            });
        }
    }

    private static String fixName(String name) {
        return name.replaceAll("[\\?\\\\/]+", "");
    }

    private static File getFilesFromPage(Entry<String, String> entry) {
        return SupplierEx.get(() -> {
            String name = entry.getKey();
            String text = fixName(name);
            String url3 = entry.getValue();
            String extensions = url3.replaceAll(".+(\\.\\w+).+", "$1");
            String key = "eSanar/" + text + extensions;
            File outFile = ResourceFXUtils.getOutFile(key);
            if (outFile.exists()) {
                return outFile;
            }
            return ExtractUtils.getFile(key, url3);
        });
    }

    private static boolean isValidLink(int level, SimpleEntry<String, String> t) {
        return level == 1
                || containsIgnoreCase(t.getValue(), "https://www.e-sanar.com.br" + "/arquivos/esanar_assuntos/");
    }

    private static File toVideoFileName(String nome) {
        String finalName = fixName(nome).replaceAll("\\.\\w$", "");
        return ResourceFXUtils.getOutFile("videos/" + finalName + ".mp4");
    }

}
