package fxsamples.bounds;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

/** Demo for understanding JavaFX Layout Bounds */
public class BoundsPlayground extends Application {
    private final ObservableList<Shape> shapes = FXCollections.observableArrayList();
    private final ObservableList<ShapePair> intersections = FXCollections.observableArrayList();
    private final ObjectProperty<BoundsType> selectedBoundsType = new SimpleObjectProperty<>(BoundsType.LAYOUT_BOUNDS);

    @Override
    public void start(Stage stage) {
        stage.setTitle("Bounds Playground");
        // define some objects to manipulate on the scene.
        Circle greenCircle = new Circle(100, 100, 50, Color.FORESTGREEN);
        greenCircle.setId("Green Circle");
        final Circle redCircle = new Circle(300, 200, 50, Color.FIREBRICK);
        redCircle.setId("Red Circle");
        final Line line = new Line(25, 300, 375, 200);
        line.setId("Line");
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStroke(Color.MIDNIGHTBLUE);
        line.setStrokeWidth(5);
        final Anchor anchor1 = new Anchor("Anchor 1", line.startXProperty(), line.startYProperty());
        final Anchor anchor2 = new Anchor("Anchor 2", line.endXProperty(), line.endYProperty());
        final Group group = new Group(greenCircle, redCircle, line, anchor1, anchor2);
        group.getChildrenUnmodifiable().stream().filter(Shape.class::isInstance).map(Shape.class::cast)
            .forEach(shapes::add);
        // monitor intersections of shapes in the scene.
        testIntersections();
        // enable dragging for the scene objects.
        Circle[] circles = { greenCircle, redCircle, anchor1, anchor2 };
        for (Circle circle : circles) {
            enableDrag(circle);
            circle.centerXProperty().addListener((ob, oldValue, newValue) -> testIntersections());
            circle.centerYProperty().addListener((ob, oldValue, newValue) -> testIntersections());
        }
        // define an overlay to show the layout bounds of the scene's shapes.
        Group layoutBoundsOverlay = new Group();
        layoutBoundsOverlay.setMouseTransparent(true);
        shapes.stream().filter(s -> !(s instanceof Anchor)).map(s -> new BoundsDisplay(selectedBoundsType, s))
            .forEach(layoutBoundsOverlay.getChildren()::add);
        // layout the scene.
        final StackPane background = new StackPane();
        background.setStyle("-fx-background-color: cornsilk;");
        final Scene scene = new Scene(new Group(background, group, layoutBoundsOverlay), 600, 500);
        background.prefHeightProperty().bind(scene.heightProperty());
        background.prefWidthProperty().bind(scene.widthProperty());
        stage.setScene(scene);
        stage.show();
        UtilityLayout.createUtilityWindow(stage, layoutBoundsOverlay, new Shape[] { greenCircle, redCircle },
            shapes, intersections, selectedBoundsType);
    }

    private void testIntersections() {
        UtilityLayout.testIntersections(shapes, intersections, selectedBoundsType);
    }


    public static void main(String[] args) {
        launch(args);
    }

    // make a node movable by dragging it around with the mouse.
    private static void enableDrag(final Circle circle) {
        final Delta dragDelta = new Delta();
        circle.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDelta.x = circle.getCenterX() - mouseEvent.getX();
            dragDelta.y = circle.getCenterY() - mouseEvent.getY();
            circle.getScene().setCursor(Cursor.MOVE);
        });
        circle.setOnMouseReleased(mouseEvent -> circle.getScene().setCursor(Cursor.HAND));
        circle.setOnMouseDragged(mouseEvent -> {
            circle.setCenterX(mouseEvent.getX() + dragDelta.x);
            circle.setCenterY(mouseEvent.getY() + dragDelta.y);
        });
        circle.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                circle.getScene().setCursor(Cursor.HAND);
            }
        });
        circle.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                circle.getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    // records relative x and y co-ordinates.
    private static class Delta {
        protected double x;
        protected double y;
    }
}