package paintexp.tool;

import java.util.stream.Stream;
import javafx.scene.Group;
import utils.StringSigaUtils;

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
    MIRROR(new MirrorTool()),
    PATTERN(new PatternTool()),
    BORDER(new BorderTool());

    private PaintTool tool = emptyTool();


    PaintTools(final PaintTool paintTool) {
        tool = paintTool;
    }


    public PaintTool getTool() {
        return tool;
    }

    public String getTooltip() {
        return StringSigaUtils.splitMergeCamelCase(tool.getClass().getSimpleName().replaceAll("Tool", ""));
    }

    public static DummyTool emptyTool() {
        return new DummyTool();
    }

	public static AreaTool getSelectRectTool(PaintTool tool2, Group imageStack2) {
		return Stream.of(values()).map(PaintTools::getTool).filter(AreaTool.class::isInstance)
				.map(AreaTool.class::cast).filter(e -> imageStack2.getChildren().contains(e.getArea())).findFirst()
				.orElseGet(() -> tool2 instanceof AreaTool ? (AreaTool) tool2
						: (AreaTool) SELECT_RECT.getTool());
	}

}
