package gaming.ex20;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RoundMazeLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        final GridPane gridPane = new GridPane();
        Canvas canvas = new Canvas(600, 600);
        gridPane.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        canvas.setTranslateX(10);
        final Scene scene = new Scene(new Group(canvas));
		RoundMazeModel.create(scene, canvas).draw();
        stage.setScene(scene);
        stage.setWidth(canvas.getWidth() + 50);
        stage.setHeight(canvas.getHeight() + 50);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
