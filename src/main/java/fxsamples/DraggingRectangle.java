package fxsamples;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import utils.RotateUtils;

public class DraggingRectangle extends Application {

	private static final double SIZE = 800;

    @Override
    public void start(final Stage primaryStage) {

        StackPane root = new StackPane();
        Rectangle rect = new Rectangle(0, 0, SIZE / 2, SIZE / 2);
        rect.setManaged(false);
        rect.setLayoutX(SIZE / 4);
        rect.setLayoutY(SIZE / 4);
        root.getChildren().add(rect);
        RotateUtils.createDraggableRectangle(rect);
        rect.setFill(Color.NAVY);

        Scene scene = new Scene(root, SIZE, SIZE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(final String[] args) {
        Application.launch(args);
    }


}