package fxsamples;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/** Example of dragging anchors around to manipulate a line. */
public class LineManipulator extends Application {
	@Override
	public void start(final Stage stage) {
		DoubleProperty startX = new SimpleDoubleProperty(100);
		DoubleProperty startY = new SimpleDoubleProperty(100);
        int finalValue = 500;
        DoubleProperty endX = new SimpleDoubleProperty(finalValue - 100);
        DoubleProperty endY = new SimpleDoubleProperty(finalValue - 100);

		AnchorCircle start = new AnchorCircle(Color.PALEGREEN, startX, startY);
		AnchorCircle end = new AnchorCircle(Color.TOMATO, endX, endY);

		Line line = new BoundLine(startX, startY, endX, endY);
		stage.setTitle("Line Manipulation Sample");
        stage.setScene(new Scene(new Group(line, start, end), finalValue, finalValue, Color.ALICEBLUE));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

