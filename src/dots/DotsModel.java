package dots;

import static dots.StreamHelp.*;

import java.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 *
 * @author Note
 */
public class DotsModel {

	public static final int MAZE_SIZE = 6;

	DotsSquare[][] maze = new DotsSquare[MAZE_SIZE][MAZE_SIZE];
	Group gridPane;
	Line line = new Line(0, 0, 0, 0);
	DotsSquare over;
	DotsSquare selected;
	final ObservableMap<String, ObservableSet<Set<DotsSquare>>> points = FXCollections.observableHashMap();
	int currentPlayer = 1;
	String[] jogadores = { "EU", "TU" };
	Color[] colors = { Color.RED, Color.BLUE };
	Random random = new Random();

	public DotsModel(Group gridPane, BorderPane borderPane) {
		this.gridPane = gridPane;
		points.put("EU", FXCollections.observableSet());
		points.put("TU", FXCollections.observableSet());

		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				maze[i][j] = new DotsSquare(i, j);
				gridPane.getChildren().add(maze[i][j]);
			}
		}
		gridPane.setOnMousePressed(e -> {
			final EventTarget target = e.getTarget();
			if (target instanceof DotsSquare) {
				DotsSquare a = (DotsSquare) target;
				line.setStartY(a.getLayoutY() + a.getHeight() / 2);
				line.setStartX(a.getLayoutX() + a.getWidth() / 2);
				line.setEndX(e.getX());
				line.setEndY(e.getY());
				selected = a;
			}
		});
		gridPane.setOnMouseDragged(e -> {
			final EventTarget target = e.getTarget();
			if (target instanceof DotsSquare) {
				line.setEndX(e.getX());
				line.setEndY(e.getY());
			}
		});
		gridPane.setOnMouseReleased(e -> {
			for (int i = 0; i < MAZE_SIZE; i++) {
				for (int j = 0; j < MAZE_SIZE; j++) {
					if (maze[i][j].getBoundsInParent().contains(e.getX(), e.getY())) {
						over = maze[i][j];
					}
				}
			}

			if (selected != null && over != null && selected != over && Math.abs(over.i - selected.i) + Math.abs(over.j - selected.j) == 1
					&& !over.contains(selected)) {
				final Line line1 = new Line(selected.getCenter()[0], selected.getCenter()[1], over.getCenter()[0], over.getCenter()[1]);
				gridPane.getChildren().add(line1);
				over.addAdj(selected);

				Set<Set<DotsSquare>> check = over.check();

				Set<Set<DotsSquare>> collect = toSet(flatMap(points.values(), (a) -> a));

				Set<Set<DotsSquare>> collect1 = filter(check, s -> !collect.contains(s));
				if (!collect1.isEmpty()) {
					points.get(jogadores[currentPlayer]).addAll(collect1);
					for (Set<DotsSquare> collect2 : collect1) {
						List<Double> map = map(flatMap(collect2, a -> Arrays.asList(a.getCenter())), (Double d) -> Double.valueOf(d));
						final Double[] toArray = map.toArray(new Double[map.size()]);
						final Polygon polygon = new Polygon(doubleArray(toArray));
						polygon.setFill(colors[currentPlayer]);
						gridPane.getChildren().add(polygon);
					}
				} else {
					currentPlayer = (currentPlayer + 1) % jogadores.length;
					if (currentPlayer == 0) {
						int nplayed = 0;
						while (currentPlayer == 0) {
							List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = getMelhorPossibilidades();

							possibilidades = possibilidades.isEmpty() ? getMelhorPossibilidades2() : possibilidades;
							possibilidades = possibilidades.isEmpty() ? getMelhorPossibilidades3() : possibilidades;

							possibilidades = possibilidades.isEmpty() ? getPossibilidades() : possibilidades;
							if (possibilidades.isEmpty()) {
								currentPlayer = (currentPlayer + 1) % jogadores.length;
								break;
							}
							final Map.Entry<DotsSquare, DotsSquare> get = possibilidades.get(random.nextInt(possibilidades.size()));
							final Double[] center = get.getKey().getCenter();
							final Double[] center2 = get.getValue().getCenter();
							final Line line2 = new Line(center[0], center[1], center[0], center[1]);
							gridPane.getChildren().add(line2);
							get.getKey().addAdj(get.getValue());
							Set<Set<DotsSquare>> check2 = get.getKey().check();
							final Set<Set<DotsSquare>> collect2 = toSet(flatMap(points.values(), a -> a));
							final Set<Set<DotsSquare>> collect3 = filter(check2, s -> !collect2.contains(s));
							final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(nplayed * 0.5), new KeyValue(line2.endXProperty(),
									center[0])), new KeyFrame(Duration.seconds(0.5 + nplayed * 0.5), new KeyValue(line2.endXProperty(), center2[0])),
									new KeyFrame(Duration.seconds(nplayed * 0.5), new KeyValue(line2.endYProperty(), center[1])), new KeyFrame(
											Duration.seconds(0.5 + nplayed * 0.5), new KeyValue(line2.endYProperty(), center2[1])));
							nplayed++;
							timeline.play();
							if (!collect3.isEmpty()) {
								points.get(jogadores[currentPlayer]).addAll(collect3);
								for (Set<DotsSquare> q : collect3) {

									List<Double> map = map(flatMap(q, a -> Arrays.asList(a.getCenter())), (Double d) -> Double.valueOf(d));
									final Double[] toArray = map.toArray(new Double[map.size()]);
									final Polygon polygon = new Polygon(doubleArray(toArray));
									polygon.setFill(colors[currentPlayer]);

									if (timeline.getOnFinished() == null) {
										timeline.setOnFinished(f -> gridPane.getChildren().add(polygon));
									} else {
										final EventHandler<ActionEvent> onFinished = timeline.getOnFinished();
										timeline.setOnFinished(f -> {
											onFinished.handle(f);
											gridPane.getChildren().add(polygon);
										});
									}
								}
							} else {
								currentPlayer = (currentPlayer + 1) % jogadores.length;
							}
						}

					}
				}

			}
			line.setEndX(0);
			line.setStartX(0);
			line.setEndY(0);
			line.setStartY(0);
			over = null;
			selected = null;
		});
		final Text text = new Text("EU:");
		final Text text2 = new Text("0");
		text2.textProperty().bind(new StringBinding() {
			{
				bind(points.get("EU"));
			}

			@Override
			protected String computeValue() {
				return Integer.toString(points.get("EU").size());
			}
		});
		gridPane.getChildren().addAll(text, text2);
		final Text tuText = new Text("TU:");
		final Text tuPoints = new Text("0");
		tuPoints.textProperty().bind(new StringBinding() {
			{
				bind(points.get("TU"));
			}

			@Override
			protected String computeValue() {
				return Integer.toString(points.get("TU").size());
			}
		});
		borderPane.setTop(new HBox(text, text2, tuText, tuPoints));
	}

	private List<Map.Entry<DotsSquare, DotsSquare>> getPossibilidades() {
		List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = new ArrayList<>();
		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				if (i < MAZE_SIZE - 1 && !maze[i][j].contains(maze[i + 1][j])) {
					possibilidades.add(new AbstractMap.SimpleEntry<>(maze[i][j], maze[i + 1][j]));
				}
				if (j < MAZE_SIZE - 1 && !maze[i][j].contains(maze[i][j + 1])) {
					possibilidades.add(new AbstractMap.SimpleEntry<>(maze[i][j], maze[i][j + 1]));
				}
			}
		}
		return possibilidades;
	}

	private List<Map.Entry<DotsSquare, DotsSquare>> getMelhorPossibilidades() {
		List<Map.Entry<DotsSquare, DotsSquare>> melhor = new ArrayList<>();
		for (int i = 0; i < MAZE_SIZE; i++) {
			for (int j = 0; j < MAZE_SIZE; j++) {
				final List<DotsSquare> checkMelhor = maze[i][j].checkMelhor();
				final DotsSquare maze1 = maze[i][j];
				final List<Map.Entry<DotsSquare, DotsSquare>> collect = map(checkMelhor, e -> new AbstractMap.SimpleEntry<>(maze1, e));
				melhor.addAll(collect);
			}
		}
		return melhor;
	}

	private List<Map.Entry<DotsSquare, DotsSquare>> getMelhorPossibilidades2() {
		final List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = getPossibilidades();

		final List<Map.Entry<DotsSquare, DotsSquare>> collect = filter(possibilidades, (Map.Entry<DotsSquare, DotsSquare> entry) -> {
			final boolean checkMelhor = entry.getKey().checkMelhor(entry.getValue());
			if (!checkMelhor) {
				return false;
			}

			final boolean checkMelhor1 = entry.getValue().checkMelhor(entry.getKey());
			if (!checkMelhor1) {
				return false;
			}
			entry.getKey().addAdj(entry.getValue());
			List<DotsSquare[]> asList = Arrays.asList(maze);
			List<DotsSquare> f = flatMap(asList, (ar) -> Arrays.asList(ar));
			final boolean criou = flatMap(f, DotsSquare::almostSquare).size() > 0;
			entry.getKey().removeAdj(entry.getValue());
			if (criou) {
				return false;
			}
			return true;
		});

		return collect;
	}

	private List<Map.Entry<DotsSquare, DotsSquare>> getMelhorPossibilidades3() {
		final List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = getPossibilidades();

		final Map<Integer, List<Map.Entry<DotsSquare, DotsSquare>>> collect = groupBy(possibilidades, e -> getCountMap(e.getKey(), e.getValue()));

		final int orElse = min(collect.keySet(), 0);

		final List<Map.Entry<DotsSquare, DotsSquare>> orDefault = collect.getOrDefault(orElse, Collections.emptyList());

		return orDefault;
	}

	int getCountMap(DotsSquare a1, DotsSquare b1) {
		DotsSquare a = a1.i < b1.i ? a1 : a1.j < b1.j ? a1 : b1;
		DotsSquare b = b1.i > a1.i ? b1 : b1.j > a1.j ? b1 : a1;
		if (a == b) {
			System.out.println("ERRRRRRRRRRRRRROO");
			return 0;
		}

		a.addAdj(b);
		int sum = 0;
		int i = a.i < b.i ? a.i : b.i;
		int j = a.j < b.j ? a.j : b.j;

		if (a.i == b.i) {
			if (i > 0) {
				DotsSquare c = maze[i - 1][j];
				DotsSquare d = maze[i - 1][j + 1];
				if (a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a)) {
					sum++;
					sum += getCountMap(a, c);
				}
				if (a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, b);
				}
				if (a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, c);
				}
			}
			if (i < MAZE_SIZE - 1) {
				DotsSquare c = maze[i + 1][j];
				DotsSquare d = maze[i + 1][j + 1];
				if (a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a)) {
					sum++;
					sum += getCountMap(a, c);
				}
				if (a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, b);
				}
				if (a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, c);
				}
			}
		} else if (a.j == b.j) {
			if (j > 0) {
				DotsSquare c = maze[i][j - 1];
				DotsSquare d = maze[i + 1][j - 1];
				if (a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a)) {
					sum++;
					sum += getCountMap(a, c);
				}
				if (a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, b);
				}
				if (a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, c);
				}
			}
			if (j < MAZE_SIZE - 1) {
				DotsSquare c = maze[i][j + 1];
				DotsSquare d = maze[i + 1][j + 1];
				if (a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a)) {
					sum++;
					sum += getCountMap(a, c);
				}
				if (a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, b);
				}
				if (a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a)) {
					sum++;
					sum += getCountMap(d, c);
				}
			}

		}

		a.removeAdj(b);

		return sum;
	}

}
