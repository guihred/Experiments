package paintexp;

import javafx.scene.Cursor;
import paintexp.tool.*;

public enum PaintTools {
    //    SELECT_FREE,
    ERASER(new EraserTool()),
    BUCKET(new BucketTool()),
    EYEDROP(new EyedropTool()),
    //    LUPE,
    PENCIL(new PencilTool()),
    BRUSH(new BrushTool()),
    SPRAY(new SprayTool()),
    SELECT_RECT(new SelectRectTool()),
	ROTATE(new RotateTool()),
    TEXT(new TextTool()),
    LINE(new LineTool()),
    CURVE(new CurveTool()),
	RECTANGLE(new RectangleTool()),
	POLYGON(new PolygonTool()),
	CIRCLE(new EllipseTool()),
	PICTURE(new PictureTool()),
	BLUR(new BlurTool()),
    ;

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
