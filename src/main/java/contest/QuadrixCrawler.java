package contest;

import static utils.CommonsFX.onCloseWindow;
import static utils.ex.RunnableEx.runNewThread;
import static utils.ex.SupplierEx.getIgnore;
import static utils.ex.SupplierEx.orElse;

import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
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
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.jsoup.nodes.Document;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.HibernateUtil;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class QuadrixCrawler extends Application {

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
        Map.Entry<String, String> simpleEntry = new SimpleEntry<>("",
            QuadrixHelper.addQuadrixDomain("encerrados.aspx"));
        treeBuilder.setRoot(new TreeItem<>(simpleEntry));
        Property<Concurso> concurso = new SimpleObjectProperty<>();

        SimpleTreeViewBuilder.onSelect(treeBuilder, t -> getNewLinks(t, treeBuilder));
        vagasList.setItems(FXCollections.observableArrayList());
        vagasList.setCellFactory(SimpleListViewBuilder.newCellFactory(IadesHelper::addClasses));

        tableColumn4.setCellFactory(SimpleTableViewBuilder.newCellFactory(IadesHelper::addClasses));
        SimpleListViewBuilder.onSelect(vagasList,
            (old, value) -> runNewThread(() -> QuadrixHelper.saveConcurso(concurso, vagasList, value)));
        concursosTable.setItems(concursos);
        SimpleTableViewBuilder.onSelect(concursosTable, (old, v) -> RunnableEx.runIf(v, value -> {
            concurso.setValue(value);
            value.getVagas().sort(String.CASE_INSENSITIVE_ORDER);
            vagasList.setItems(value.getVagas());
        }));
    }

    @Override
    public void start(Stage primaryStage) {
        ExtractUtils.insertProxyConfig();
        CommonsFX.loadFXML("Quadrix Crawler", "QuadrixCrawler.fxml", this, primaryStage);
        onCloseWindow(primaryStage, HibernateUtil::shutdown);
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
        SimpleStringProperty domain = new SimpleStringProperty(QuadrixHelper.QUADRIX_DOMAIN);
        CommonsFX.runInPlatform(() -> {
            URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(QuadrixHelper.addQuadrixDomain(url)));
            domain.set(url2.getProtocol() + "://" + url2.getHost());
            Document doc = QuadrixHelper.getDocumentCookies(url2);
            List<Entry<String, String>> l = QuadrixHelper.getLinks(doc, entry, domain, level, concursos, links);
            links.addAll(l.stream().map(Entry<String, String>::getValue).collect(Collectors.toList()));
            l.forEach(m -> newValue.getChildren().add(new TreeItem<>(m)));
        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}
