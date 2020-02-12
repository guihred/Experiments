package gaming.ex05;

import static utils.CommonsFX.onCloseWindow;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

public class TetrisLauncher extends Application {
	private final GridPane gridPane = new GridPane();
	private final TetrisModel tetrisModel = new TetrisModel(gridPane);
    private Timeline timeline;

	@Override
	public void start(Stage stage) {
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        final Scene scene = new Scene(gridPane);
        timeline = new SimpleTimelineBuilder()
                .addKeyFrame(new Duration(500), t -> tetrisModel.movePiecesTimeline(timeline))
                .cycleCount(Animation.INDEFINITE).build();
        timeline.play();
		scene.setOnKeyPressed(this::handleKeyPressed);
        stage.setScene(scene);
        onCloseWindow(stage, () -> timeline.stop());
        stage.show();
    }

    private void handleKeyPressed(KeyEvent e) {
		final KeyCode code = e.getCode();
		switch (code) {
            case UP:
            case W:
                tetrisModel.changeDirection();
                break;
            case LEFT:
            case A:
                moveSideways(-1);
                break;
            case RIGHT:
            case D:
                moveSideways(1);
                break;
            case DOWN:
            case S:
                moveDown();
                break;
            default:
		}
	}

    private void moveDown() {
        if (!tetrisModel.checkCollision(tetrisModel.getCurrentI(), tetrisModel.getCurrentJ() + 1)) {
            tetrisModel.setCurrentJ(tetrisModel.getCurrentJ() + 1);
            tetrisModel.clearMovingPiece();
            tetrisModel.drawPiece();
        }
    }
    private void moveSideways(int i) {
        if (!tetrisModel.checkCollision(tetrisModel.getCurrentI() + i, tetrisModel.getCurrentJ())) {
            tetrisModel.setCurrentI(tetrisModel.getCurrentI() + i);
            tetrisModel.clearMovingPiece();
            tetrisModel.drawPiece();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
