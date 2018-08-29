package gaming.ex01;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SnakeLauncher extends Application {
    public static final int UPDATE_MILLIS = 200;

    private final SnakeModel newGameModel = new SnakeModel();

    private int currentI;

    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        for (int i = 0; i < SnakeModel.MAP_SIZE; i++) {
            for (int j = 0; j < SnakeModel.MAP_SIZE; j++) {
                gridPane.add(newGameModel.getMap()[i][j], i, j);
            }
        }

        final Scene scene = new Scene(gridPane);
		scene.setOnKeyPressed(this::handleKeyPressed);
        final Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(new Duration(UPDATE_MILLIS), t -> gameLoop(timeline)));
		timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(400);
        stage.show();
    }

    private void gameLoop(final Timeline timeline) {
        KeyFrame remove = timeline.getKeyFrames().get(0);
        if (remove.getTime().greaterThan(new Duration(50))) {
            currentI++;
            if (currentI % 50 == 0) {
                timeline.stop();
                timeline.getKeyFrames().clear();
                Duration add = remove.getTime().add(new Duration(-2));
                timeline.getKeyFrames().add(new KeyFrame(add, f -> gameLoop(timeline)));
                timeline.play();
            }
        }
	    if (newGameModel.updateMap()) {
		    timeline.stop();
		    final Button button = new Button("Reset");
		    final Stage stage1 = new Stage();
		    button.setOnAction(a -> {
		        newGameModel.reset();
		        timeline.play();
		        stage1.close();
		    });
			final Group group = new Group(new Text("You Got " + newGameModel.getSnake().size() + " points"), button);
		    group.setLayoutX(50);
		    group.setLayoutY(50);
		    stage1.setScene(new Scene(group));
		    stage1.show();
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
