/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex11;

import static gaming.ex11.DotsHelper.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;
import utils.StageHelper;

/**
 *
 * @author Note
 */
public class DotsModel {

    public static final int MAZE_SIZE = 6;

    private Color[] colors = { Color.RED, Color.BLUE };
    private int currentPlayer = 1;
    private Group gridPane = new Group();
    private String[] jogadores = { "EU", "TU" };
    private final Line line = new Line(0, 0, 0, 0);
    private DotsSquare[][] maze = new DotsSquare[MAZE_SIZE][MAZE_SIZE];
    private final ObservableMap<String, ObservableSet<Set<DotsSquare>>> points = FXCollections.observableHashMap();
    private DotsSquare selected;

    public DotsModel(BorderPane borderPane) {
        borderPane.setCenter(gridPane);
        initialize(borderPane);
    }

    public Line getLine() {
        return line;
    }

    private void addPolygonOnFinished(Polygon polygon, EventHandler<ActionEvent> onFinished, ActionEvent f) {
        if (onFinished != null) {
            onFinished.handle(f);
        }
        gridPane.getChildren().add(polygon);
        verifyEnd();

    }

    private void handleMouseDragged(MouseEvent e) {
        if (e.getTarget() instanceof DotsSquare) {
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
            return;
        }
        Object b = e.getTarget();
        if (b instanceof Circle && ((Circle) b).getParent() instanceof DotsSquare) {
            Circle a = (Circle) b;
            selected = (DotsSquare) a.getParent();
            line.setStartY(selected.getLayoutY() + selected.getHeight() / 2);
            line.setStartX(selected.getLayoutX() + selected.getWidth() / 2);
            line.setEndX(e.getX());
            line.setEndY(e.getY());
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        DotsSquare over = Stream.of(maze).flatMap(Stream::of)
            .filter(m -> m.getBoundsInParent().contains(e.getX(), e.getY())).findFirst().orElse(null);
        if (squareOverNotSuitable(over)) {
            return;
        }

        if (isPointNeighborToCurrent(over, selected)) {
            Line line1 = new Line(selected.getCenter()[0], selected.getCenter()[1], over.getCenter()[0],
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
                    double[] toArray = collect2.stream().flatMap(a -> Stream.of(a.getCenter()))
                        .mapToDouble(Double::valueOf).toArray();
                    Polygon polygon = new Polygon(toArray);
                    polygon.setFill(colors[currentPlayer]);
                    gridPane.getChildren().add(polygon);
                }
            } else {
                currentPlayer = (currentPlayer + 1) % jogadores.length;
                int nplayed = 0;
                while (currentPlayer == 0) {
                    List<Map.Entry<DotsSquare, DotsSquare>> possibilities = getBestPossibilities(maze);
                    possibilities = notEmpty(possibilities, getBestPossibilities2(maze));
                    possibilities = notEmpty(possibilities, getBestPossibilities3(maze));
                    possibilities = notEmpty(possibilities, getPossibilities(maze));
                    if (possibilities.isEmpty()) {
                        currentPlayer = (currentPlayer + 1) % jogadores.length;
                        break;
                    }
                    Map.Entry<DotsSquare, DotsSquare> get = possibilities.remove(0);
                    Double[] center = get.getKey().getCenter();
                    Double[] center2 = get.getValue().getCenter();
                    Line line2 = new Line(center[0], center[1], center[0], center[1]);
                    gridPane.getChildren().add(line2);

                    get.getKey().addAdj(get.getValue());

                    Set<Set<DotsSquare>> check2 = get.getKey().check();
                    Set<Set<DotsSquare>> collect2 = points.values().stream()
                        .flatMap(ObservableSet<Set<DotsSquare>>::stream).collect(Collectors.toSet());
                    List<Set<DotsSquare>> collect3 = check2.stream().filter(s -> !collect2.contains(s))
                        .collect(Collectors.toList());

                    Timeline timeline = new SimpleTimelineBuilder()
                        .addKeyFrame(Duration.seconds(nplayed * 0.5), line2.endXProperty(), center[0])
                        .addKeyFrame(Duration.seconds(0.5 + nplayed * 0.5), line2.endXProperty(), center2[0])
                        .addKeyFrame(Duration.seconds(nplayed * 0.5), line2.endYProperty(), center[1])
                        .addKeyFrame(Duration.seconds(0.5 + nplayed * 0.5), line2.endYProperty(), center2[1]).build();

                    nplayed++;
                    timeline.play();
                    if (!collect3.isEmpty()) {
                        points.get(jogadores[currentPlayer]).addAll(collect3);
                        collect3.forEach((Set<DotsSquare> q) -> {
                            double[] toArray = q.stream().flatMap((DotsSquare a) -> Stream.of(a.getCenter()))
                                .mapToDouble(Double::valueOf).toArray();
                            Polygon polygon = new Polygon(toArray);
                            polygon.setFill(colors[currentPlayer]);
                            EventHandler<ActionEvent> onFinished = timeline.getOnFinished();
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
        verifyEnd();
    }

    @SuppressWarnings("unchecked")
    private void initialize(BorderPane borderPane) {
        points.put("EU", FXCollections.observableSet());
        points.put("TU", FXCollections.observableSet());
        gridPane.getChildren().add(getLine());
        DotsHelper.initializeMaze(gridPane, maze);
        gridPane.setOnMousePressed(this::handleMousePressed);
        gridPane.setOnMouseDragged(this::handleMouseDragged);
        gridPane.setOnMouseReleased(this::handleMouseReleased);
        Text text = new Text("EU:");
        Text text2 = DotsHelper.pointsDisplay("EU", points);
        gridPane.getChildren().addAll(text, text2);
        Text tuText = new Text("TU:");
        String key = "TU";
        Text tuPoints = DotsHelper.pointsDisplay(key, points);
        borderPane.setTop(new HBox(text, text2, tuText, tuPoints));
    }


    private boolean squareOverNotSuitable(DotsSquare over) {
        return selected == null || over == null || Objects.equals(selected, over);
    }

    private void verifyEnd() {
        int size = MAZE_SIZE - 1;
        if (gridPane.getChildren().stream().filter(e -> e instanceof Polygon).count() == size * size) {

            int size2 = points.get("EU").size();
            int size3 = points.get("TU").size();

            StageHelper.displayDialog(size3 > size2 ? "You Won" : "You Lose", "Reset", () -> {
                gridPane.getChildren().clear();
                initialize((BorderPane) gridPane.getParent());
            });

        }
    }

    public static DotsModel createModel(BorderPane borderPane) {
        return new DotsModel(borderPane);
    }

}
