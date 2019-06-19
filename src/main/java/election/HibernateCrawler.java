package election;

import japstudy.db.HibernateUtil;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ml.graph.PieGraph;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.ImageTableCell;

public class HibernateCrawler extends Application {
    private CandidatoDAO candidatoDAO = new CandidatoDAO();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hibernate Entities");

        IntegerProperty maxResult = new SimpleIntegerProperty(50);
        StringProperty column = new SimpleStringProperty("Partido");
        IntegerProperty first = new SimpleIntegerProperty(0);

        VBox root = new VBox();
        ObservableList<Object> observableArrayList = FXCollections.observableArrayList();
        FilteredList<Object> value = observableArrayList.filtered(e -> true);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        TableView<Object> build = new SimpleTableViewBuilder<>()
            .addColumn("fotoUrl", "fotoUrl", s -> new ImageTableCell<>())
            .addColumn("nome", "nome")
            .addColumn("numero", "numero")
            .addColumn("partido", "partido")
            .addColumn("cidade", Cidade::getCity, "cidade")
            .addColumn("votos", "votos")
            .addColumn("eleito", this::simNao, "eleito")
            .addColumn("grauInstrucao", "grauInstrucao")
            .addColumn("cargo", "cargo")
            .addColumn("nascimento", dateFormat::format, "nascimento")
            .addColumn("naturalidade", "naturalidade")
            .addColumn("ocupacao", "ocupacao")
            .addColumn("nomeCompleto", "nomeCompleto")

            .prefWidth(500).equalColumns().items(value).build();


        TextField newFastFilter = CommonsFX.newFastFilter(value);
        root.getChildren().add(newFastFilter);
        root.getChildren().add(build);
        PieGraph pieGraph = new PieGraph();

        ComboBox<String> columnCombo = new SimpleComboBoxBuilder<String>()
            .items("partido", "grauInstrucao", "cargo")
            .onChange((old, n) -> updateTable(first, maxResult.get(), n, pieGraph, observableArrayList)).bind(column)
            .select(0).build();
        ComboBox<Number> maxCombo = new SimpleComboBoxBuilder<Number>().items(10, 50, 100, 200)
            .onChange((old, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, observableArrayList))
            .bind(maxResult)
            .select(0)
            .build();
        root.getChildren().add(new HBox(columnCombo, maxCombo));
        root.getChildren().add(pieGraph);
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());

        primaryStage.show();
    }

    private String simNao(Boolean a) {
        return a ? "Sim" : "Não";
    }

    private void updateTable(IntegerProperty first, Integer maxResult, String column,
        PieGraph pieGraph, ObservableList<Object> observableArrayList) {
        List<Candidato> list = candidatoDAO.list(first.get(), maxResult);
        observableArrayList.setAll(list);
        Map<String, Long> histogram = candidatoDAO.histogram(column);
        pieGraph.setHistogram(histogram);
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }
}
