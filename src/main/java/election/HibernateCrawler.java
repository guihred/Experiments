package election;

import japstudy.db.HibernateUtil;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
            .addColumn("nome", "nome").addColumn("numero", "numero").addColumn("partido", "partido")
            .addColumn("cidade", Cidade::getCity, "cidade").addColumn("votos", "votos")
            .addColumn("eleito", HibernateCrawler::simNao, "eleito").addColumn("grauInstrucao", "grauInstrucao")
            .addColumn("cargo", "cargo").addColumn("nascimento", dateFormat::format, "nascimento")
            .addColumn("naturalidade", "naturalidade").addColumn("ocupacao", "ocupacao")
            .addColumn("nomeCompleto", "nomeCompleto").prefWidth(500).equalColumns().items(candidatesFiltered).build();

        TextField newFastFilter = CommonsFX.newFastFilter(candidatesFiltered);
        root.getChildren().add(newFastFilter);
        root.getChildren().add(build);
        PieGraph pieGraph = new PieGraph();

        Map<String, Set<String>> fieldMap = FXCollections.observableHashMap();
        ComboBox<String> columnCombo = new SimpleComboBoxBuilder<String>().items("partido", "grauInstrucao", "cargo")
            .onChange((old, n) -> updateTable(first, maxResult.get(), n, pieGraph, candidates, fieldMap)).bind(column)
            .select(0).build();
        ComboBox<Number> maxCombo = new SimpleComboBoxBuilder<Number>().items(10, 50, 100, 200)
            .onChange((old, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, candidates, fieldMap))
            .bind(maxResult).select(0).build();
        root.getChildren().add(new HBox(columnCombo, maxCombo));
        root.getChildren().add(pieGraph);

        BorderPane borderPane = new BorderPane(root);
        borderPane.setLeft(treeView(fieldMap, first, maxResult, column, pieGraph, candidates));
        primaryStage.setTitle("Hibernate Entities");
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());

        primaryStage.show();
    }

    private List<String> getRelevantFields() {
        return ClassReflectionUtils.getFields(Candidato.class).stream().filter(e -> candidatoDAO.distinctNumber(e) < 50)
            .collect(Collectors.toList());
    }

    private TreeView<String> treeView(Map<String, Set<String>> fieldMap, IntegerProperty first,
        IntegerProperty maxResult, StringProperty column, PieGraph pieGraph, ObservableList<Object> candidates) {
        Map<String, CheckBox> portChecks = new HashMap<>();
        SimpleTreeViewBuilder<String> treeView = new SimpleTreeViewBuilder<String>().root("Root").editable(false)
            .showRoot(false).onSelect(newValue -> {
                if (newValue != null && newValue.isLeaf()) {
                    String value = newValue.getValue();
                    if (!portChecks.containsKey(value)) {
                        portChecks.put(value, new CheckBox());

                        Set<String> set = fieldMap.get(newValue.getParent().getValue());
                        portChecks.get(value).selectedProperty()
                            .addListener((ob, o, val) -> addIfChecked(set, value, val));
                    }
                    newValue.setGraphic(portChecks.get(value));
                }
            });
        for (String field : getRelevantFields()) {
            List<String> distinct = candidatoDAO.distinct(field);
            ObservableSet<String> observableSet = FXCollections.observableSet();
            observableSet.addListener((Change<? extends String> e) -> updateTable(first, maxResult.get(), column.get(),
                pieGraph, candidates, fieldMap));
            fieldMap.put(field, observableSet);
            treeView.addItem(field, distinct);
        }

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

    private static void addIfChecked(Set<String> set, String value, Boolean val) {
        if (!val) {
            set.remove(value);
        } else {
            set.add(value);
        }

    }

    private static String simNao(Boolean a) {
        return a ? "Sim" : "Não";
    }
}
