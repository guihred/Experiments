package paintexp.tool;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleSvgPathBuilder;

public class RotateTool extends AreaTool {

    private List<Circle> circles;
    private double initialRotate;
    private boolean dragged;

    @Override
    public Node createIcon() {
        return new SimpleSvgPathBuilder().fill(Color.BLACK).stroke(Color.BLACK)
				.content("m15 0 l0 3 a15 15 -5 1 0 15 15l-3 0 a12 12 5 1 1 -12 -12l0 3 4.5 -4.5z").build();
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
        super.handleKeyEvent(e, model);
        KeyCode code = e.getCode();
        switch (code) {
            case UP:
                getArea().setRotate(getArea().getRotate() + 1);
                break;
            case DOWN:
                getArea().setRotate(getArea().getRotate() - 1);
                break;
            default:
                break;
        }
    }

    @Override
    protected void addRect(final PaintModel model) {
        double hvalue = model.getScrollPane().getHvalue();
        double vvalue = model.getScrollPane().getVvalue();
        super.addRect(model);
        if (!model.getImageStack().getChildren().containsAll(getCircles(model))) {
            model.getImageStack().getChildren().addAll(getCircles(model));
        }
        getArea().setRotate(0);
        dragged = false;
        model.getScrollPane().setHvalue(hvalue);
        model.getScrollPane().setVvalue(vvalue);
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        if (!dragged) {
            super.onMouseDragged(e, model);
        } else {
            double x = e.getX() - getArea().getLayoutX() - getArea().getWidth() / 2;
            double y = e.getY() - getArea().getLayoutY() - getArea().getHeight() / 2;
            getArea().setRotate(initialRotate + Math.toDegrees(Math.atan2(y, x)));
        }
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        if (!dragged) {
            super.onMousePressed(e, model);
        }
    }

    @Override
    protected void onMouseReleased(final PaintModel model) {
        super.onMouseReleased(model);

        dragged = false;
    }

    @Override
    protected void setIntoImage(final PaintModel model) {
        getArea().setStroke(Color.TRANSPARENT);
		model.takeSnapshotFill(getArea());
        imageSelected = null;
        dragged = false;
        model.createImageVersion();
    }

    private List<Circle> getCircles(final PaintModel model) {
        if (circles == null) {
            circles = Stream.generate(() -> new SimpleCircleBuilder().radius(5).fill(Color.BLUE).stroke(Color.BLACK)
                .managed(false).cursor(Cursor.OPEN_HAND).onMousePressed(e -> {
                    circles.forEach(c -> c.setCursor(Cursor.CLOSED_HAND));
                    initialRotate = getArea().getRotate();
                    dragged = true;
                    createSelectedImage(model);
                }).onMouseReleased(e -> {
                    dragged = false;
                    circles.forEach(c -> c.setCursor(Cursor.OPEN_HAND));
                }).build()).limit(4).collect(Collectors.toList());
            double width = getArea().getWidth();
            double height = getArea().getHeight();
            Math.acos(height / width);
            circles.get(0).layoutXProperty().bind(xBinding(-1, Math.PI));
            circles.get(0).layoutYProperty().bind(yBinding(-1, Math.PI));
            circles.get(1).layoutXProperty().bind(xBinding(1, Math.PI));
            circles.get(1).layoutYProperty().bind(yBinding(1, Math.PI));
            circles.get(2).layoutXProperty().bind(xBinding(-1, 0));
            circles.get(2).layoutYProperty().bind(yBinding(-1, 0));
            circles.get(3).layoutXProperty().bind(xBinding(1, 0));
            circles.get(3).layoutYProperty().bind(yBinding(1, 0));

        }
        return circles;
    }

    private DoubleBinding xBinding(final double mul, final double add) {
        return Bindings.createDoubleBinding(() -> {
            double width = getArea().getWidth();
            double height = getArea().getHeight();
            double acos = Math.atan2(height, width);
            double s = mul * acos + add;
            double r = Math.sqrt(width * width + height * height) / 2;
            return getArea().getLayoutX() + getArea().getWidth() / 2
                + Math.cos(s + Math.toRadians(getArea().getRotate())) * r;
        }, getArea().layoutXProperty(), getArea().rotateProperty(), getArea().widthProperty(),
            getArea().heightProperty());
    }

    private DoubleBinding yBinding(final double mul, final double add) {
        return Bindings.createDoubleBinding(() -> {
            double width = getArea().getWidth();
            double height = getArea().getHeight();
            double acos = Math.atan2(height, width);
            double s = mul * acos + add;
            double r = Math.sqrt(width * width + height * height) / 2;
            return getArea().getLayoutY() + getArea().getHeight() / 2
                + Math.sin(s + Math.toRadians(getArea().getRotate())) * r;
        }, getArea().layoutYProperty(), getArea().rotateProperty(), getArea().widthProperty(),
            getArea().heightProperty());
    }
}
