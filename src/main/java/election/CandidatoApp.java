package election;

import static election.CandidatoHelper.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.PieGraph;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.HibernateUtil;

public class CandidatoApp extends Application {
    @FXML
    private Text text18;
    @FXML
    private TableColumn<Candidato, String> fotoUrl;
    @FXML
    private TableColumn<Candidato, Boolean> eleito;
    @FXML
    private Slider slider20;
    @FXML
    private TreeView<String> treeView0;
    @FXML
    private TextField filter;
    @FXML
    private TableColumn<Candidato, LocalDate> nascimento;
    @FXML
    private TableView<Candidato> tableView2;
    @FXML
    private TableColumn<Candidato, Cidade> cidade;
    @FXML
    private PieGraph pieGraph;
    @FXML
    private SimpleIntegerProperty maxResult;
    @FXML
    private SimpleStringProperty column;
    private SimpleIntegerProperty first = new SimpleIntegerProperty(0);
    @FXML
    private ObservableList<Candidato> candidates;
    @FXML
    private ObservableMap<String, Set<String>> fieldMap;
    @FXML
    private ObservableMap<String, CheckBox> portChecks;

    public void initialize() {
        CrawlerTask.insertProxyConfig();
        List<String> relevantFields = getRelevantFields();
        for (String field : relevantFields) {
            SimpleTreeViewBuilder.addToRoot(treeView0, field, distinct(field));
        }
        CandidatoHelper.configTable(fotoUrl, cidade, eleito, nascimento, tableView2);
        tableView2.setItems(CommonsFX.newFastFilter(filter, candidates.filtered(e -> true)));
        column.addListener((ob, o, n) -> updateTable(first, maxResult.get(), n, pieGraph, candidates, fieldMap));
        maxResult
            .addListener((ob, o, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, candidates, fieldMap));
        slider20.valueProperty().bindBidirectional(pieGraph.legendsRadiusProperty());
        treeView0.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, newValue) -> onChangeElement(fieldMap, portChecks, newValue));
        bindTextToMap(text18, fieldMap);
        fieldMap.addListener(
            (Observable e) -> updateTable(first, maxResult.get(), column.get(), pieGraph, candidates, fieldMap));
        column.set(relevantFields.get(0));
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Candidato App", "CandidatoApp.fxml", this, primaryStage);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
