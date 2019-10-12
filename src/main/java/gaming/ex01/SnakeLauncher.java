package gaming.ex01;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
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

    @Override
    public void start(Stage stage) throws Exception {
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
        final KeyCode code = e.getCode();
        switch (code) {
            case UP:
            case W:
                if (newGameModel.getDirection() != SnakeDirection.DOWN) {
                    newGameModel.setDirection(SnakeDirection.UP);
                }
                break;
            case LEFT:
            case A:
                if (newGameModel.getDirection() != SnakeDirection.RIGHT) {
                    newGameModel.setDirection(SnakeDirection.LEFT);
                }
                break;
            case RIGHT:
            case S:
                if (newGameModel.getDirection() != SnakeDirection.LEFT) {
                    newGameModel.setDirection(SnakeDirection.RIGHT);
                }
                break;
            case DOWN:
            case D:
                if (newGameModel.getDirection() != SnakeDirection.UP) {
                    newGameModel.setDirection(SnakeDirection.DOWN);
                }
                break;
            default:
        }
        newGameModel.updateMap();
    }

    public static void main(String[] args) {
        launch(SnakeLauncher.class, args);
    }
}
