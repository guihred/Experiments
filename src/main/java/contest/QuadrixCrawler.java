
package contest;

import static contest.IadesHelper.addDomain;
import static contest.IadesHelper.saveContestValues;
import static utils.SupplierEx.getIgnore;
import static utils.SupplierEx.orElse;

import extract.UnRar;
import extract.UnZip;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javafx.application.Application;
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
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.*;

public class QuadrixCrawler extends Application {

    private static final Logger LOG = HasLogging.log();
    private static final String DOMAIN = "http://www.quadrix.org.br";

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();

    Map<String, String> cookies = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Quadrix Crawler");
        CrawlerTask.insertProxyConfig();
        Parent node = createSplitTreeListDemoNode();
        primaryStage.setScene(new Scene(node));
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
    }

    private Parent createSplitTreeListDemoNode() {
        SimpleTreeViewBuilder<Map.Entry<String, String>> root = new SimpleTreeViewBuilder<Map.Entry<String, String>>()
                .root(new AbstractMap.SimpleEntry<>("", DOMAIN + "/encerrados.aspx"));
        TreeView<Map.Entry<String, String>> treeBuilder = root.build();
        Set<String> links = new HashSet<>();
        Property<Concurso> concurso = new SimpleObjectProperty<>();
        root.onSelect(t -> getNewLinks(t, links, treeBuilder));
        SimpleListViewBuilder<String> listBuilder = new SimpleListViewBuilder<>();
        listBuilder.items(FXCollections.observableArrayList()).onSelect((old,
                value) -> new Thread(() -> saveContestValues(concurso, value, listBuilder.build()), "Save Contest")
                        .start());
        SimpleTableViewBuilder<Concurso> tableBuilder =
                new SimpleTableViewBuilder<Concurso>().items(concursos).addColumns("nome").onSelect((old, value) -> {
                    concurso.setValue(value);
                    listBuilder.items(value.getVagas());
                }).prefWidthColumns(1).minWidth(200);

        return new VBox(new SplitPane(treeBuilder, tableBuilder.build(), listBuilder.build()));
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url,
            SimpleStringProperty domain, Set<String> links, int level) {
        List<SimpleEntry<String, String>> allLinks = doc.select("a").stream()
                .map(l -> new AbstractMap.SimpleEntry<>(l.text(), addDomain(domain, l.attr("href"))))
                .filter(t -> !"#".equals(t.getValue())).collect(Collectors.toList());
        List<Map.Entry<String, String>> linksFound =
                allLinks.stream()
                        .filter(t -> level < 2 || StringUtils.containsIgnoreCase(t.getKey(), "aplicada")
                                || StringUtils.containsIgnoreCase(t.getKey(), "Gabarito")
                                || t.getKey().contains("Caderno"))
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
        if (newValue == null) {
            return;
        }

        Entry<String, String> entry = newValue.getValue();
        String key = entry.getKey();
        String url = entry.getValue();

        if (key.endsWith(".pdf") || key.endsWith(".zip") || key.endsWith(".rar")) {
            String url1 = DOMAIN + "/" + url;
            SupplierEx.get(() -> {
                // TODO Solve problem
                File outFile = ResourceFXUtils.getOutFile(key);
                URL url2 = new URL(url1);
                HttpURLConnection con = (HttpURLConnection) url2.openConnection();
                if (!CrawlerTask.isNotProxied()) {
                    con.addRequestProperty("Proxy-Authorization",
                            "Basic " + CrawlerTask.getEncodedAuthorization());
                }
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                con.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                con.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0");
                con.setRequestProperty("Accept-Encoding"
                        , "gzip, deflate");
                con.setConnectTimeout(100000);
                con.setReadTimeout(100000);
                InputStream input = con.getInputStream();
                CrawlerTask.copy(input, outFile);
                if (url1.endsWith(".zip")) {
                    UnZip.extractZippedFiles(outFile);
                }
                if (url1.endsWith(".rar")) {
                    UnRar.extractRarFiles(outFile);
                }
                LOG.info("FILE {} SAVED", key);
                return outFile;
            });
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        int level = build.getTreeItemLevel(newValue);
        SimpleStringProperty domain = new SimpleStringProperty(DOMAIN);
        CompletableFuture.supplyAsync(SupplierEx.makeSupplier(() -> {
            URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(DOMAIN + "/" + url));
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            LOG.info("GETTING {} level {}", url, level);
            return CrawlerTask.getDocument(url2.toExternalForm(), cookies);
        })).thenApply(doc -> getLinks(doc, entry, domain, links, level)).thenAccept(l -> {
            LOG.info("Links {}", l);
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {

        launch(args);
    }

}
