package gaming.ex01;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleTimelineBuilder;

public class SnakeLauncher extends Application {
    public static final int UPDATE_MILLIS = 200;

    private final SnakeModel newGameModel = new SnakeModel();

    private int currentI;

    private Timeline timeline;

    private Stage primaryStage;

    public ObservableList<SnakeSquare> getSnake() {
        return newGameModel.getSnake();

    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        final GridPane gridPane = new GridPane();
        for (int i = 0; i < SnakeSquare.MAP_SIZE; i++) {
            for (int j = 0; j < SnakeSquare.MAP_SIZE; j++) {
                gridPane.add(newGameModel.getMap()[i][j], i, j);
            }
        }

        final Scene scene = new Scene(gridPane);
        scene.setOnKeyPressed(this::handleKeyPressed);

        timeline = new SimpleTimelineBuilder().addKeyFrame(new Duration(UPDATE_MILLIS), t -> gameLoop())
            .cycleCount(Animation.INDEFINITE).build();
        timeline.play();
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> timeline.stop());
        stage.show();
    }

    private void gameLoop() {
        KeyFrame remove = timeline.getKeyFrames().get(0);
        if (remove.getTime().greaterThan(new Duration(50))) {
            currentI++;
            if (currentI % 50 == 0) {
                timeline.stop();
                timeline.getKeyFrames().clear();
                Duration add = remove.getTime().add(new Duration(-2));
                timeline.getKeyFrames().add(new KeyFrame(add, f -> gameLoop()));
                timeline.play();
            }
        }
        if (newGameModel.updateMap()) {
            timeline.stop();
            new SimpleDialogBuilder().text("You Got " + newGameModel.getSnake().size() + " points")
                .button("Reset", () -> {
                    newGameModel.reset();
                    timeline.play();
                }).bindWindow(primaryStage).displayDialog();
        }
    }

    private void handleKeyPressed(KeyEvent e) {
        SnakeDirection byKeyCode = SnakeDirection.getByKeyCode(e.getCode());
        if (SnakeDirection.isNotOpposite(byKeyCode, newGameModel.getDirection())) {
            newGameModel.setDirection(byKeyCode);
        }
        newGameModel.updateMap();
    }

    public static void main(String[] args) {
        launch(SnakeLauncher.class, args);
    }
}
