package fxsamples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;

public class DrawingLines extends Application {
	@Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Lines");
        VBox root = new VBox(10);
        root.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        StackPane stack = new StackPane(root);
        stack.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        stack.setPadding(new Insets(10));
        Scene scene = new Scene(stack);
		// Red line
        final int endX = 200;
        Line redLine = new Line(10, 10, endX, 10);
		// setting common properties
		redLine.setStroke(Color.RED);
		redLine.setStrokeWidth(10);
		redLine.setStrokeLineCap(StrokeLineCap.BUTT);
		// creating a dashed pattern
		redLine.getStrokeDashArray().addAll(10D, 5D, 15D, 5D, 20D);
		redLine.setStrokeDashOffset(0);
		root.getChildren().add(redLine);
		// White line
		Line whiteLine = new Line(10, 30, endX, 30);
		whiteLine.setStroke(Color.WHITE);
		whiteLine.setStrokeWidth(10);
		whiteLine.setStrokeLineCap(StrokeLineCap.ROUND);
		root.getChildren().add(whiteLine);
		// Blue line
		Line blueLine = new Line(10, 50, endX, 50);
		blueLine.setStroke(Color.BLUE);
		blueLine.setStrokeWidth(10);
		root.getChildren().add(blueLine);
		// slider min, max, and current value
        Slider slider = new SimpleSliderBuilder(0, 100, 0).build();
		// bind the stroke dash offset property
		redLine.strokeDashOffsetProperty().bind(slider.valueProperty());
		root.getChildren().add(slider);


		Text offsetText = new Text("Stroke Dash Offset: 0.0");
		offsetText.setStroke(Color.WHITE);

		// display stroke dash offset value
		slider.valueProperty().addListener(
				(ov, curVal, newVal) -> offsetText
                        .setText(String.format("Stroke Dash Offset: %.2f", slider.getValue())));
		root.getChildren().add(offsetText);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}