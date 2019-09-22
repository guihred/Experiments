package election;

import static election.CandidatoHelper.*;
import static simplebuilder.SimpleTableViewBuilder.equalColumns;
import static simplebuilder.SimpleTableViewBuilder.setFormat;

import java.time.LocalDate;
import java.util.Set;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.PieGraph;
import simplebuilder.SimpleTreeViewBuilder;
import utils.*;

public class CandidatoApp extends Application {
    @FXML
    private Text text18;
    @FXML
    private TableColumn<Candidato, String> fotoUrl;
    @FXML
    private TableColumn<Candidato, Boolean> eleito;
    @FXML
    private ComboBox<String> comboBox16;
    @FXML
    private Slider slider20;
    @FXML
    private TreeView<String> treeView0;
    @FXML
    private ComboBox<Number> comboBox17;
    @FXML
    private TextField textField1;
    @FXML
    private TableColumn<Candidato, LocalDate> nascimento;
    @FXML
    private TableView<Candidato> tableView2;
    @FXML
    private TableColumn<Candidato, Cidade> cidade;
    @FXML
    private PieGraph pieGraph;
    private SimpleIntegerProperty maxResult = new SimpleIntegerProperty(50);
    private SimpleStringProperty column = new SimpleStringProperty("partido");
    private SimpleIntegerProperty first = new SimpleIntegerProperty(0);
    private ObservableList<Candidato> candidates = FXCollections.observableArrayList();
    private ObservableMap<String, Set<String>> fieldMap = FXCollections.observableHashMap();

    public void initialize() {
        for (String field : getRelevantFields()) {
            SimpleTreeViewBuilder.addToRoot(treeView0, field, distinct(field));
        }

        CrawlerTask.insertProxyConfig();
        fotoUrl.setCellFactory(ImageTableCell::new);
        cidade.setCellFactory(setFormat(Cidade::getCity));
        eleito.setCellFactory(setFormat(StringSigaUtils::simNao));
        nascimento.setCellFactory(setFormat(DateFormatUtils::formatDate));
        equalColumns(tableView2);
        column.bind(comboBox16.getSelectionModel().selectedItemProperty());
        maxResult.bind(comboBox17.getSelectionModel().selectedItemProperty());
        tableView2.setItems(CommonsFX.newFastFilter(textField1, candidates.filtered(e -> true)));
        comboBox16.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, n) -> updateTable(first, maxResult.get(), n, pieGraph, candidates, fieldMap));
        comboBox17.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, candidates, fieldMap));
        slider20.valueProperty().bindBidirectional(pieGraph.legendsRadiusProperty());
        ObservableMap<String, CheckBox> portChecks = FXCollections.observableHashMap();
        treeView0.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, newValue) -> onChangeElement(fieldMap, portChecks, newValue));
        bindTextToMap(text18, fieldMap);
        fieldMap.addListener(
            (Observable e) -> updateTable(first, maxResult.get(), column.get(), pieGraph, candidates, fieldMap));
        fieldMap.put(getRelevantFields().get(0), FXCollections.observableSet());
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
