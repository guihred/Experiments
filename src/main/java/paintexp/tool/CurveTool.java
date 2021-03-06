package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;

public class CurveTool extends PaintTool {

    private CubicCurve line;
    private int stage;

    @Override
    public Node createIcon() {
        CubicCurve icon = new CubicCurve();
        icon.setStroke(Color.BLACK);
        icon.setFill(Color.TRANSPARENT);
        icon.setStartX(0);
        icon.setStartY(0);
        icon.setControlX1(0);
        final int control = 30;
        icon.setControlY1(control);
        icon.setControlX2(control);
        icon.setControlY2(0);

        icon.setStartY(0);
        icon.setEndX(control);
        icon.setEndY(control);
        return icon;
    }

    public CubicCurve getLine() {
        if (line == null) {
            line = new CubicCurve();
            line.setStroke(Color.BLACK);
            line.setFill(Color.TRANSPARENT);
            line.setSmooth(false);
        }
        return line;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.CROSSHAIR;
    }

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        simpleHandleEvent(e, model);
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        KeyCode code = e.getCode();
        if (code == KeyCode.ESCAPE) {
            paintModel.takeSnapshotFill(getLine());
            paintModel.createImageVersion();
        }
    }

    @Override
    public void onDeselected(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getLine())) {
            model.takeSnapshot(getLine());
            children.remove(getLine());
        }
        onSelected(model);
    }

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
        if (stage == 0) {
            getLine().setEndX(e.getX());
            getLine().setEndY(e.getY());
        }
        if (stage == 1) {
            getLine().setControlX1(e.getX());
            getLine().setControlY1(e.getY());
        }
        if (stage == 2) {
            getLine().setControlX2(e.getX());
            getLine().setControlY2(e.getY());
        }
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        getLine().setStroke(model.getFrontColor());
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getLine())) {
            children.add(getLine());
        }
        if (stage == 0) {
            getLine().setStartX(e.getX());
            getLine().setControlX1(e.getX());
            getLine().setControlX2(e.getX());
            getLine().setStartY(e.getY());
            getLine().setControlY1(e.getY());
            getLine().setControlY2(e.getY());
        }
        onMouseDragged(e, model);

    }

    @Override
    public void onMouseReleased(final PaintModel model) {

        ObservableList<Node> children = model.getImageStack().getChildren();
        if ((size() >= 2 || !children.contains(getLine())) && stage == 2) {
            model.takeSnapshot(line);
            model.createImageVersion();
        }
        stage = ++stage % 3;
    }

    @Override
    public void onSelected(PaintModel model) {
        stage = 0;
        getLine().setStartX(-1);
        getLine().setControlX1(-1);
        getLine().setControlX2(-1);
        getLine().setStartY(-1);
        getLine().setEndX(-1);
        getLine().setEndY(-1);
        getLine().setControlY1(-1);
        getLine().setControlY2(-1);

        model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren()
                .add(PaintTool.propertiesPane(getLine(), "fill", "stroke", "startX", "startY", "endX", "endY",
                        "controlX1", "controlY1", "controlX2", "controlY2"));

    }

    private double size() {
        double w = getLine().getLayoutBounds().getWidth();
        double h = getLine().getLayoutBounds().getHeight();
        return Math.sqrt(w * w + h * h);
    }

}