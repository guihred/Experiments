
package contest;

import static contest.IadesHelper.addDomain;
import static contest.IadesHelper.saveContestValues;
import static utils.CommonsFX.onCloseWindow;

import java.net.URL;
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
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.*;

public class IadesCrawler extends Application {

    private static final String DOMAIN = "http://www.iades.com.br";

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("IADES Crawler");
        CrawlerTask.insertProxyConfig();
        Parent node = createSplitTreeListDemoNode();
        primaryStage.setScene(new Scene(node));
        onCloseWindow(primaryStage, HibernateUtil::shutdown);
        primaryStage.getScene().getStylesheets().add(ResourceFXUtils.toExternalForm("filesComparator.css"));
        primaryStage.show();
    }

    private Parent createSplitTreeListDemoNode() {
        SimpleTreeViewBuilder<Map.Entry<String, String>> root = new SimpleTreeViewBuilder<Map.Entry<String, String>>()
            .root(new AbstractMap.SimpleEntry<>("", DOMAIN + "/inscricao/?v=encerrado"));
        TreeView<Map.Entry<String, String>> treeBuilder = root.build();
        Set<String> links = new HashSet<>();
        Property<Concurso> concurso = new SimpleObjectProperty<>();
        root.onSelect(t -> getNewLinks(t, links, treeBuilder));
        SimpleListViewBuilder<String> listBuilder = new SimpleListViewBuilder<>();
        listBuilder.cellFactory(SimpleListViewBuilder.newCellFactory(IadesHelper::addClasses));
        listBuilder.items(FXCollections.observableArrayList()).onSelect(
            (old, value) -> RunnableEx.runNewThread(() -> saveContestValues(concurso, value, listBuilder.build())));
        SimpleTableViewBuilder<Concurso> tableBuilder = new SimpleTableViewBuilder<Concurso>().items(concursos)
            .addColumn("nome", IadesHelper::addClasses).onSelect((old, value) -> {
                concurso.setValue(value);
                listBuilder.items(value.getVagas());
            }).prefWidthColumns(1).minWidth(200);

        return new VBox(new SplitPane(treeBuilder, tableBuilder.build(), listBuilder.build()));
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url,
        SimpleStringProperty domain, Set<String> links, int level) {
        List<Map.Entry<String, String>> linksFound = doc.select("a").stream()
            .map(l -> new AbstractMap.SimpleEntry<>(l.text(), addDomain(domain, l.attr("href"))))
            .filter(t -> !"#".equals(t.getValue()))
            .filter(t -> StringUtils.isNotBlank(t.getKey()) && !t.getKey().matches("\\d+\\..*"))
            .filter(t -> level < 1 || t.getKey().contains("Provas") || t.getKey().contains("Gabarito"))
            .filter(t -> links.add(t.getValue())).distinct().collect(Collectors.toList());
        if (level == 1 && !linksFound.isEmpty()) {
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
        String url = entry.getValue();

        if (url.endsWith(".pdf") || url.endsWith(".zip") || url.endsWith(".rar")) {
            ExtractUtils.extractURL(url);
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
            return ExtractUtils.getDocument(url, CrawlerTask.getEncodedAuthorization());
        })).thenApply(doc -> getLinks(doc, entry, domain, links, level)).thenAccept(l -> {
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.stream().sorted(Comparator.comparing(Entry<String, String>::getKey))
                .forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {

        launch(args);
    }

}
