package javaexercises.graphs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class GraphModelLauncher extends Application {

	Graph graph = new Graph();

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();

		graph = new Graph();
		VBox vBox = new VBox();
		vBox.getChildren().add(
				newButton(
						"Shortest Path",
						(ev) -> {
							graph.getModel().clearSelected();
							Stage dialog = new Stage();
							dialog.setWidth(70);
							dialog.initStyle(StageStyle.UTILITY);
							ObservableList<String> cells = FXCollections.observableArrayList(graph.getModel().getAllCells().stream()
									.map(c -> c.getCellId()).collect(Collectors.toList()));
							ChoiceBox<String> c1 = new ChoiceBox<>(cells);
							ChoiceBox<String> c2 = new ChoiceBox<>(cells);
							Scene scene = new Scene(new VBox(new Text("Source"), c1, new Text("Target"), c2, newButton("OK", (event) -> {
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

						}));
		vBox.getChildren().add(newButton("Kruskal", (ev) -> {
			graph.getModel().clearSelected();
			List<Edge> prim = graph.getModel().kruskal();
			prim.forEach(e -> e.setSelected(true));
		}));
		vBox.getChildren().add(newButton("Articulations", (ev) -> {
			graph.getModel().clearSelected();
			graph.getModel().findArticulations();
		}));

		vBox.getChildren().add(newButton("Sort Topology", (ev) -> {
			graph.getModel().clearSelected();
			graph.getModel().sortTopology();
		}));
		Layout layout = new ConvergeLayout(graph);

		vBox.getChildren().add(newButton("Color", (ev) -> graph.getModel().coloring()));

		DelaunayTopology delaunayTopology = new DelaunayTopology(10, graph);
		ObservableList<GenTopology> topologies = FXCollections.observableArrayList(delaunayTopology, new RandomTopology(50, graph), new TreeTopology(
				30, graph), new CircleTopology(30, graph), new GabrielTopology(30, graph));

		SimpleConverter<GenTopology> converterTopology = new SimpleConverter<>(l -> l.getName());
		ChoiceBox<GenTopology> topologySelect = newSelect(topologies, converterTopology, "Select Topology");
		vBox.getChildren().add(new HBox(topologySelect, newButton("Go", (e) -> topologySelect.getSelectionModel().getSelectedItem().execute())));
		ConvergeLayout convergeLayout = new ConvergeLayout(graph);
		Timeline timeline = new Timeline(new KeyFrame(new Duration(50.0), convergeLayout.eventHandler));
		timeline.setCycleCount(Animation.INDEFINITE);
		vBox.getChildren().add(newButton("Triangulate", (ev) -> {
			Status status = timeline.getStatus();
			if (status == Status.RUNNING) {
				timeline.stop();
			}
			delaunayTopology.triangulate();
			if (status == Status.RUNNING) {
				timeline.play();
			}
		}));
		vBox.getChildren().add(newButton("Voronoi", (ev) -> {
			Status status = timeline.getStatus();
			if (status == Status.RUNNING) {
				timeline.stop();
			}
			graph.voronoi();

		}));
		root.setLeft(vBox);
		root.setCenter(graph.getScrollPane());

		Scene scene = new Scene(root, 800, 600);
		// scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.show();

		addGraphComponents();

		new CircleLayout(graph).execute();
		layout.execute();
		// @SuppressWarnings("static-access")
		ObservableList<Layout> layouts = FXCollections.observableArrayList(new GridLayout(graph), new CircleLayout(graph), new RandomLayout(graph),
				new CustomLayout(graph), convergeLayout);
		SimpleConverter<Layout> converter = new SimpleConverter<>(l -> l.getClass().getSimpleName().replace("Layout", ""));
		ChoiceBox<Layout> selectLayout = newSelect(layouts, converter, "Select Layout");
		vBox.getChildren().add(newButton("Create Topology", (ev) -> {
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
		}));
		vBox.getChildren().add(new HBox(selectLayout, newButton("Go", (e) -> selectLayout.getSelectionModel().getSelectedItem().execute())));
		vBox.getChildren().add(newButton("Pause/Play", (ev) -> {
			Status status = timeline.getStatus();
			if (status == Status.RUNNING) {
				timeline.stop();
			} else {
				timeline.play();
			}
		}));
		vBox.getChildren().add(newCheck("Show Weight", Graph.SHOW_WEIGHT));

	}

	private String[] getWords() {
		String string = "alice.txt";
		try {
			return Files.lines(Paths.get(string)).flatMap(e -> Stream.of(e.split("[^a-zA-Z]"))).filter(s -> s.length() == 4).map(String::toLowerCase)
					.distinct().toArray(String[]::new);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String[] words = { "fine", "line", "mine", "nine", "pine", "vine", "wine", "wide", "wife", "wipe", "wire", "wind", "wing", "wink", "wins",
				"none", "gone", "note", "vote", "site", "nite", "bite" };
		return words;
	}

	private Button newButton(String nome, EventHandler<ActionEvent> onAction) {
		Button button = new Button(nome);
		button.setOnAction(onAction);
		return button;
	}

	private <T> ChoiceBox<T> newSelect(ObservableList<T> nome, StringConverter<T> converter, String string) {
		ChoiceBox<T> choiceBox = new ChoiceBox<>(nome);
		Tooltip arg0 = new Tooltip(string);
		choiceBox.setTooltip(arg0);
		choiceBox.setConverter(converter);
		return choiceBox;
	}

	private CheckBox newCheck(String name, BooleanProperty property) {
		CheckBox checkBox = new CheckBox(name);
		checkBox.setSelected(property.get());
		property.bind(checkBox.selectedProperty());
		return checkBox;
	}

	private void addGraphComponents() {

		Model model = graph.getModel();

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
			if (word1.charAt(i) != word2.charAt(i)) {
				if (++diffs > 1) {
					return false;
				}
			}
		}

		return diffs == 1;
	}

	public static void main(String[] args) {
		launch(args);
	}
}

class SimpleConverter<T> extends StringConverter<T> {
	Map<String, T> mapaLayout = new HashMap<>();
	private Function<T, String> func;

	public SimpleConverter(Function<T, String> func) {
		this.func = func;
	}

	@Override
	public String toString(T lay) {
		String simpleName = func.apply(lay);
		mapaLayout.put(simpleName, lay);
		return simpleName;
	}

	@Override
	public T fromString(String arg0) {
		return mapaLayout.get(arg0);
	}
}
