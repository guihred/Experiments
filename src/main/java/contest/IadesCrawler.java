package contest;

import static contest.IadesHelper.saveContestValues;
import static utils.CommonsFX.onCloseWindow;
import static utils.ExtractUtils.addDomain;

import extract.web.JsoupUtils;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import simplebuilder.ListHelper;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.HibernateUtil;
import utils.ProjectProperties;
import utils.ex.RunnableEx;

public class IadesCrawler extends Application {
    private static final String DOMAIN = ProjectProperties.getField();
    @FXML
    private TableView<Concurso> tableView2;
    @FXML
    private SplitPane splitPane0;
    @FXML
    private TableColumn<Concurso, String> tableColumn4;
    @FXML
    private TreeView<Map.Entry<String, String>> treeView1;

    @FXML
    private ListView<String> listView3;

    @FXML
    private TextField filter;

    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();
    private Set<String> links = new LinkedHashSet<>();
    private Property<Concurso> concurso = new SimpleObjectProperty<>();
    private SimpleStringProperty currentDomain = new SimpleStringProperty(DOMAIN);

    public void initialize() {
        treeView1.setRoot(new TreeItem<>(new SimpleEntry<>("", DOMAIN + "/inscricao/?v=encerrado")));
        SimpleTreeViewBuilder.onSelect(treeView1, this::getNewLinks);
        listView3.setCellFactory(SimpleListViewBuilder.newCellFactory(IadesHelper::addClasses));
        listView3.setItems(FXCollections.observableArrayList());
        SimpleListViewBuilder.onSelect(listView3,
                (old, value) -> RunnableEx.runNewThread(() -> saveContestValues(concurso, value, listView3)));
        tableColumn4.setCellFactory(SimpleTableViewBuilder.newCellFactory(IadesHelper::addClasses));
        SimpleTableViewBuilder.of(tableView2).items(concursos).equalColumns().onSelect((old, value) -> {
            concurso.setValue(value);
            listView3.setItems(value.getVagas());
        });

    }

    @Override
    public void start(Stage primaryStage) {
        ExtractUtils.insertProxyConfig();
        CommonsFX.loadFXML("IADES Crawler", "IadesCrawler.fxml", this, primaryStage);
        CommonsFX.addCSS(primaryStage.getScene(), "filesComparator.css");
        onCloseWindow(primaryStage, HibernateUtil::shutdown);
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url, int level) {
        List<Map.Entry<String, String>> linksFound = doc.select("a").stream()
                .map(l -> new SimpleEntry<>(l.text(), addDomain(currentDomain, l.attr("abs:href"))))
                .filter(t -> !"#".equals(t.getValue()))
                .filter(t -> StringUtils.isNotBlank(t.getKey()) && !t.getKey().matches("\\d+\\..*|[\\d \\-]+"))
                .filter(t -> level < 1 || t.getKey().contains("Provas") || t.getKey().contains("Gabarito"))
                .filter(t -> links.add(t.getValue())).distinct().collect(Collectors.toList());
        if (level == 1 && !linksFound.isEmpty()) {
            Concurso e2 = new Concurso();
            e2.setUrl(url.getValue());
            String key = url.getKey();
            String[] name = key.split("Processo|Inscriç|Concurso|Vestibular");
            e2.setNome(name[0]);
            e2.setLinksFound(linksFound);
            doc.select("fieldset").stream().filter(e -> e.select("legend").text().contains("Vagas"))
                    .flatMap(e -> e.select("b").stream()).forEach(e -> e2.getVagas().add(e.text()));
            concursos.add(e2);
        }
        return linksFound;
    }

    private void getNewLinks(TreeItem<Map.Entry<String, String>> newValue) {
        if (newValue == null) {
            return;
        }
        Entry<String, String> entry = newValue.getValue();
        String url = entry.getValue();

        if (IadesHelper.hasFileExtension(url)) {
            ExtractUtils.extractURL(url);
            return;
        }
        if (!newValue.getChildren().isEmpty()) {
            return;
        }
        int level = treeView1.getTreeItemLevel(newValue);
        RunnableEx.runNewThread(() -> {
            URL url2 = new URL(url);
            currentDomain.set(url2.getProtocol() + "://" + url2.getHost());
            Document doc = JsoupUtils.getDocument(url);
            return getLinks(doc, entry, level);
        }, l -> CommonsFX.runInPlatform(() -> {
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            ObservableList<Entry<String, String>> linkList = FXCollections.<Entry<String, String>>observableArrayList();
            FilteredList<Entry<String, String>> filteredChildren = linkList.filtered(e -> true);
            ListHelper.referenceMapping(filteredChildren, TreeItem::new, newValue.getChildren());
            if (level == 0) {
                CommonsFX.newFastFilter(filter, filteredChildren);
            }
            l.stream().collect(Collectors.toCollection(() -> linkList));

        }));

    }

    public static void main(String[] args) {
        launch(args);
    }
}
