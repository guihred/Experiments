
package contest;

import static contest.IadesHelper.addDomain;
import static contest.IadesHelper.extractURL;
import static contest.IadesHelper.saveContestValues;

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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.HibernateUtil;
import utils.SupplierEx;

public class IadesCrawler extends Application {

    private static final String DOMAIN = "http://www.iades.com.br";
    private static final Logger LOG = HasLogging.log();

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();

    @Override
	public void start(Stage primaryStage) {
        CrawlerTask.insertProxyConfig();
        primaryStage.setTitle("IADES Crawler");
		Parent node = createSplitTreeListDemoNode();
		primaryStage.setScene(new Scene(node));
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
				.addListener(
						(ob, old, value) -> new Thread(() -> saveContestValues(concurso, value, vagasView)).start());
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
            .filter(t -> !"#".equals(t.getValue()))
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
		if (newValue == null) {
			return;
		}

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

        launch(args);
    }


}
