package fxsamples;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import simplebuilder.*;

public class DrawingColors extends Application {
	@Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Colors");
		Group root = new Group();
        StackPane stackPane = new StackPane(root);
        stackPane.setPadding(new Insets(20));
        final Scene scene = new Scene(stackPane);
        scene.setFill(Color.WHITE);
        final RadialGradient gradient1 = new SimpleRadialGradientBuilder()
				.focusAngle(0)
				.focusDistance(1)
				.centerX(80)
				.centerY(45)
				.radius(120)
				.proportional(false)
				.cycleMethod(CycleMethod.NO_CYCLE)
				.stops(new Stop(0, Color.RED),
						new Stop(1, Color.BLACK))
				.build();
        final Ellipse ellipse = new SimpleEllipseBuilder()
				.centerX(100)
				.centerY(85)
				.radiusX(50)
				.fill(gradient1)
				.radiusY(70/2)
				.build();
		root.getChildren().add(ellipse);
		double ellipseHeight = ellipse.getBoundsInParent().getHeight();

        final Line blackLine = new SimpleLineBuilder()
				.startX(170)
				.startY(30)
				.endX(20)
				.endY(140)
				.fill(Color.BLACK)
				.strokeWidth(10.0F)
				.translateY(ellipseHeight + 10)
				.build();
		root.getChildren().add(blackLine);

        final LinearGradient linearGrad = new SimpleLinearGradientBuilder()
				.endY(1)
				.proportional(true).cycleMethod(CycleMethod.NO_CYCLE)
				.stops(new Stop(0.1F, Color.rgb(255, 200, 0, .784)), new Stop(1.0F, Color.rgb(0, 0, 0, .784)))
				.build();
        final Rectangle rectangle = new SimpleRectangleBuilder()
				.x(50)
				.y(50)
				.width(100)
				.height(70)
				.translateY(ellipseHeight + 10)
				.fill(linearGrad)
				.build();
		root.getChildren().add(rectangle);

        final LinearGradient cycleGrad = new SimpleLinearGradientBuilder()
				.startX(50)
				.startY(50)
				.endX(70)
				.endY(70)
				.proportional(false)
				.cycleMethod(CycleMethod.REFLECT)
				.stops(new Stop(0F, Color.rgb(0, 255, 0, .784)), new Stop(1.0F,Color.rgb(0, 0, 0, .784)))
				.build();
        final Rectangle roundRect = new SimpleRectangleBuilder()
				.x(50)
				.y(50)
				.width(100)
				.height(70)
				.arcWidth(20)
				.arcHeight(20)
				.translateY(ellipseHeight + 10 + rectangle.getHeight() + 10)
				.fill(cycleGrad)
				.build();
		
		root.getChildren().add(roundRect);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
