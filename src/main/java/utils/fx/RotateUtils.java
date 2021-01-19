package utils.fx;

import static utils.DrawOnPoint.getWithinRange;
import static utils.ex.RunnableEx.runIf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import utils.DrawOnPoint;
import utils.ImageFXUtils;
import utils.ex.ConsumerEx;
import utils.ex.RunnableEx;

public final class RotateUtils {
    private static final double CAMERA_MODIFIER = 50.0;

    private static final double CAMERA_QUANTITY = 10.0;

    private RotateUtils() {

    }

    public static List<Circle> createDraggableRectangle(final Rectangle rect) {
        final double handleRadius = 5;
        // top left resize handle:
        Color transparent = Color.YELLOW;
        Circle resizeHandleNW = new Circle(handleRadius, transparent);
        resizeHandleNW.setManaged(false);
        resizeHandleNW.setCursor(Cursor.NW_RESIZE);
        // bind to top left corner of Rectangle:
        resizeHandleNW.layoutXProperty().bind(rect.layoutXProperty());
        resizeHandleNW.layoutYProperty().bind(rect.layoutYProperty());

        // bottom right resize handle:
        Circle resizeHandleSE = new Circle(handleRadius, transparent);
        resizeHandleSE.setCursor(Cursor.SE_RESIZE);
        // bind to bottom right corner of Rectangle:
        resizeHandleSE.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty()));
        resizeHandleSE.layoutYProperty().bind(rect.layoutYProperty().add(rect.heightProperty()));
        resizeHandleSE.setManaged(false);

        // move handle:
        Circle moveHandle = new Circle(handleRadius, transparent);
        moveHandle.setCursor(Cursor.MOVE);
        // bind to bottom center of Rectangle:
        moveHandle.layoutXProperty().bind(rect.layoutXProperty().add(rect.widthProperty().divide(2)));
        moveHandle.layoutYProperty().bind(rect.layoutYProperty().add(rect.heightProperty().divide(2)));
        moveHandle.radiusProperty().bind(rect.heightProperty().divide(2));
        moveHandle.setManaged(false);
        // force circles to live in same parent as rectangle:
        Parent newParent = rect.getParent();
        List<Circle> nodes = Arrays.asList(resizeHandleNW, resizeHandleSE, moveHandle);
        for (Circle c : nodes) {
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

        resizeHandleNW.setOnMouseDragged(event -> onNWDrag(rect, handleRadius, mouseLocation, event));

        resizeHandleSE.setOnMouseDragged(event -> onSEDrag(rect, handleRadius, mouseLocation, event));

        moveHandle.setOnMouseDragged(event -> onMoveHandleDrag(rect, handleRadius, mouseLocation, event));
        return nodes;
    }

    public static double getAngle(final double ax, final double ay, final double bx, final double by) {
        double a = ax - bx;
        double b = ay - by;
        return a > 0 ? Math.PI + Math.atan(b / a) : Math.atan(b / a);
    }

    public static double getAngle(Line line) {
        return getAngle(line.getEndX(), line.getEndY(), line.getStartX(), line.getStartY());
    }

    public static List<String> getUrl(Dragboard db) {
        if (db.hasFiles()) {
            return db.getFiles().stream().map(e -> e.toURI().toString()).collect(Collectors.toList());
        }
        if (db.hasUrl()) {
            return Arrays.asList(db.getUrl());
        }
        return Collections.emptyList();
    }

    public static void initSceneDragAndDrop(Scene scene, ConsumerEx<String> onUrl) {
        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() || db.hasUrl()) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            List<String> url = RotateUtils.getUrl(db);
            for (String string : url) {
                RunnableEx.ignore(() -> onUrl.accept(string));
            }
            event.setDropCompleted(!url.isEmpty());
            event.consume();
        });
    }

    public static void makeZoomable(Node control) {

        final double MAX_SCALE = 20.0;
        final double MIN_SCALE = 0.1;
        final double DELTA = 1.2;

        control.addEventFilter(ScrollEvent.ANY, event -> {
            double scale = control.getScaleX();
            if (event.getDeltaY() < 0) {
                scale /= DELTA;
            } else {
                scale *= DELTA;
            }
            scale = DrawOnPoint.clamp(scale, MIN_SCALE, MAX_SCALE);
            control.setScaleX(scale);
            control.setScaleY(scale);
            event.consume();
        });

    }

    public static void moveArea(StackPane stackPane, Rectangle area, ImageView imageView,
            ConsumerEx<Image> onImageCropped) {
        DoubleProperty initialX = new SimpleDoubleProperty(0);
        DoubleProperty initialY = new SimpleDoubleProperty(0);
        stackPane.setOnMousePressed(e -> {
            initialX.set(e.getX());
            initialY.set(e.getY());
            area.setStroke(Color.BLACK);
        });
        stackPane.sceneProperty().addListener((ob, old, val) -> val.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.A && e.isAltDown()) {
                area.setLayoutX(0);
                area.setLayoutY(0);
                Bounds bounds = imageView.getBoundsInLocal();
                double width = bounds.getWidth();
                double height = bounds.getHeight();
                area.setWidth(width);
                area.setHeight(height);
                cropImage(area, imageView, onImageCropped, (int) width, (int) height);

            }
        }));
        stackPane.setOnMouseDragged(e -> {
            double x0 = e.getX();
            double y0 = e.getY();
            Bounds image = imageView.getBoundsInLocal();
            double width = image.getWidth();
            double height = image.getHeight();
            double x = getWithinRange(x0, 0, width);
            double y = getWithinRange(y0, 0, height);
            area.setLayoutX(Math.min(x, initialX.get()));
            area.setLayoutY(Math.min(y, initialY.get()));
            area.setWidth(Math.abs(x - initialX.get()));
            area.setHeight(Math.abs(y - initialY.get()));
        });
        stackPane.setOnMouseReleased(e -> {
            int width = Math.max(1, (int) area.getWidth());
            int height = Math.max(1, (int) area.getHeight());
            cropImage(area, imageView, onImageCropped, width, height);
        });

    }

    public static void setMovable(Node node) {
        setMovable(node, node.getScene());
    }

    public static void setMovable(Node node, Scene scene) {
        scene.setOnKeyPressed(event -> {
            double change = event.isShiftDown() ? CAMERA_MODIFIER : CAMERA_QUANTITY;
            // What key did the user press?
            KeyCode keycode = event.getCode();
            // Step 2c: Add Zoom controls
            if (keycode == KeyCode.W) {
                node.setTranslateZ(node.getTranslateZ() + change);
            }
            if (keycode == KeyCode.S) {
                node.setTranslateZ(node.getTranslateZ() - change);
            }
            // Step 2d: Add Strafe controls
            if (keycode == KeyCode.A) {
                node.setTranslateX(node.getTranslateX() - change);
            }
            if (keycode == KeyCode.D) {
                node.setTranslateX(node.getTranslateX() + change);
            }
        });
    }

    public static void setSpinnable(Node cube, Scene scene) {
        DoubleProperty mousePosX = new SimpleDoubleProperty();
        DoubleProperty mousePosY = new SimpleDoubleProperty();
        DoubleProperty mouseOldX = new SimpleDoubleProperty();
        DoubleProperty mouseOldY = new SimpleDoubleProperty();
        final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
        final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);

        cube.getTransforms().addAll(rotateX, rotateY);
        scene.setOnMousePressed(me -> {
            mouseOldY.set(me.getSceneY());
            mouseOldX.set(me.getSceneX());
        });
        scene.setOnMouseDragged(me -> {
            mousePosX.set(me.getSceneX());
            mousePosY.set(me.getSceneY());
            rotateX.setAngle(rotateX.getAngle() - (mousePosY.get() - mouseOldY.get()));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX.get() - mouseOldX.get()));
            mouseOldX.set(mousePosX.get());
            mouseOldY.set(mousePosY.get());
        });
    }

    public static Scale setZoomable(Node node) {
        return setZoomable(node, false);
    }

    public static Scale setZoomable(Node node, boolean onlyClose) {
        Scale scale = new Scale(1, 1);
        Translate translate = new Translate(0, 0);
        node.getTransforms().addAll(scale, translate);
        final double delta = 0.1;
        DoubleProperty iniX = new SimpleDoubleProperty(0);
        DoubleProperty iniY = new SimpleDoubleProperty(0);

        node.setOnScroll(scrollEvent -> {
            double scaleValue = scale.getX();
            double s = scaleValue;
            if (scrollEvent.getDeltaY() < 0) {
                scaleValue -= delta;
            } else {
                scaleValue += delta;
            }
            if (onlyClose && scaleValue < 1) {
                scaleValue = s;
            }

            if (scaleValue <= delta) {
                scaleValue = s;
            }
            scale.setX(scaleValue);
            scale.setY(scaleValue);
            scrollEvent.consume();
        });

        node.setOnMousePressed(evt -> {
            iniX.set(evt.getX());
            iniY.set(evt.getY());
        });

        node.setOnMouseDragged(evt -> {
            double deltaX = evt.getX() - iniX.get();
            double deltaY = evt.getY() - iniY.get();
            translate.setX(translate.getX() + deltaX);
            translate.setY(translate.getY() + deltaY);
        });
        return scale;
    }

    private static void cropImage(Rectangle area, ImageView imageView, ConsumerEx<Image> onImageCropped, int width,
            int height) {
        Image image = imageView.getImage();
        if (image == null) {
            return;
        }

        WritableImage srcImage = ImageFXUtils.copyImage(image, image.getWidth(), image.getHeight());
        double p = srcImage.getWidth() / imageView.getFitWidth();
        double width1 = width * p;
        double height1 = height * p;
        if (width1 <= 0 || height1 <= 0) {
            return;
        }
        WritableImage imageSelected = new WritableImage((int) width1, (int) height1);
        double x = area.getLayoutX() * p;
        double y = area.getLayoutY() * p;
        RectBuilder.build().startX(x).startY(y).width(width1).height(height1).copyImagePart(srcImage, imageSelected,
                Color.TRANSPARENT);
        ConsumerEx.accept(onImageCropped, imageSelected);
        area.setStroke(Color.TRANSPARENT);
    }

    private static void onMoveHandleDrag(final Rectangle rect, final double handleRadius,
            Wrapper<Point2D> mouseLocation, MouseEvent event) {
        if (mouseLocation.value != null) {
            double deltaX = event.getSceneX() - mouseLocation.value.getX();
            double deltaY = event.getSceneY() - mouseLocation.value.getY();
            double newX = rect.getLayoutX() + deltaX;
            double newMaxX = newX + rect.getWidth();
            if (newX >= handleRadius && newMaxX <= rect.getWidth() - handleRadius) {
                rect.setLayoutX(newX);
            }
            double newY = rect.getLayoutY() + deltaY;
            double newMaxY = newY + rect.getHeight();
            if (newY >= handleRadius && newMaxY <= rect.getHeight() - handleRadius) {
                rect.setLayoutY(newY);
            }
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        }
    }

    private static void onNWDrag(final Rectangle rect, final double handleRadius, Wrapper<Point2D> mouseLocation,
            MouseEvent event) {
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
    }

    private static void onSEDrag(final Rectangle rect, final double handleRadius, Wrapper<Point2D> mouseLocation,
            MouseEvent event) {
        if (mouseLocation.value != null) {
            double deltaX = event.getSceneX() - mouseLocation.value.getX();
            double deltaY = event.getSceneY() - mouseLocation.value.getY();
            double newMaxX = rect.getLayoutX() + rect.getWidth() + deltaX;
            if (newMaxX >= rect.getLayoutX() && newMaxX <= rect.getWidth() - handleRadius) {
                rect.setWidth(rect.getWidth() + deltaX);
            }
            double newMaxY = rect.getLayoutY() + rect.getHeight() + deltaY;
            if (newMaxY >= rect.getLayoutY() && newMaxY <= rect.getHeight() - handleRadius) {
                rect.setHeight(rect.getHeight() + deltaY);
            }
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        }
    }

    private static void setUpDragging(final Circle circle, final Wrapper<Point2D> mouseLocation) {

        circle.setOnDragDetected(event -> {
            Parent parent = circle.getParent();
            runIf(parent, p -> p.setCursor(Cursor.CLOSED_HAND));
            mouseLocation.value = new Point2D(event.getSceneX(), event.getSceneY());
        });

        circle.setOnMouseReleased(event -> {
            Parent parent = circle.getParent();
            runIf(parent, p -> p.setCursor(Cursor.DEFAULT));
            mouseLocation.value = null;
        });
    }

    static class Wrapper<T> {
        private T value;
    }

}
