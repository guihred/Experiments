package paintexp;

import javafx.scene.Cursor;
import paintexp.tool.*;

public enum PaintTools {
    SELECT_FREE,
    ERASER(new EraserTool()),
    BUCKET(new BucketTool()),
    EYEDROP,
    LUPE,
    PENCIL(new PencilTool()),
    BRUSH,
    SPRAY,
    SELECT_RECT(new SelectRectTool()),
    TEXT,
    LINE(new LineTool()),
    CURVE,
	RECTANGLE(new RectangleTool()),
	POLYGON(new PolygonTool()),
	CIRCLE(new EllipseTool()),
    ROUND;


    private PaintTool tool = new DummyTool();

    PaintTools() {
    }

    PaintTools(final PaintTool paintTool) {
        tool = paintTool;
    }
    public Cursor getCursor() {
        return tool.getCursor();
    }

    public PaintTool getTool() {
        return tool;
    }

}