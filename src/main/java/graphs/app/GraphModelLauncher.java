package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Edge;
import graphs.entities.Graph;
import java.util.Comparator;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;

public class GraphModelLauncher extends Application {
	private final Graph graph = new Graph();
	private ConvergeLayout convergeLayout = new ConvergeLayout(graph);
	private ObservableList<Layout> layouts = FXCollections
			.observableArrayList(new GridLayout(graph), new ConcentricLayout(graph), new CircleLayout(graph),
					new LayerLayout(graph), new RandomLayout(graph), new CustomLayout(graph), convergeLayout)
			.sorted(Comparator.comparing(Layout::getName));

	private ComboBox<Layout> selectLayout = new SimpleComboBoxBuilder<Layout>().items(layouts).tooltip("Select Layout")
			.converter(Layout::getName).select(0).build();
	private Timeline timeline = new SimpleTimelineBuilder()
			.addKeyFrame(50.0, convergeLayout.getEventHandler()).cycleCount(Animation.INDEFINITE).build();

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setTitle("Graph Application");
		BorderPane root = new BorderPane();

		VBox buttons = new VBox();
		buttons.getChildren().add(CommonsFX.newButton("Color", ev -> graph.getModel().coloring()));
		buttons.getChildren().add(CommonsFX.newButton("Kruskal", ev -> {
			graph.getModel().clearSelected();
			List<Edge> prim = graph.getModel().kruskal();
			prim.forEach(e -> e.setSelected(true));
		}));
		buttons.getChildren().add(getVoronoiOptions());
		buttons.getChildren().add(getTriangulateOption());
		buttons.getChildren().add(CommonsFX.newButton("Articulations", ev -> {
			graph.getModel().clearSelected();
			graph.getModel().findArticulations();
		}));
		buttons.getChildren().add(CommonsFX.newButton("Sort Topology", ev -> {
			graph.getModel().clearSelected();
			graph.getModel().sortTopology();
		}));
		buttons.getChildren().add(CommonsFX.newButton("Page Rank", ev -> {
			graph.getModel().clearSelected();
			graph.getModel().pageRank();
		}));

		buttons.getChildren().add(getLayoutOptions());
		buttons.getChildren().add(getShortestPathOptions());
		buttons.getChildren().add(getTopologyOptions());
		buttons.getChildren().add(getPausePlayOption());
		buttons.getChildren().add(CommonsFX.newCheck("Show Weight", Graph.SHOW_WEIGHT));

		root.setLeft(buttons);
		root.setCenter(graph.getScrollPane());

		final int width = 800;
		Scene scene = new Scene(root, width, width);

		primaryStage.setScene(scene);
		primaryStage.show();
		addGraphComponents();
		new CircleLayout(graph).execute();
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

	private VBox getLayoutOptions() {
		return new VBox(new Text("Layout"), new HBox(selectLayout, CommonsFX.newButton("Go", e -> {
			Layout selectedItem = selectLayout.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				selectedItem.execute();
			}
		})));
	}

	private Button getPausePlayOption() {
		return CommonsFX.newButton("Pause/Play", ev -> {
			Timeline.Status status = timeline.getStatus();
			if (status == Timeline.Status.RUNNING) {
				timeline.stop();
			} else {
				timeline.play();
			}
		});
	}

	private Node getShortestPathOptions() {

		ObservableList<String> cells = graph.getModel().getCellIds();
		ComboBox<String> c1 = new SimpleComboBoxBuilder<String>().items(cells).build();
		ComboBox<String> c2 = new SimpleComboBoxBuilder<String>().items(cells).build();
		cells.addListener((final Observable observable) -> {
			if (!cells.isEmpty()) {
				c1.selectionModelProperty().get().select(0);
				c2.selectionModelProperty().get().select(cells.size() - 1);
			}
		});

		VBox source = new VBox(new Text("Source"), c1);
		VBox target = new VBox(new Text("Target"), c2);
		Button button = CommonsFX.newButton("_Path", event -> {
			graph.getModel().clearSelected();
			if (c1.getValue() != null && c2.getValue() != null) {
				List<Edge> chain = graph.getModel().chainEdges(c1.getValue(), c2.getValue());
				chain.forEach(e -> {
					e.setSelected(true);
					e.getTarget().setSelected(true);
					e.getSource().setSelected(true);
				});
			}
		});

		FlowPane flowPane = new FlowPane(source, target, new VBox(new Text(""), button));
		final int maxWidth = 160;
		flowPane.setMaxWidth(maxWidth);
		return flowPane;
	}

	private VBox getTopologyOptions() {
		DelaunayTopology delaunayTopology = new DelaunayTopology(10, graph);
		final int netSize = 30;
		PackageTopology packageTopology = new PackageTopology(graph);
		NetworkTopology networkTopology = new NetworkTopology(graph);
		ObservableList<BaseTopology> topologies = FXCollections
				.observableArrayList(delaunayTopology, new RandomTopology(netSize, graph),
						new TreeTopology(netSize, graph), new CircleTopology(netSize, graph),
						new GabrielTopology(netSize, graph), new WordTopology(netSize * 3, graph),
						packageTopology, networkTopology, new ProjectTopology(graph))
				.sorted(Comparator.comparing(BaseTopology::getName));
		TextField networkField = new TextField(networkTopology.getNetworkAddress());
		networkTopology.networkAddressProperty().bind(networkField.textProperty());
		ComboBox<String> packageSelect = new SimpleComboBoxBuilder<String>().items(packageTopology.getPackages())
				.tooltip("Package").onChange((old, newV) -> packageTopology.setChosenPackageName(newV)).select(0)
				.build();
		ComboBox<BaseTopology> topologySelect = new SimpleComboBoxBuilder<BaseTopology>().items(topologies)
				.tooltip("Select Topology").converter(BaseTopology::getName).build();

		networkField.visibleProperty()
		.bind(topologySelect.getSelectionModel().selectedItemProperty().isEqualTo(networkTopology));
		networkField.managedProperty().bind(networkField.visibleProperty());

		packageSelect.visibleProperty()
		.bind(topologySelect.getSelectionModel().selectedItemProperty().isEqualTo(packageTopology));
		packageSelect.managedProperty().bind(packageSelect.visibleProperty());

		topologySelect.getSelectionModel().select(0);
		HBox topologyOptions = new HBox(topologySelect, CommonsFX.newButton("Go", e -> {
			BaseTopology selectedItem = topologySelect.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				selectedItem.execute();
			}
		}));
		return new VBox(new Text("Topology"), topologyOptions, packageSelect, networkField);
	}

	private Button getTriangulateOption() {
		return CommonsFX.newButton("Triangulate", ev -> {
			Timeline.Status status = timeline.getStatus();
			if (status == Timeline.Status.RUNNING) {
				timeline.stop();
			}
			graph.triangulate();
			if (status == Timeline.Status.RUNNING) {
				timeline.play();
			}
		});
	}

	private Button getVoronoiOptions() {
		return CommonsFX.newButton("Voronoi", ev -> {
			Timeline.Status status = timeline.getStatus();
			if (status == Timeline.Status.RUNNING) {
				timeline.stop();
			}
			graph.voronoi();

		});
	}

	public static void main(final String[] args) {
		launch(args);
	}
}
