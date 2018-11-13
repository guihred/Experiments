package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import utils.CommonsFX;

public enum PaintTools {
    SELECT_FREE,
    ERASER,
    BUCKET,
    EYEDROP,
    LUPE,
    PENCIL,
    BRUSH,
    SPRAY,
    SELECT_RECT(new SelectRectTool()),
    TEXT,
    LINE,
    CURVE,
    RECTANGLE,
    POLYGON,
    CIRCLE,
    ROUND;


    private Node icon = new Circle(10, CommonsFX.generateRandomColors(50).get(0));
    private PaintTool tool;

    PaintTools() {
    }

    PaintTools(PaintTool paintTool) {
        tool = paintTool;
    }
    public Cursor getCursor() {
        return tool != null ? tool.getCursor():Cursor.DEFAULT;
    }

    public Node getIcon() {
        return tool != null ? tool.getIcon() : icon;
    }

    public PaintTool getTool() {
        return tool;
    }

}
