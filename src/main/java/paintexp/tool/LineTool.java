package paintexp.tool;

import graphs.entities.Edge;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import simplebuilder.SimpleLineBuilder;

public class LineTool extends PaintTool {

    private Line line;

    @Override
    public Node createIcon() {
        return new SimpleLineBuilder().startX(0).startY(0).endX(30).endY(30).stroke(Color.BLACK).build();
    }

    public Line getLine() {
        if (line == null) {
            line = new SimpleLineBuilder().layoutX(0).layoutY(0).managed(false).build();
        }
        return line;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.CROSSHAIR;
    }

    @Override
    public void onMouseDragged(final MouseEvent e, PaintModel model) {
        getLine().setEndX(e.getX());
        getLine().setEndY(e.getY());
        if (e.isShiftDown()) {
            double angulo = -Math.PI / 2 - Edge.getAngulo(getLine());
            final double ang = 2 * Math.PI / 8;
            long round = Math.round(angulo / ang);
            double size = size();
            double sin = Math.sin(round * ang) * size;
            double cos = Math.cos(round * ang) * size;
            getLine().setEndX(getLine().getStartX() + sin);
            if (Math.abs(sin) > 1) {
                getLine().setEndY(getLine().getStartY() + cos);
            }
        }
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        getLine().setStroke(model.getFrontColor());
        model.getImageStack().getChildren().add(getLine());
        getLine().setStartX(e.getX());
        getLine().setStartY(e.getY());
        onMouseDragged(e, model);
    }

    @Override
    public void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (size() > 2) {
            RectBuilder.build().startX(line.getStartX()).startY(line.getStartY()).endX(line.getEndX())
                .endY(line.getEndY()).drawLine(model.getImage(), model.getFrontColor());
        }
        children.remove(getLine());
    }

    private double size() {
        double w = getLine().getLayoutBounds().getWidth();
        double h = getLine().getLayoutBounds().getHeight();
        return Math.sqrt(w * w + h * h);
    }
}