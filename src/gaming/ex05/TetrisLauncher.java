package gaming.ex05;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class TetrisLauncher extends Application {
	private final GridPane gridPane = new GridPane();
	private final TetrisModel tetrisModel = new TetrisModel(gridPane);

	private void handleKeyPressed(KeyEvent e) {
		final KeyCode code = e.getCode();
		switch (code) {
		case UP:
		case W:
			tetrisModel.changeDirection();
			break;
		case LEFT:
		case A:
			if (!tetrisModel.checkCollision(tetrisModel.getCurrentI() - 1, tetrisModel.getCurrentJ())) {
				tetrisModel.setCurrentI(tetrisModel.getCurrentI() - 1);
				tetrisModel.clearMovingPiece();
				tetrisModel.drawPiece();
			}

			break;
		case RIGHT:
		case D:
			if (!tetrisModel.checkCollision(tetrisModel.getCurrentI() + 1, tetrisModel.getCurrentJ())) {
				tetrisModel.setCurrentI(tetrisModel.getCurrentI() + 1);
				tetrisModel.clearMovingPiece();
				tetrisModel.drawPiece();
			}
			break;
		case DOWN:
		case S:
			if (!tetrisModel.checkCollision(tetrisModel.getCurrentI(), tetrisModel.getCurrentJ() + 1)) {
				tetrisModel.setCurrentJ(tetrisModel.getCurrentJ() + 1);
				tetrisModel.clearMovingPiece();
				tetrisModel.drawPiece();
			}

			break;
		default:
		}
	}
    @Override
    public void start(Stage stage) throws Exception {
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        final Scene scene = new Scene(gridPane);
        final Timeline timeline = new Timeline();
        final EventHandler<ActionEvent> eventHandler = tetrisModel.getEventHandler(timeline);
        timeline.getKeyFrames().add(new KeyFrame(new Duration(500), eventHandler));
		timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
		scene.setOnKeyPressed(e -> handleKeyPressed(e));
        stage.setScene(scene);
        stage.setWidth(300);
        stage.setHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
