package fxsamples;

import java.util.Arrays;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class DraggingRectangle extends Application {

    @Override
    public void start(Stage primaryStage) {

        StackPane root = new StackPane();
        Rectangle rect = new Rectangle(00, 00, 400, 300);
        rect.setManaged(false);
        rect.setLayoutX(200);
        rect.setLayoutY(200);
        root.getChildren().add(rect);
        createDraggableRectangle(rect);
        rect.setFill(Color.NAVY);


        Scene scene = new Scene(root, 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void createDraggableRectangle(Rectangle rect) {
        final double handleRadius = 5;
        // top left resize handle:
        Circle resizeHandleNW = new Circle(handleRadius, Color.TRANSPARENT);
        resizeHandleNW.setManaged(false);
        resizeHandleNW.setCursor(Cursor.NW_RESIZE);
        // bind to top left corner of Rectangle:
        resizeHandleNW.layoutXProperty().bind(rect.layoutXProperty());
        resizeHandleNW.layoutYProperty().bind(rect.layoutYProperty());


        // bottom right resize handle:
        Circle resizeHandleSE = new Circle(handleRadius, Color.TRANSPARENT);
        resizeHandleSE.setCursor(Cursor.SE_RESIZE);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty()));
        resizeHandleSE.layoutYProperty().bind(rect.layoutYProperty().add(rect.heightProperty()));
        resizeHandleSE.setManaged(false);

        // move handle:
        Circle moveHandle = new Circle(handleRadius, Color.TRANSPARENT);
        moveHandle.setCursor(Cursor.MOVE);
        // bind to bottom center of Rectangle:
        moveHandle.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty().divide(2)));
        moveHandle.layoutYProperty().bind(rect.layoutYProperty().add(rect.heightProperty().divide(2)));
        moveHandle.radiusProperty().bind(rect.heightProperty().divide(2));
        moveHandle.setManaged(false);
        // force circles to live in same parent as rectangle:
        Parent newParent = rect.getParent();
        for (Circle c : Arrays.asList(resizeHandleNW, resizeHandleSE, moveHandle)) {
                if (newParent instanceof Pane && !((Pane) newParent).getChildren().contains(c)) {
                    ((Pane) newParent).getChildren().add(c);
                }
                if (newParent instanceof Group && !((Group) newParent).getChildren().contains(c)) {
                    ((Group) newParent).getChildren().add(c);
                }
            }

        Wrapper<Point2D> mouseLocation = new Wrapper<>();

        setUpDragging(resizeHandleNW, mouseLocation);
        setUpDragging(resizeHandleSE, mouseLocation);
        setUpDragging(moveHandle, mouseLocation);

        resizeHandleNW.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getLayoutX() + deltaX;
                if (newX >= handleRadius && newX <= rect.getLayoutX() + rect.getWidth() - handleRadius) {
                    rect.setLayoutX(newX);
                    rect.setWidth(rect.getWidth() - deltaX);
                }
                double newY = rect.getLayoutY() + deltaY;
                if (newY >= handleRadius && newY <= rect.getLayoutY() + rect.getHeight() - handleRadius) {
                    rect.setLayoutY(newY);
                    rect.setHeight(rect.getHeight() - deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        resizeHandleSE.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newMaxX = rect.getLayoutX() + rect.getWidth() + deltaX;
                if (newMaxX >= rect.getLayoutX()
                        && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    rect.setWidth(rect.getWidth() + deltaX);
                }
                double newMaxY = rect.getLayoutY() + rect.getHeight() + deltaY;
                if (newMaxY >= rect.getLayoutY()
                        && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleRadius) {
                    rect.setHeight(rect.getHeight() + deltaY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });

        moveHandle.setOnMouseDragged(event -> {
            if (mouseLocation.value != null) {
                double deltaX = event.getSceneX() - mouseLocation.value.getX();
                double deltaY = event.getSceneY() - mouseLocation.value.getY();
                double newX = rect.getLayoutX() + deltaX;
                double newMaxX = newX + rect.getWidth();
                if (newX >= handleRadius && newMaxX <= rect.getParent().getBoundsInLocal().getWidth() - handleRadius) {
                    rect.setLayoutX(newX);
                }
                double newY = rect.getLayoutY() + deltaY;
                double newMaxY = newY + rect.getHeight();
                if (newY >= handleRadius && newMaxY <= rect.getParent().getBoundsInLocal().getHeight() - handleRadius) {
                    rect.setLayoutY(newY);
                }
                mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
            }
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private static void setUpDragging(Circle circle, Wrapper<Point2D> mouseLocation) {

        circle.setOnDragDetected(event -> {
            circle.getParent().setCursor(Cursor.CLOSED_HAND);
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        circle.setOnMouseReleased(event -> {
            circle.getParent().setCursor(Cursor.DEFAULT);
            mouseLocation.value = null;
        });
    }

    static class Wrapper<T> {
        T value;
    }

}