/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex11;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
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

    private Color[] colors = { Color.RED, Color.BLUE };
	private int currentPlayer = 1;
	private Group gridPane;
	private String[] jogadores = { "EU", "TU" };
	private final Line line = new Line(0, 0, 0, 0);
	private DotsSquare[][] maze = new DotsSquare[MAZE_SIZE][MAZE_SIZE];



	private final ObservableMap<String, ObservableSet<Set<DotsSquare>>> points = FXCollections.observableHashMap();

	private Random random = new Random();
	private DotsSquare selected;

	@SuppressWarnings("unchecked")
	public DotsModel(Group gridPane, BorderPane borderPane) {
        this.gridPane = gridPane;
        points.put("EU", FXCollections.observableSet());
        points.put("TU", FXCollections.observableSet());
		initializeMaze(gridPane);
		gridPane.setOnMousePressed(this::handleMousePressed);
		gridPane.setOnMouseDragged(this::handleMouseDragged);
		gridPane.setOnMouseReleased(this::handleMouseReleased);
        final Text text = new Text("EU:");
        final Text text2 = new Text("0");
		text2.textProperty()
				.bind(Bindings.createStringBinding(() -> Integer.toString(points.get("EU").size()), points.get("EU")));
        gridPane.getChildren().addAll(text, text2);
        final Text tuText = new Text("TU:");
        final Text tuPoints = new Text("0");
		tuPoints.textProperty()
				.bind(Bindings.createStringBinding(() -> Integer.toString(points.get("TU").size()), points.get("TU")));
        borderPane.setTop(new HBox(text, text2, tuText, tuPoints));
    }
	private void addPolygonOnFinished(final Polygon polygon, final EventHandler<ActionEvent> onFinished,
			ActionEvent f) {
		if (onFinished != null) {
			onFinished.handle(f);
		}
		gridPane.getChildren().add(polygon);
	}

	private int getCountMap(DotsSquare a, DotsSquare b) {
        a.addAdj(b);
        int sum = 0;
        int i = Integer.min(a.getI(), b.getI());
        int j = Integer.min(a.getJ(), b.getJ());
        if (a.getI() == b.getI()) {
            if (i > 0) {
                DotsSquare c = maze[i - 1][j];
                DotsSquare d = maze[i - 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d);
            }
            if (i < MAZE_SIZE - 1) {
                DotsSquare c = maze[i + 1][j];
                DotsSquare d = maze[i + 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d);
            }
        } else if (a.getJ() == b.getJ()) {
            if (j > 0) {
                DotsSquare c = maze[i][j - 1];
                DotsSquare d = maze[i + 1][j - 1];
                sum = getSumBySquare(a, b, sum, c, d);
            }
            if (j < MAZE_SIZE - 1) {
                DotsSquare c = maze[i][j + 1];
                DotsSquare d = maze[i + 1][j + 1];
                sum = getSumBySquare(a, b, sum, c, d);
            }

        }

        a.removeAdj(b);

        return sum;
    }
    private int getSumBySquare(DotsSquare a, DotsSquare b, int sum, DotsSquare c, DotsSquare d) {
        if (cNotContainsA(a, b, c, d)) {
            sum++;
            sum += getCountMap(a, c);
        }
        if (bNotContainsD(a, b, c, d)) {
            sum++;
            sum += getCountMap(b, d);
        }
        if (dNotContainsC(a, b, c, d)) {
            sum++;
            sum += getCountMap(d, c);
        }
        return sum;
    }

    private boolean cNotContainsA(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && b.contains(d) && d.contains(c) && !c.contains(a);
    }

    private boolean bNotContainsD(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && !b.contains(d) && d.contains(c) && c.contains(a);
    }

    private boolean dNotContainsC(DotsSquare a, DotsSquare b, DotsSquare c, DotsSquare d) {
        return a.contains(b) && b.contains(d) && !d.contains(c) && c.contains(a);
    }
	public Line getLine() {
		return line;
	}

	private List<Map.Entry<DotsSquare, DotsSquare>> getMelhorPossibilidades() {
        List<Map.Entry<DotsSquare, DotsSquare>> melhor = new ArrayList<>();
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                final List<DotsSquare> checkMelhor = maze[i][j].checkMelhor();
                final DotsSquare maze1 = maze[i][j];
                final List<Map.Entry<DotsSquare, DotsSquare>> collect = checkMelhor.stream().map(e -> new AbstractMap.SimpleEntry<>(maze1, e)).collect(Collectors.toList());
                melhor.addAll(collect);
            }
        }
        return melhor;
    }

    private List<Map.Entry<DotsSquare, DotsSquare>> getMelhorPossibilidades2() {
        final List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = getPossibilidades();

        return possibilidades.stream().filter((Map.Entry<DotsSquare, DotsSquare> entry) -> {
            final boolean checkMelhor = entry.getKey().checkMelhor(entry.getValue());
            if (!checkMelhor) {
                return false;
            }

            final boolean checkMelhor1 = entry.getValue().checkMelhor(entry.getKey());
            if (!checkMelhor1) {
                return false;
            }
            entry.getKey().addAdj(entry.getValue());
            final boolean criou = Stream.of(maze).flatMap(Stream::of).flatMap(DotsSquare::almostSquare).count() > 0;
            entry.getKey().removeAdj(entry.getValue());
			return !criou;
        }).collect(Collectors.toList());
    }

    private List<Map.Entry<DotsSquare, DotsSquare>> getMelhorPossibilidades3() {
        final List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = getPossibilidades();
        final Map<Integer, List<Map.Entry<DotsSquare, DotsSquare>>> collect = possibilidades.stream().collect(Collectors.groupingBy(e -> getCountMap(e.getKey(), e.getValue())));
		final int bestPossibility = collect.keySet().stream().mapToInt(i -> i).min().orElse(0);
		return collect.getOrDefault(bestPossibility, Collections.emptyList());
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

    private void handleMouseDragged(MouseEvent e) {
		final EventTarget target = e.getTarget();
		if (target instanceof DotsSquare) {
			line.setEndX(e.getX());
			line.setEndY(e.getY());
		}
	}

	private void handleMousePressed(MouseEvent e) {
		if (e.getTarget() instanceof DotsSquare) {
			DotsSquare a = (DotsSquare) e.getTarget();
			line.setStartY(a.getLayoutY() + a.getHeight() / 2);
			line.setStartX(a.getLayoutX() + a.getWidth() / 2);
			line.setEndX(e.getX());
			line.setEndY(e.getY());
			selected = a;
		}
	}

	private void handleMouseReleased(MouseEvent e) {
		DotsSquare over = Stream.of(maze).flatMap(Stream::of)
				.filter(m -> m.getBoundsInParent().contains(e.getX(), e.getY()))
				.findFirst().orElse(null);
        if (squareOverNotSuitable(over)) {
			return;
		}

        if (isPointNeighborToCurrent(over)) {
			final Line line1 = new Line(selected.getCenter()[0], selected.getCenter()[1], over.getCenter()[0],
					over.getCenter()[1]);
			gridPane.getChildren().add(line1);
			over.addAdj(selected);
			Set<Set<DotsSquare>> check = over.check();
			Set<Set<DotsSquare>> collect = points.values().stream().flatMap(ObservableSet<Set<DotsSquare>>::stream)
					.collect(Collectors.toSet());
			List<Set<DotsSquare>> collect1 = check.stream().filter(s -> !collect.contains(s))
					.collect(Collectors.toList());
			if (!collect1.isEmpty()) {
				points.get(jogadores[currentPlayer]).addAll(collect1);
				for (Set<DotsSquare> collect2 : collect1) {
					final double[] toArray = collect2.stream().flatMap((DotsSquare a) -> Stream.of(a.getCenter()))
							.mapToDouble(Double::valueOf).toArray();
					final Polygon polygon = new Polygon(toArray);
					polygon.setFill(colors[currentPlayer]);
					gridPane.getChildren().add(polygon);
				}
			} else {
				currentPlayer = (currentPlayer + 1) % jogadores.length;
				int nplayed = 0;
				while (currentPlayer == 0) {
					List<Map.Entry<DotsSquare, DotsSquare>> possibilidades = getMelhorPossibilidades();
					possibilidades = notEmpty(possibilidades, getMelhorPossibilidades2());
                    possibilidades = notEmpty(possibilidades, getMelhorPossibilidades3());
                    possibilidades = notEmpty(possibilidades, getPossibilidades());
					if (possibilidades.isEmpty()) {
						currentPlayer = (currentPlayer + 1) % jogadores.length;
						break;
					}
					final Map.Entry<DotsSquare, DotsSquare> get = possibilidades
							.get(random.nextInt(possibilidades.size()));
					final Double[] center = get.getKey().getCenter();
					final Double[] center2 = get.getValue().getCenter();
					final Line line2 = new Line(center[0], center[1], center[0], center[1]);
					gridPane.getChildren().add(line2);

					get.getKey().addAdj(get.getValue());

					Set<Set<DotsSquare>> check2 = get.getKey().check();
					final Set<Set<DotsSquare>> collect2 = points.values().stream()
							.flatMap(ObservableSet<Set<DotsSquare>>::stream)
							.collect(Collectors.toSet());
					final List<Set<DotsSquare>> collect3 = check2.stream().filter(s -> !collect2.contains(s))
							.collect(Collectors.toList());

					final KeyFrame keyFrame = new KeyFrame(Duration.seconds(nplayed * 0.5),
							new KeyValue(line2.endXProperty(), center[0]));
					final KeyFrame keyFrame1 = new KeyFrame(Duration.seconds(0.5 + nplayed * 0.5),
							new KeyValue(line2.endXProperty(), center2[0]));

					final Timeline timeline = new Timeline(keyFrame, keyFrame1,
							new KeyFrame(Duration.seconds(nplayed * 0.5),
									new KeyValue(line2.endYProperty(), center[1])),
							new KeyFrame(Duration.seconds(0.5 + nplayed * 0.5),
									new KeyValue(line2.endYProperty(), center2[1])));
					nplayed++;
					timeline.play();
					if (!collect3.isEmpty()) {
						points.get(jogadores[currentPlayer]).addAll(collect3);
						collect3.forEach((Set<DotsSquare> q) -> {
							final double[] toArray = q.stream().flatMap((DotsSquare a) -> Stream.of(a.getCenter()))
									.mapToDouble(Double::valueOf).toArray();
							final Polygon polygon = new Polygon(toArray);
							polygon.setFill(colors[currentPlayer]);
							final EventHandler<ActionEvent> onFinished = timeline.getOnFinished();
							timeline.setOnFinished(f -> addPolygonOnFinished(polygon, onFinished, f));
						});
					} else {
						currentPlayer = (currentPlayer + 1) % jogadores.length;
					}
				}

			}

		}
		line.setEndX(0);
		line.setStartX(0);
		line.setEndY(0);
		line.setStartY(0);
		selected = null;
	}

    private boolean squareOverNotSuitable(DotsSquare over) {
        return selected == null || over == null || selected == over;
    }
    private List<Entry<DotsSquare, DotsSquare>> notEmpty(List<Map.Entry<DotsSquare, DotsSquare>> possibilidades,
            List<Entry<DotsSquare, DotsSquare>> melhorPossibilidades2) {
        return possibilidades.isEmpty() ? melhorPossibilidades2 : possibilidades;
    }

    private boolean isPointNeighborToCurrent(DotsSquare over) {
        return Math.abs(over.getI() - selected.getI()) + Math.abs(over.getJ() - selected.getJ()) == 1
				&& !over.contains(selected);
    }

	private void initializeMaze(Group gridPane1) {
		for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                maze[i][j] = new DotsSquare(i, j);
				gridPane1.getChildren().add(maze[i][j]);
            }
        }
	}


}
