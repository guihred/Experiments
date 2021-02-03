package fxsamples;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import simplebuilder.SimpleCubicCurveBuilder;

public class DrawingShape extends Application {
	@Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Drawing Shapes");
        Group root = new Group();
        StackPane stack = new StackPane(root);
        stack.setPadding(new Insets(20));
        Scene scene = new Scene(stack);
        scene.setFill(Color.WHITE);
        final CubicCurve cubicCurve = new SimpleCubicCurveBuilder()
				.startX(50)
				.startY(75)
				.controlX1(80)
				.controlY1(-25)
				.controlX2(110)
				.controlY2(175)
				.endX(140)
				.endY(75)
				.strokeType(StrokeType.CENTERED)
				.stroke(Color.BLACK)
				.strokeWidth(3)
				.fill(Color.WHITE)
				.build();
		root.getChildren().add(cubicCurve);


        final MoveTo moveTo = new MoveTo(50, 150);

        final QuadCurveTo quadCurveTo = new QuadCurveTo(100, 50, 150, 150);

        final LineTo lineTo1 = new LineTo(50, 150);

        final LineTo lineTo2 = new LineTo(100, 275);

        final LineTo lineTo3 = new LineTo(150, 150);

        Path path = new Path();
        path.setStrokeWidth(3);
		path.getElements().addAll(moveTo, quadCurveTo, lineTo1, lineTo2,
				lineTo3);
        final int minY = 30;
        path.setTranslateY(minY);
		root.getChildren().add(path);

        final QuadCurve quad = new QuadCurve(50, 50, 125, 150, 150, 50);
		quad.setTranslateY(path.getBoundsInParent().getMaxY());
		quad.setStrokeWidth(3);
		quad.setStroke(Color.BLACK);
		quad.setFill(Color.WHITE);
		root.getChildren().add(quad);

        final Ellipse bigCircle = new Ellipse(100, 100, 50, 75 / 2);
		bigCircle.setStrokeWidth(3);
		bigCircle.setStroke(Color.BLACK);
		bigCircle.setFill(Color.WHITE);

        final Ellipse smallCircle = new Ellipse(100, 100, 35 / 2, 25 / 2);

		Shape donut = Shape.subtract(bigCircle, smallCircle);
        final double strokeWidth = 1.8;
        donut.setStrokeWidth(strokeWidth);
		donut.setStroke(Color.BLACK);

        final Color purpleColor = Color.rgb(255, 200, 0);
        donut.setFill(purpleColor);

        final DropShadow dropShadow = new DropShadow(5, 2.0F, 2.0F,
                Color.grayRgb(50, .588));
		donut.setEffect(dropShadow);

        donut.setTranslateY(quad.getBoundsInParent().getMinY() + minY);
		root.getChildren().add(donut);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
