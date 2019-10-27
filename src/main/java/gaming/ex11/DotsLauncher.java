package gaming.ex11;

import static gaming.ex11.DotsHelper.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;
import utils.CommonsFX;

public class DotsLauncher extends Application {
    @FXML
    private Text textEu;
    @FXML
    private Group gridPane;
    @FXML
    private Text textTu;
    @FXML
    private Line line;
    private final ObservableMap<String, ObservableSet<Set<DotsSquare>>> points = FXCollections.observableHashMap();
    private Color[] colors = { Color.RED, Color.BLUE };
    private int currentPlayer = 1;
    private String[] jogadores = { "EU", "TU" };
    private DotsSquare selected;
    private DotsSquare[][] maze;

    @SuppressWarnings("unchecked")
    public void initialize() {
        maze = gridPane.getChildren().stream().filter(e -> e instanceof DotsSquare).map(e -> (DotsSquare) e)
            .collect(Collectors.groupingBy(DotsSquare::getI)).values().stream()
            .map(e -> e.toArray(new DotsSquare[0])).toArray(DotsSquare[][]::new);
        points.put("EU", FXCollections.observableSet());
        points.put("TU", FXCollections.observableSet());
        DotsHelper.bindText("EU", textEu, points);
        DotsHelper.bindText("TU", textTu, points);

    }

    public void onMouseDraggedGroup0(MouseEvent e) {
        if (e.getTarget() instanceof DotsSquare) {
            line.setEndX(e.getX());
            line.setEndY(e.getY());
        }
    }

    public void onMousePressedGroup0(MouseEvent e) {
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

    public void onMouseReleasedGroup0(MouseEvent e) {
        DotsSquare over = Stream.of(maze).flatMap(Stream::of)
            .filter(m -> m.getBoundsInParent().contains(e.getX(), e.getY())).findFirst().orElse(null);
        if (selected == null || over == null || selected.equals(over)) {
            return;
        }
        if (isPointNeighborToCurrent(over, selected)) {
            Line line1 = new Line(selected.getCenter()[0], selected.getCenter()[1], over.getCenter()[0],
                over.getCenter()[1]);
            gridPane.getChildren().add(line1);
            over.addAdj(selected);
            Set<Set<DotsSquare>> check = over.check();
            Set<Set<DotsSquare>> allSquares = points.values().stream().flatMap(ObservableSet<Set<DotsSquare>>::stream)
                .collect(Collectors.toSet());
            List<Set<DotsSquare>> squaresFilled = check.stream().filter(s -> !allSquares.contains(s))
                .collect(Collectors.toList());
            processNextTurn(squaresFilled);
        }
        line.setEndX(0);
        line.setStartX(0);
        line.setEndY(0);
        line.setStartY(0);
        selected = null;
        verifyEnd();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        CommonsFX.loadFXML("Dots", "DotsLauncher.fxml", this, primaryStage,
            DotsHelper.MAZE_SIZE * DotsSquare.SQUARE_SIZE + 20.,
            (DotsHelper.MAZE_SIZE + 1) * DotsSquare.SQUARE_SIZE + 20.);
    }

    private void changeTurn() {
        currentPlayer = (currentPlayer + 1) % jogadores.length;
    }

    private void processNextTurn(List<Set<DotsSquare>> squaresFilled) {
        if (!squaresFilled.isEmpty()) {
            points.get(jogadores[currentPlayer]).addAll(squaresFilled);
            DotsHelper.addBlueSquare(squaresFilled, gridPane, colors[currentPlayer]);
            return;
        }
        changeTurn();
        int nplayed = 0;
        while (currentPlayer == 0) {
            List<Map.Entry<DotsSquare, DotsSquare>> possibilities = getBestPossibilities(maze);
            possibilities = notEmpty(possibilities, getBestPossibilities2(maze));
            possibilities = notEmpty(possibilities, getBestPossibilities3(maze));
            possibilities = notEmpty(possibilities, getPossibilities(maze));
            if (possibilities.isEmpty()) {
                changeTurn();
                break;
            }
            Map.Entry<DotsSquare, DotsSquare> get = possibilities.remove(0);
            double[] center = get.getKey().getCenter();
            double[] center2 = get.getValue().getCenter();
            Line line2 = new Line(center[0], center[1], center[0], center[1]);
            gridPane.getChildren().add(line2);

            get.getKey().addAdj(get.getValue());

            Set<Set<DotsSquare>> anySquares = get.getKey().check();
            Set<Set<DotsSquare>> collect2 = points.values().stream().flatMap(ObservableSet<Set<DotsSquare>>::stream)
                .collect(Collectors.toSet());
            List<Set<DotsSquare>> squaresClosed = anySquares.stream().filter(s -> !collect2.contains(s))
                .collect(Collectors.toList());

            Timeline timeline = DotsHelper.createAnimation(nplayed, center, center2, line2);

            nplayed++;
            timeline.play();
            // RED PLAYER COULDN'T CLOSE A SQUARE
            if (squaresClosed.isEmpty()) {
                changeTurn();
                return;// Change turn
            }
            points.get(jogadores[currentPlayer]).addAll(squaresClosed);
            for (Set<DotsSquare> q : squaresClosed) {
                double[] toArray = q.stream().flatMapToDouble((DotsSquare a) -> DoubleStream.of(a.getCenter()))
                    .toArray();
                EventHandler<ActionEvent> onFinished = timeline.getOnFinished();
                Color value = colors[currentPlayer];
                timeline.setOnFinished(f -> {
                    DotsHelper.addPolygon(gridPane, toArray, onFinished, value, f);
                    verifyEnd();
                });
            }
        }
    }

    private void verifyEnd() {
        if (DotsHelper.isCountPolygonOver(gridPane)) {
            int size2 = points.get("EU").size();
            int size3 = points.get("TU").size();
            new SimpleDialogBuilder().text(size3 > size2 ? "You Won" : "You Lose").button("Reset", () -> {
                gridPane.getChildren().clear();
                gridPane.getChildren().add(line);
                DotsHelper.initializeMaze(gridPane, maze);
                initialize();
            }).bindWindow(gridPane).displayDialog();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
