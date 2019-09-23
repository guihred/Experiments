package paintexp.tool;

import java.util.stream.Stream;
import javafx.scene.Cursor;
import javafx.scene.Group;

public enum PaintTools {
    SELECT_FREE(new SelectFreeTool()),
    ERASER(new EraserTool()),
    BUCKET(new BucketTool()),
    EYEDROP(new EyedropTool()),
    LUPE(new WandTool()),
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
	BORDER(new BorderTool()),
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

    public String getTooltip() {
        return tool.getClass().getSimpleName().replaceAll("Tool", "")
                .replaceAll("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|\\W+", " ");
    }

	public static SelectRectTool getSelectRectTool(PaintTool tool2, Group imageStack2) {
		return Stream.of(values()).map(PaintTools::getTool).filter(SelectRectTool.class::isInstance)
	            .map(SelectRectTool.class::cast).filter(e -> imageStack2.getChildren().contains(e.getArea()))
	            .findFirst().orElseGet(() -> tool2 instanceof SelectRectTool ? (SelectRectTool) tool2
				        : (SelectRectTool) SELECT_RECT.getTool());
	}

}
