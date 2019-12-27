
package contest;

import static contest.IadesHelper.addDomain;
import static contest.IadesHelper.saveContestValues;
import static utils.SupplierEx.getIgnore;
import static utils.SupplierEx.orElse;

import extract.UnRar;
import extract.UnZip;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
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
        listBuilder.items(FXCollections.observableArrayList()).onSelect(
            (old, value) -> new Thread(() -> saveContestValues(concurso, value, listBuilder.build()), "Save Contest")
                .start());
        SimpleTableViewBuilder<Concurso> tableBuilder = new SimpleTableViewBuilder<Concurso>().items(concursos)
            .addColumns("nome").onSelect((old, value) -> {
                concurso.setValue(value);
                listBuilder.items(value.getVagas());
            }).prefWidthColumns(1).minWidth(200);

        return new VBox(new SplitPane(treeBuilder, tableBuilder.build(), listBuilder.build()));
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url,
        SimpleStringProperty domain, Set<String> links, int level) {
        List<Map.Entry<String, String>> linksFound = doc.select("a").stream()
            .map(l -> new AbstractMap.SimpleEntry<>(l.text(), addDomain(domain, l.attr("href"))))
            .filter(t -> !"#".equals(t.getValue())).filter(t -> level < 1 || t.getKey().contains("aplicada"))
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
                URLConnection openConnection = url2.openConnection();
                openConnection.addRequestProperty("Proxy-Authorization",
                    "Basic " + CrawlerTask.getEncodedAuthorization());
                String collect = cookies.entrySet().stream().map(e -> e.toString()).collect(Collectors.joining("; "));
                openConnection.addRequestProperty("Cookie", collect);
                openConnection.setAllowUserInteraction(true);
                openConnection.setConnectTimeout(10000);
                System.out.println(openConnection.getRequestProperties());
                InputStream input = openConnection.getInputStream();
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
