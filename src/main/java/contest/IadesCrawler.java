package contest;

import static contest.IadesHelper.saveContestValues;
import static utils.CommonsFX.onCloseWindow;
import static utils.ExtractUtils.addDomain;

import java.net.URL;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.HibernateUtil;
import utils.RunnableEx;

public class IadesCrawler extends Application {
    private static final String DOMAIN = "http://www.iades.com.br";
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
    private ObservableList<Concurso> concursos = FXCollections.observableArrayList();
    private Set<String> links = new HashSet<>();
    private Property<Concurso> concurso = new SimpleObjectProperty<>();
    private SimpleStringProperty currentDomain = new SimpleStringProperty(DOMAIN);

    @Override
    public void start(Stage primaryStage) throws Exception {
        ExtractUtils.insertProxyConfig();
        CommonsFX.loadFXML("IADES Crawler", "IadesCrawler.fxml", this, primaryStage);
        createSplitTreeListDemoNode();
        CommonsFX.addCSS(primaryStage.getScene(), "filesComparator.css");
        onCloseWindow(primaryStage, HibernateUtil::shutdown);
    }

    private void createSplitTreeListDemoNode() {
        treeView1.setRoot(new TreeItem<>(new SimpleEntry<>("", DOMAIN + "/inscricao/?v=encerrado")));
        SimpleTreeViewBuilder.onSelect(treeView1, this::getNewLinks);
        listView3.setCellFactory(SimpleListViewBuilder.newCellFactory(IadesHelper::addClasses));
        listView3.setItems(FXCollections.observableArrayList());
        SimpleListViewBuilder.onSelect(listView3,
                (old, value) -> RunnableEx.runNewThread(() -> saveContestValues(concurso, value, listView3)));
        tableColumn4.setCellFactory(SimpleTableViewBuilder.newCellFactory(IadesHelper::addClasses));
        tableView2.setItems(concursos);
        SimpleTableViewBuilder.onSelect(tableView2, (old, value) -> {
            concurso.setValue(value);
            listView3.setItems(value.getVagas());
        });
    }

    private List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url, int level) {
        List<Map.Entry<String, String>> linksFound = doc.select("a").stream()
                .map(l -> new SimpleEntry<>(l.text(), addDomain(currentDomain, l.attr("href"))))
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
        RunnableEx.runInPlatform(() -> {
            URL url2 = new URL(url);
            currentDomain.set(url2.getProtocol() + "://" + url2.getHost());
            Document doc = ExtractUtils.getDocument(url);
            List<Entry<String, String>> l = getLinks(doc, entry, level);
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.stream().sorted(Comparator.comparing(Entry<String, String>::getKey))
                    .forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
