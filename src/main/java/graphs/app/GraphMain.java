package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Edge;
import graphs.entities.Graph;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.CommonsFX;
import utils.ImageFXUtils;

public class GraphMain extends Application {

    @FXML
    private ComboBox<String> c2;

    @FXML
    private Graph graph;

    @FXML
    private ComboBox<String> c1;

    @FXML
    private CheckBox showHeight;

    @FXML
    private TextField networkField;

    @FXML
    private ComboBox<String> packageSelect;
    @FXML
    private ComboBox<Layout> selectLayout;
    @FXML
    private ComboBox<BaseTopology> topologySelect;
    @FXML
    private ConvergeLayout convergeLayout;
    @FXML
    private PackageTopology packageTopology;

    @FXML
    private NetworkTopology networkTopology;

    @FXML
    private MethodsTopology methodsTopology;
    @FXML
    private BorderPane borderPane;

    @FXML
    private Timeline timeline;

    public void initialize() {
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), convergeLayout.getEventHandler()));
        borderPane.setCenter(graph.getScrollPane());
        networkField.setText(networkTopology.getNetworkAddress());
        networkField.visibleProperty()
            .bind(topologySelect.getSelectionModel().selectedItemProperty().isEqualTo(networkTopology));
        networkField.managedProperty().bind(networkField.visibleProperty());
        packageSelect.managedProperty().bind(packageSelect.visibleProperty());
        packageSelect.setItems(packageTopology.getPackages());
        packageSelect.visibleProperty()
            .bind(topologySelect.getSelectionModel().selectedItemProperty().isEqualTo(packageTopology)
                .or(topologySelect.getSelectionModel().selectedItemProperty().isEqualTo(methodsTopology)));
        networkTopology.networkAddressProperty().bind(networkField.textProperty());
        ObservableList<String> cells = graph.getModel().getCellIds();
        c1.setItems(cells);
        c2.setItems(cells);
        cells.addListener((Observable observable) -> {
            if (!cells.isEmpty()) {
                c1.getSelectionModel().selectFirst();
                c2.getSelectionModel().selectLast();
            }
        });
        packageSelect.getSelectionModel().selectedItemProperty().addListener((ob, old, newV) -> {
            packageTopology.setChosenPackageName(newV);
            methodsTopology.setChosenPackageName(newV);
        });
        Edge.SHOW_WEIGHT.bind(showHeight.selectedProperty());
        addGraphComponents();
        topologySelect.getSelectionModel().selectFirst();
        selectLayout.getSelectionModel().selectFirst();
        packageSelect.getSelectionModel().selectFirst();
        selectLayout.getSelectionModel().getSelectedItem().execute();
    }

    public void onActionArticulations() {
        graph.getModel().clearSelected();
        graph.getModel().findArticulations();
    }

    public void onActionColor() {
        graph.getModel().coloring();
    }

    public void onActionGo() {
        Layout selectedItem = selectLayout.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.execute();
        }
    }

    public void onActionGo27() {
        BaseTopology selectedItem = topologySelect.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            selectedItem.execute();
        }
    }

    public void onActionKruskal() {
        graph.getModel().clearSelected();
        List<Edge> prim = graph.getModel().kruskal();
        prim.forEach(e -> e.setSelected(true));
    }

    public void onActionPageRank() {
        graph.getModel().clearSelected();
        graph.getModel().pageRank();
    }

    public void onActionPath() {
        graph.getModel().clearSelected();
        if (c1.getValue() != null && c2.getValue() != null) {
            List<Edge> chain = graph.getModel().chainEdges(c1.getValue(), c2.getValue());
            chain.forEach(e -> {
                e.setSelected(true);
                e.getTarget().setSelected(true);
                e.getSource().setSelected(true);
            });
        }
    }

    public void onActionPausePlay() {
        Timeline.Status status = timeline.getStatus();
        if (status == Timeline.Status.RUNNING) {
            timeline.stop();
        } else {
            timeline.play();
        }
    }

    public void onActionSortTopology() {
        graph.getModel().clearSelected();
        graph.getModel().sortTopology();
    }

    public void onActionTakeSnap() {
        double scaleValue = graph.getScrollPane().getScaleValue();
        Bounds bounds = graph.getScrollPane().getContent().getBoundsInLocal();
        ImageFXUtils.take(graph.getScrollPane().getContent(), bounds.getWidth(), bounds.getHeight(), scaleValue);
    }

    public void onActionTriangulate() {
        Timeline.Status status = timeline.getStatus();
        if (status == Timeline.Status.RUNNING) {
            timeline.stop();
        }
        graph.triangulate();
        if (status == Timeline.Status.RUNNING) {
            timeline.play();
        }
    }

    public void onActionVoronoi() {
        Timeline.Status status = timeline.getStatus();
        if (status == Timeline.Status.RUNNING) {
            timeline.stop();
        }
        graph.voronoi();
    }

    @Override
    public void start(final Stage primaryStage) {
        final int width = 800;
        CommonsFX.loadFXML("Graph Application", "GraphModelLauncher.fxml", this, primaryStage, width, width);
    }

    private void addGraphComponents() {
        graph.getModel().addCell("A", CellType.CIRCLE);
        graph.getModel().addCell("B", CellType.CIRCLE);
        graph.getModel().addCell("C", CellType.CIRCLE);
        graph.getModel().addCell("D", CellType.CIRCLE);
        graph.getModel().addCell("E", CellType.CIRCLE);
        graph.getModel().addCell("F", CellType.CIRCLE);
        graph.getModel().addCell("G", CellType.CIRCLE);
        graph.getModel().addBiEdge("A", "B", 1);
        graph.getModel().addBiEdge("A", "D", 1);
        graph.getModel().addBiEdge("A", "C", 1);
        graph.getModel().addBiEdge("B", "D", 1);
        graph.getModel().addBiEdge("B", "E", 1);
        graph.getModel().addBiEdge("C", "D", 1);
        graph.getModel().addBiEdge("C", "F", 1);
        graph.getModel().addBiEdge("D", "E", 1);
        graph.getModel().addBiEdge("D", "F", 1);
        graph.getModel().addBiEdge("D", "G", 1);
        graph.getModel().addBiEdge("E", "G", 1);
        graph.getModel().addBiEdge("F", "G", 1);
        graph.endUpdate();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
