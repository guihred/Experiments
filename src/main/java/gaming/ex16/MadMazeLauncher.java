package gaming.ex16;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public class MadMazeLauncher extends Application {

    private static final int WIDTH = 500;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mad Maze");
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, WIDTH);
        MadTopology madTopology = new MadTopology();
        madTopology.execute(WIDTH, WIDTH);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        madTopology.drawShapes(gc);
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}