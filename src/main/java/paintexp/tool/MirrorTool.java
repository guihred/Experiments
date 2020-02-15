package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleSvgPathBuilder;

public class MirrorTool extends PaintTool {

    private IntegerProperty length = new SimpleIntegerProperty(10);
    private DoubleProperty opacity = new SimpleDoubleProperty(1);
    private Slider lengthSlider;
    private Slider opacitySlider;
    private Circle circle0;
    private Circle circle1;
    private double dx;
    private double dy;

    @Override
    public Node createIcon() {
        return new SimpleSvgPathBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
            .content("m0 50 a 20 20 1 1 0 1 1m30 -30 a 20 20 1 1 0 1 1.00m-40 40 v5 h-20 v10 m50 -45 v5 h-20 v10 ")
            .build();
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        PaintTool.handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onDeselected(PaintModel model) {
        circle0 = null;
        circle1 = null;
        dx = 0;
        dy = 0;
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren().add(getLengthSlider());
        model.getToolOptions().getChildren().add(getOpacitySlider());
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        onMouseMoved(e, model);
        if (withinImage(x2, y2, model.getImage()) && circle0 != null && circle1 != null) {
            int r = length.get();
            RectBuilder.build().startX(circle1.getCenterX()).startY(circle1.getCenterY()).endX(circle0.getCenterX())
                .endY(circle0.getCenterY()).width(r).height(r)
                .drawCircle(model.getImage(), model.getImageVersions(), opacity.get());
            circle0.setCenterX(x2 - dx);
            circle0.setCenterY(y2 - dy);
        }
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        if (withinImage(x2, y2, model.getImage())) {
            if (circle1 == null) {
                circle1 = newCircle(x2, y2, model);
            } else if (dx == 0) {
                dx = circle1.getCenterX() - circle0.getCenterX();
                dy = circle1.getCenterY() - circle0.getCenterY();
            }
        }
    }

    @Override
    protected void simpleHandleEvent(MouseEvent e, PaintModel model) {
        if (MouseEvent.MOUSE_MOVED.equals(e.getEventType())) {
            onMouseMoved(e, model);
        }
        super.simpleHandleEvent(e, model);
    }

    private void addIfNotContains(PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(circle0)) {
            children.add(circle0);
        }
        if (!children.contains(circle1)) {
            children.add(circle1);
        }
    }

    private Slider getLengthSlider() {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(1, 30, 10).bindBidirectional(length).prefWidth(50).build();
        }
        return lengthSlider;
    }

    private Slider getOpacitySlider() {
        if (opacitySlider == null) {
            opacitySlider = new SimpleSliderBuilder(0, 1, 1).bindBidirectional(opacity).prefWidth(50).build();
        }
        return opacitySlider;
    }

    private Circle newCircle(double x2, double y2, PaintModel model) {
        Circle circle01 = new Circle(length.get(), Color.TRANSPARENT);
        circle01.setStroke(Color.BLACK);
        circle01.radiusProperty().bind(length);
        circle01.setCenterX(x2);
        circle01.setCenterY(y2);
        model.getImageStack().getChildren().add(circle01);
        return circle01;
    }

    private void onMouseMoved(MouseEvent e, PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        if (circle0 == null) {
            circle0 = newCircle(x, y, model);
        }
        if (circle1 != null) {
            circle1.setCenterX(x);
            circle1.setCenterY(y);
            if (dx != 0) {
                circle0.setCenterX(x - dx);
                circle0.setCenterY(y - dy);

                addIfNotContains(model);
            }
            return;
        }
        circle0.setCenterX(x);
        circle0.setCenterY(y);

    }
}
