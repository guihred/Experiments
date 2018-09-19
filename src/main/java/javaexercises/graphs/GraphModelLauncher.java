package javaexercises.graphs;

import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.CommonsFX;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;
import simplebuilder.SimpleTimelineBuilder;

public class GraphModelLauncher extends Application implements HasLogging {
	private final Graph graph = new Graph();
	private ConvergeLayout convergeLayout = new ConvergeLayout(graph);
	private ObservableList<Layout> layouts = FXCollections.observableArrayList(new GridLayout(graph),
            new ConcentricLayout(graph),
			new CircleLayout(graph),
			new RandomLayout(graph), new CustomLayout(graph), convergeLayout);
    private ChoiceBox<Layout> selectLayout = CommonsFX.newSelect(layouts,
			new SimpleConverter<>(l -> l.getClass().getSimpleName().replace("Layout", "")), "Select Layout");


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

	private void createTopology() {
		graph.getModel().clearSelected();
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();

		String[] words = getWords();

		Stream.of(words).sorted().forEach(a -> graph.getModel().addCell(a, CellType.CIRCLE));
		for (String v : words) {
			for (String w : words) {
				if (oneCharOff(w, v)) {
					graph.getModel().addBiEdge(v, w, 1);
				}
			}
		}
		graph.endUpdate();
		Layout selectedItem = selectLayout.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {
			selectedItem.execute();
		}
	}

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
		ObservableList<BaseTopology> topologies = FXCollections.observableArrayList(delaunayTopology, new RandomTopology(50, graph), new TreeTopology(
                        30, graph),
                new CircleTopology(30, graph), new GabrielTopology(30, graph), new ProjectTopology(30, graph));

        SimpleConverter<BaseTopology> converterTopology = new SimpleConverter<>(BaseTopology::getName);
        ChoiceBox<BaseTopology> topologySelect = CommonsFX.newSelect(topologies, converterTopology, "Select Topology");
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
			delaunayTopology.triangulate();
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

        vBox.getChildren().add(CommonsFX.newButton("Create Topology", ev -> createTopology()));
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

	private String[] getWords() {
        try (Stream<String> lines = Files.lines(ResourceFXUtils.toPath("alice.txt"))) {
            return lines.flatMap((String e) -> Stream.of(e.split("[^a-zA-Z]")))
					.filter(s -> s.length() == 4).map(String::toLowerCase).distinct().toArray(String[]::new);
        } catch (Exception e) {
            getLogger().error("", e);
		}

		return new String[] { "fine", "line", "mine", "nine", "pine", "vine", "wine", "wide", "wife", "wipe", "wire",
				"wind", "wing", "wink", "wins", "none", "gone", "note", "vote", "site", "nite", "bite" };
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

	public static boolean oneCharOff(String word1, String word2) {
		if (word1.length() != word2.length()) {
			return false;
		}

		int diffs = 0;

		for (int i = 0; i < word1.length(); i++) {
			if (word1.charAt(i) != word2.charAt(i) && ++diffs > 1) {
				return false;
			}
		}

		return diffs == 1;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
