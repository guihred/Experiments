package graphs.app;

import graphs.entities.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;
import utils.HasLogging;

public class GraphModelLauncher extends Application implements HasLogging {
	private final Graph graph = new Graph();
	private ConvergeLayout convergeLayout = new ConvergeLayout(graph);
	private ObservableList<Layout> layouts = FXCollections.observableArrayList(
	        new GridLayout(graph),
            new ConcentricLayout(graph),
			new CircleLayout(graph),
            new LayerLayout(graph),
			new RandomLayout(graph), 
			new CustomLayout(graph), 
			convergeLayout).sorted(Comparator.comparing(e -> e.getClass().getSimpleName().replace("Layout", "")));
    private ComboBox<Layout> selectLayout = new SimpleComboBoxBuilder<Layout>().items(layouts)
            .tooltip("Select Layout")
            .converter(l -> l.getClass().getSimpleName().replace("Layout", "")).build();

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();

		VBox vBox = new VBox();
        vBox.getChildren().add(CommonsFX.newButton("Shortest Path", ev -> displayDialogForShortestPath()));
        vBox.getChildren().add(CommonsFX.newButton("Kruskal", ev -> {
			graph.getModel().clearSelected();
			List<Edge> prim = graph.getModel().kruskal();
			prim.forEach(e -> e.setSelected(true));
		}));
        vBox.getChildren().add(CommonsFX.newButton("Articulations", ev -> {
			graph.getModel().clearSelected();
			graph.getModel().findArticulations();
		}));

        vBox.getChildren().add(CommonsFX.newButton("Sort Topology", ev -> {
			graph.getModel().clearSelected();
			graph.getModel().sortTopology();
		}));
		Layout layout = new ConvergeLayout(graph);

        vBox.getChildren().add(CommonsFX.newButton("Color", ev -> graph.getModel().coloring()));

		DelaunayTopology delaunayTopology = new DelaunayTopology(10, graph);
		ObservableList<BaseTopology> topologies = FXCollections.observableArrayList(delaunayTopology,
				new RandomTopology(50, graph), 
				new TreeTopology(30, graph),
                new CircleTopology(30, graph), 
                new GabrielTopology(30, graph), 
                new WordTopology(30, graph),
                new PackageTopology(graph),
                new NetworkTopology(graph),
				new ProjectTopology(graph))
				.sorted(Comparator.comparing(BaseTopology::getName));

        ComboBox<BaseTopology> topologySelect = new SimpleComboBoxBuilder<BaseTopology>().items(topologies)
                .tooltip("Select Topology")
                .converter(BaseTopology::getName)
                .build();

        topologySelect.getSelectionModel().select(0);
		vBox.getChildren().add(new HBox(topologySelect,
                CommonsFX.newButton("Go", e -> {
                    BaseTopology selectedItem = topologySelect.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        selectedItem.execute();
                    }
                })));

        Timeline timeline = new SimpleTimelineBuilder()
                .addKeyFrame(new Duration(50.0), convergeLayout.getEventHandler()).build();
		timeline.setCycleCount(Animation.INDEFINITE);
        vBox.getChildren().add(CommonsFX.newButton("Triangulate", ev -> {
            Animation.Status status = timeline.getStatus();
            if (status == Animation.Status.RUNNING) {
				timeline.stop();
			}
            graph.triangulate();
            if (status == Animation.Status.RUNNING) {
				timeline.play();
			}
		}));
        vBox.getChildren().add(CommonsFX.newButton("Voronoi", ev -> {
            Animation.Status status = timeline.getStatus();
            if (status == Animation.Status.RUNNING) {
				timeline.stop();
			}
			graph.voronoi();

		}));
		root.setLeft(vBox);
		root.setCenter(graph.getScrollPane());

		Scene scene = new Scene(root, 800, 600);

		primaryStage.setScene(scene);
		primaryStage.show();

		addGraphComponents();

		new CircleLayout(graph).execute();
		layout.execute();

        selectLayout.getSelectionModel().select(0);
        vBox.getChildren().add(new HBox(selectLayout,
                CommonsFX.newButton("Go", e -> {
                    Layout selectedItem = selectLayout.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        selectedItem.execute();
                    }
                })));
        vBox.getChildren().add(CommonsFX.newButton("Pause/Play", ev -> {
            Animation.Status status = timeline.getStatus();
            if (status == Animation.Status.RUNNING) {
				timeline.stop();
			} else {
				timeline.play();
			}
		}));
        vBox.getChildren().add(CommonsFX.newCheck("Show Weight", Graph.SHOW_WEIGHT));

	}


	private void addGraphComponents() {

		GraphModel model = graph.getModel();

		model.addCell("A", CellType.CIRCLE);
		model.addCell("B", CellType.CIRCLE);
		model.addCell("C", CellType.CIRCLE);
		model.addCell("D", CellType.CIRCLE);
		model.addCell("E", CellType.CIRCLE);
		model.addCell("F", CellType.CIRCLE);
		model.addCell("G", CellType.CIRCLE);
		model.addBiEdge("A", "B", 1);
		model.addBiEdge("A", "D", 1);
		model.addBiEdge("A", "C", 1);
		model.addBiEdge("B", "D", 1);
		model.addBiEdge("B", "E", 1);
		model.addBiEdge("C", "D", 1);
		model.addBiEdge("C", "F", 1);
		model.addBiEdge("D", "E", 1);
		model.addBiEdge("D", "F", 1);
		model.addBiEdge("D", "G", 1);
		model.addBiEdge("E", "G", 1);
		model.addBiEdge("F", "G", 1);
		graph.endUpdate();

	}

	private void displayDialogForShortestPath() {
		graph.getModel().clearSelected();
		Stage dialog = new Stage();
		dialog.setWidth(70);
		ObservableList<String> cells = FXCollections.observableArrayList(
                graph.getModel().getAllCells().stream().map(Cell::getCellId).collect(Collectors.toList()));
		ChoiceBox<String> c1 = new ChoiceBox<>(cells);
		ChoiceBox<String> c2 = new ChoiceBox<>(cells);
        Scene scene = new Scene(
                new VBox(new Text("Source"), c1, new Text("Target"), c2, CommonsFX.newButton("OK", event -> {
			if (c1.getValue() != null && c2.getValue() != null) {
				List<Edge> chain = graph.getModel().chainEdges(c1.getValue(), c2.getValue());
				chain.forEach(e -> {
					e.setSelected(true);
					e.getTarget().setSelected(true);
					e.getSource().setSelected(true);
				});
			}
			dialog.close();
		})));
		dialog.setScene(scene);
		dialog.show();
	}


	public static void main(String[] args) {
		launch(args);
	}
}
