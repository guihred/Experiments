package fxsamples;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import simplebuilder.SimpleLineBuilder;
import simplebuilder.SimpleRectangleBuilder;

public class DrawingColors extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Chapter 2 Painting Colors");
		Group root = new Group();
		Scene scene = new Scene(root, 350, 300, Color.WHITE);
		// Red ellipse with radial gradient color
		Ellipse ellipse = new Ellipse(100, // center X
				50 + 70 / 2, // center Y
				50, // radius X
				70 / 2); // radius Y
		RadialGradient gradient1 = new RadialGradient(0, // focusAngle
				.1, // focusDistance
				80, // centerX
				45, // centerY
				120, // radius
				false, // proportional
				CycleMethod.NO_CYCLE, // cycleMethod
				new Stop(0, Color.RED), // stops
				new Stop(1, Color.BLACK));
		ellipse.setFill(gradient1);
		root.getChildren().add(ellipse);
		double ellipseHeight = ellipse.getBoundsInParent().getHeight();
		// thick black line behind second shape
		Line blackLine = new SimpleLineBuilder()
				.startX(170)
				.startY(30)
				.endX(20)
				.endY(140)
				.fill(Color.BLACK)
				.strokeWidth(10.0f)
				.translateY(ellipseHeight + 10)
				.build();
		root.getChildren().add(blackLine);
		// A rectangle filled with a linear gradient with a translucent color.
		Rectangle rectangle = new SimpleRectangleBuilder().x(50).y(50).width(100).height(70)
				.translateY(ellipseHeight + 10).build();
		LinearGradient linearGrad = new LinearGradient(0, // start X
				0, // start Y
				0, // end X
				1, // end Y
				true, // proportional
				CycleMethod.NO_CYCLE, // cycle colors
				// stops
				new Stop(0.1f, Color.rgb(255, 200, 0, .784)), new Stop(1.0f,
						Color.rgb(0, 0, 0, .784)));
		rectangle.setFill(linearGrad);
		root.getChildren().add(rectangle);
		// A rectangle filled with a linear gradient with a reflective cycle.
		Rectangle roundRect = new SimpleRectangleBuilder()
				.x(50)
				.y(50)
				.width(100)
				.height(70)
				.arcWidth(20)
				.arcHeight(20)
				.translateY(ellipseHeight + 10 + rectangle.getHeight() + 10)
				.build();
		LinearGradient cycleGrad = new LinearGradient(50, // start X
				50, // start Y
				70, // end X
				70, // end Y
				false, // proportional
				CycleMethod.REFLECT, // cycleMethod
				new Stop(0f, Color.rgb(0, 255, 0, .784)), new Stop(1.0f,
						Color.rgb(0, 0, 0, .784)));
		roundRect.setFill(cycleGrad);
		root.getChildren().add(roundRect);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
