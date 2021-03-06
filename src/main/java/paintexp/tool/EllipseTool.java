package paintexp.tool;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import simplebuilder.SimpleToggleGroupBuilder;

public class EllipseTool extends PaintTool {

    private Ellipse area;
    private int initialX;
    private int initialY;

    private FillOption option = FillOption.STROKE;

    @Override
    public Ellipse createIcon() {
        final Ellipse icon = new Ellipse(18, 15);
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.BLACK);
        return icon;
    }

    public Ellipse getArea() {
        if (area == null) {
            area = new Ellipse(3, 5);
            area.setFill(Color.TRANSPARENT);
            area.setStroke(Color.BLACK);
            area.setSmooth(false);
            area.setManaged(false);
        }
        return area;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.DISAPPEAR;
    }

    @Override
    public void onMouseDragged(final MouseEvent e, PaintModel model) {
        double radiusX = Math.abs(e.getX() - initialX);
        getArea().setRadiusX(radiusX);
        double radiusY = Math.abs(e.getY() - initialY);
        getArea().setRadiusY(radiusY);
        if (e.isShiftDown()) {
            double max = Math.max(radiusX, radiusY);
            getArea().setRadiusX(max);
            getArea().setRadiusY(max);
        }
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }
        initialX = (int) e.getX();
        getArea().setLayoutX(initialX);
        initialY = (int) e.getY();
        getArea().setLayoutY(initialY);
        getArea().setRadiusX(1);
        getArea().setRadiusY(1);
        getArea().setStroke(Color.TRANSPARENT);
        getArea().setFill(Color.TRANSPARENT);
        if (option == FillOption.STROKE || option == FillOption.STROKE_FILL) {
            getArea().setStroke(model.getFrontColor());
        }
        if (option == FillOption.FILL || option == FillOption.STROKE_FILL) {
            getArea().setFill(model.getBackColor());
        }
    }

    @Override
    public void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getRadiusX() > 2 && children.contains(getArea())) {
            model.takeSnapshot(area);
            model.createImageVersion();
        }
        children.remove(getArea());
    }

    @Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();
        Ellipse icon2 = createIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        Ellipse icon3 = createIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        Ellipse icon4 = createIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
        List<Node> togglesAs = new SimpleToggleGroupBuilder().addToggle(icon2, FillOption.STROKE)
            .addToggle(icon3, FillOption.FILL).addToggle(icon4, FillOption.STROKE_FILL)
            .onChange((o, old, newV) -> option = newV == null ? FillOption.STROKE : (FillOption) newV.getUserData())
            .select(option).getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);
        model.getToolOptions().getChildren()
                .addAll(PaintTool.propertiesPane(getArea(), "fill", "stroke", "radiusX", "radiusY", "centerX",
                        "centerY", "strokeLineJoin", "strokeLineCap"));
    }

}