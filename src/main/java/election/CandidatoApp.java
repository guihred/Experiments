package election;

import static election.CandidatoHelper.getRelevantFields;
import static election.CandidatoHelper.treeView;
import static election.CandidatoHelper.updateTable;
import static utils.CommonsFX.newSlider;

import japstudy.db.HibernateUtil;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.graph.PieGraph;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.ImageTableCell;

public class CandidatoApp extends Application {


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
            .addColumn("cidade", Cidade::getCity, "cidade").addColumn("eleito", CandidatoHelper::simNao, "eleito")
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

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }


}
