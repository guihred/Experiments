package election;

import static utils.CommonsFX.newSlider;

import japstudy.db.HibernateUtil;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.PieGraph;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleTreeViewBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.ImageTableCell;

public class HibernateCrawler extends Application {
    private static final int RELEVANT_FIELD_THRESHOLD = 410;
    private CandidatoDAO candidatoDAO = new CandidatoDAO();

    @Override
    public void start(Stage primaryStage) throws Exception {

        IntegerProperty maxResult = new SimpleIntegerProperty(50);
        StringProperty column = new SimpleStringProperty("Partido");
        IntegerProperty first = new SimpleIntegerProperty(0);

        VBox root = new VBox();
        ObservableList<Object> candidates = FXCollections.observableArrayList();
        FilteredList<Object> candidatesFiltered = candidates.filtered(e -> true);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        TableView<Object> build = new SimpleTableViewBuilder<>().addColumn("fotoUrl", "fotoUrl", ImageTableCell::new)
            .addColumns("nome", "numero", "partido", "grauInstrucao", "cargo", "naturalidade", "ocupacao",
                "nomeCompleto", "votos")
            .addColumn("cidade", Cidade::getCity, "cidade").addColumn("eleito", HibernateCrawler::simNao, "eleito")
            .addColumn("nascimento", dateFormat::format, "nascimento").prefWidth(500).equalColumns()
            .items(candidatesFiltered).build();

        TextField newFastFilter = CommonsFX.newFastFilter(candidatesFiltered);
        root.getChildren().add(newFastFilter);
        root.getChildren().add(build);
        PieGraph pieGraph = new PieGraph();

        ObservableMap<String, Set<String>> fieldMap = FXCollections.observableHashMap();
        ComboBox<String> columnCombo = new SimpleComboBoxBuilder<String>().items(getRelevantFields())
            .onChange((old, n) -> updateTable(first, maxResult.get(), n, pieGraph, candidates, fieldMap)).bind(column)
            .select(0).build();
        SimpleComboBoxBuilder<Number> maxBuilder = new SimpleComboBoxBuilder<>();
        ComboBox<Number> maxCombo = maxBuilder.items(10, 50, 100, 200)
            .onChange((old, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, candidates, fieldMap))
            .bind(maxResult).select(0).build();
        Text text = new Text("");
        VBox propSlider = newSlider("", 0, 1., pieGraph.legendsRadiusProperty());

        root.getChildren().add(new HBox(columnCombo, maxCombo, propSlider, text));
        root.getChildren().add(pieGraph);
        text.textProperty().bind(Bindings.createStringBinding(() -> fieldMap.entrySet().stream()
            .filter(e -> !e.getValue().isEmpty()).map(Objects::toString).collect(Collectors.joining(",")), fieldMap));

        BorderPane borderPane = new BorderPane(root);
        TreeView<String> treeView = treeView(fieldMap, first, maxResult, column, pieGraph, candidates);
        borderPane.setLeft(treeView);
        primaryStage.setTitle("Hibernate Entities");
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        maxBuilder.select(0);
        primaryStage.show();
    }

    private List<String> getRelevantFields() {
        return ClassReflectionUtils.getFields(Candidato.class).stream()
            .filter(e -> candidatoDAO.distinctNumber(e) < RELEVANT_FIELD_THRESHOLD).collect(Collectors.toList());
    }

    private TreeView<String> treeView(ObservableMap<String, Set<String>> fieldMap, IntegerProperty first,
        IntegerProperty maxResult, StringProperty column, PieGraph pieGraph, ObservableList<Object> candidates) {
        Map<String, CheckBox> portChecks = new HashMap<>();
        SimpleTreeViewBuilder<String> treeView = new SimpleTreeViewBuilder<String>().root("Root").editable(false)
            .showRoot(false).onSelect(newValue -> {
                if (newValue != null && newValue.isLeaf()) {
                    String value = newValue.getValue();
                    String parent = newValue.getParent().getValue();
                    if (!portChecks.containsKey(value)) {
                        portChecks.put(value, new CheckBox());

                        portChecks.get(value).selectedProperty()
                            .addListener((ob, o, val) -> addIfChecked(parent, fieldMap, value, val));
                    }

                    newValue.setGraphic(portChecks.get(value));
                }
            });
        for (String field : getRelevantFields()) {
            List<String> distinct = candidatoDAO.distinct(field);
            fieldMap.put(field, FXCollections.observableSet());
            treeView.addItem(field, distinct);

        }

        fieldMap.addListener((MapChangeListener<String, Set<String>>) e -> updateTable(first, maxResult.get(),
            column.get(), pieGraph, candidates, fieldMap));

        return treeView.build();
    }

    private void updateTable(IntegerProperty first, int maxResult, String column, PieGraph pieGraph,
        ObservableList<Object> observableArrayList, Map<String, Set<String>> fieldMap) {
        List<Candidato> list = candidatoDAO.list(first.get(), maxResult, fieldMap);
        observableArrayList.setAll(list);
        Map<String, Long> histogram = candidatoDAO.histogram(column, fieldMap);
        pieGraph.setHistogram(histogram);
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }

    private static void addIfChecked(String parent, Map<String, Set<String>> fieldMap, String value, Boolean val) {
        Set<String> set = fieldMap.remove(parent);
        if (!val) {
            set.remove(value);
        } else {
            set.add(value);
        }
        fieldMap.put(parent, set);
    }

    private static String simNao(Boolean a) {
        return a ? "Sim" : "Não";
    }
}
