package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleRectangleBuilder;

class SelectRectTool extends PaintTool {

    private Rectangle icon;

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT)
            .stroke(Color.BLACK).strokeDashArray(1, 2, 1, 2).build();
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.CROSSHAIR;
    }

}