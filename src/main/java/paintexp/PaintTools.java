package paintexp;

import javafx.scene.Cursor;
import paintexp.tool.BucketTool;
import paintexp.tool.DummyTool;
import paintexp.tool.EllipseTool;
import paintexp.tool.EraserTool;
import paintexp.tool.LineTool;
import paintexp.tool.PaintTool;
import paintexp.tool.PencilTool;
import paintexp.tool.RectangleTool;
import paintexp.tool.SelectRectTool;

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
    POLYGON,
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
