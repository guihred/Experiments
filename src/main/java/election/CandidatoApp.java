package election;

import static election.CandidatoHelper.distinct;
import static election.CandidatoHelper.getRelevantFields;
import static election.CandidatoHelper.onChangeElement;
import static election.CandidatoHelper.updateTable;
import static simplebuilder.SimpleTableViewBuilder.setFormat;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.PieGraph;
import simplebuilder.SimpleTableViewBuilder;
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
            List<String> distinct = distinct(field);
            SimpleTreeViewBuilder.addToRoot(treeView0, field, distinct);
        }

        CrawlerTask.insertProxyConfig();
        fotoUrl.setCellFactory(ImageTableCell::new);
        cidade.setCellFactory(setFormat(Cidade::getCity));
        eleito.setCellFactory(setFormat(CandidatoHelper::simNao));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        nascimento.setCellFactory(setFormat(dateFormat::format));
        SimpleTableViewBuilder.equalColumns(tableView2);
        FilteredList<Candidato> filtered = candidates.filtered(e -> true);
        CommonsFX.newFastFilter(textField1, filtered);
        column.bind(comboBox16.getSelectionModel().selectedItemProperty());
        maxResult.bind(comboBox17.getSelectionModel().selectedItemProperty());
        tableView2.setItems(filtered);
        comboBox16.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, n) -> updateTable(first, maxResult.get(), n, pieGraph, candidates, fieldMap));
        comboBox17.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, candidates, fieldMap));
        slider20.valueProperty().bindBidirectional(pieGraph.legendsRadiusProperty());
        Map<String, CheckBox> portChecks = new HashMap<>();
        treeView0.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, newValue) -> onChangeElement(fieldMap, portChecks, newValue));
        text18.textProperty().bind(Bindings.createStringBinding(() -> fieldMap.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty()).map(Objects::toString).collect(Collectors.joining(",")), fieldMap));
        fieldMap.addListener((MapChangeListener<String, Set<String>>) e -> updateTable(first, maxResult.get(),
            column.get(), pieGraph, candidates, fieldMap));
        fieldMap.put(getRelevantFields().get(0), FXCollections.observableSet());
    }

    @Override
    public void start(Stage primaryStage) {
        File file = ResourceFXUtils.toFile("CandidatoApp.fxml");
        CommonsFX.loadFXML(this, file, "Candidato App", primaryStage);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
