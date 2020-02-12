package gaming.ex04;

import static utils.CommonsFX.onCloseWindow;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleTimelineBuilder;

public class TronLauncher extends Application {
    private static final int UPDATE_MILLIS = 40;
    private static final int WIDTH = 400;
    private final TronModel newGameModel = new TronModel();
    private Timeline timeline;

    @Override
	public void start(Stage stage) {
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
                new SimpleDialogBuilder().text(text2).button("Reset", () -> {
                    newGameModel.reset();
                    timeline.play();
                }).bindWindow(stage).displayDialog();
            }
        }).cycleCount(Animation.INDEFINITE).build();
        timeline.play();
        stage.setScene(scene);
        stage.setWidth(WIDTH);
        stage.setHeight(WIDTH);
        onCloseWindow(stage, () -> timeline.stop());
        stage.show();
    }

    private void handleKeyPressed(KeyEvent e) {
        TronDirection byKeyCode = TronDirection.getByKeyCode(e.getCode());
		if (notOppositeDirections(byKeyCode)) {
            newGameModel.setDirection(byKeyCode);
        }
        newGameModel.updateMap();
    }

	private boolean notOppositeDirections(TronDirection byKeyCode) {
		return byKeyCode != null && newGameModel.getDirection().ordinal() != (byKeyCode.ordinal() + 2) % 4;
	}

    public static void main(String[] args) {
        launch(TronLauncher.class, args);
    }
}
