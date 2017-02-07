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

    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        final SnakeModel newGameModel = new SnakeModel();
        for (int i = 0; i < SnakeModel.MAP_SIZE; i++) {
            for (int j = 0; j < SnakeModel.MAP_SIZE; j++) {
                gridPane.add(newGameModel.getMap()[i][j], i, j);
            }
        }

        final Scene scene = new Scene(gridPane);

        scene.setOnKeyPressed((KeyEvent e) -> {
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
        });
        final Timeline timeline = new Timeline();

        timeline.getKeyFrames().add(
                new KeyFrame(
                        new Duration(200), (javafx.event.ActionEvent t) -> {
                            if (newGameModel.updateMap()) {
                                timeline.stop();
                                final Text text = new Text("You Got " + newGameModel.getSnake().size() + " points");
                                final Button button = new Button("Reset");
                                final Stage stage1 = new Stage();
                                button.setOnAction(a -> {
                                    newGameModel.reset();
                                    timeline.play();
                                    stage1.close();
                                });


                                final Group group = new Group(text, button);
                                group.setLayoutX(50);
                                group.setLayoutY(50);
                                stage1.setScene(new Scene(group));
                                stage1.show();
                            }
                        })
        );
		timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch(SnakeLauncher.class, args);
    }
}
