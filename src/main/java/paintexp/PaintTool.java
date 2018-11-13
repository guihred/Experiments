package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleRectangleBuilder;

public abstract class PaintTool {
    public abstract Cursor getCursor() ;

    public abstract Node getIcon();
}

class SelectRectTool extends PaintTool {

    private Rectangle icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT)
            .stroke(Color.BLACK).strokeDashArray(1, 2, 1, 2).build();

    @Override
    public Cursor getCursor() {
        return Cursor.CROSSHAIR;
    }

    @Override
    public Node getIcon() {
        return icon;
    }

}