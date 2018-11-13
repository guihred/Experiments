package paintexp;

import javafx.scene.Cursor;

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


    private PaintTool tool = new DummyTool();

    PaintTools() {
    }

    PaintTools(PaintTool paintTool) {
        tool = paintTool;
    }
    public Cursor getCursor() {
        return tool.getCursor();
    }

    public PaintTool getTool() {
        return tool;
    }

}
