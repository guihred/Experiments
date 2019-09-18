package gaming.ex04;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;
import utils.StageHelper;

public class TronLauncher extends Application {
    private static final int UPDATE_MILLIS = 40;
    private static final int WIDTH = 400;
    private final TronModel newGameModel = new TronModel();
    private Timeline timeline;

    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        for (int i = 0; i < TronSquare.MAP_SIZE; i++) {
            for (int j = 0; j < TronSquare.MAP_SIZE; j++) {
                gridPane.add(newGameModel.getMap()[i][j], i, j);
            }
        }

        final Scene scene = new Scene(gridPane);
        scene.setOnKeyPressed(this::handleKeyPressed);
        timeline = new SimpleTimelineBuilder().addKeyFrame(Duration.millis(UPDATE_MILLIS), t -> {
            if (newGameModel.updateMap()) {
                timeline.stop();
                String text2 = "You Got " + newGameModel.getSnake().size() + " points";
                final String text = text2;
				StageHelper.displayDialog(text, "Reset", () -> {
                	newGameModel.reset();
                	timeline.play();
                });
            }
        }).cycleCount(Animation.INDEFINITE).build();
        timeline.play();
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(WIDTH);
        stage.show();
    }

    private void handleKeyPressed(KeyEvent e) {
        final KeyCode code = e.getCode();
        switch (code) {
            case UP:
            case W:
                if (newGameModel.getDirection() != TronDirection.DOWN) {
                    newGameModel.setDirection(TronDirection.UP);
                }
                break;
            case LEFT:
            case A:
                if (newGameModel.getDirection() != TronDirection.RIGHT) {
                    newGameModel.setDirection(TronDirection.LEFT);
                }
                break;
            case RIGHT:
            case S:
                if (newGameModel.getDirection() != TronDirection.LEFT) {
                    newGameModel.setDirection(TronDirection.RIGHT);
                }
                break;
            case DOWN:
            case D:
                if (newGameModel.getDirection() != TronDirection.UP) {
                    newGameModel.setDirection(TronDirection.DOWN);
                }
                break;
            default:
        }
        newGameModel.updateMap();
    }

    public static void main(String[] args) {
        launch(TronLauncher.class, args);
    }
}
