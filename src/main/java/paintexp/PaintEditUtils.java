package paintexp;

import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import paintexp.tool.RectBuilder;
import paintexp.tool.SelectRectTool;

public class PaintEditUtils {
    public static void copy(PaintModel paintModel, SelectRectTool a) {
        a.copyToClipboard(paintModel);
    }

    public static void cut(PaintModel paintModel, SelectRectTool a) {
        a.copyToClipboard(paintModel);
        Bounds bounds = a.getArea().getBoundsInParent();
        new RectBuilder()
            .startX(bounds.getMinX())
            .startY(bounds.getMinY())
            .width(bounds.getWidth())
            .height(bounds.getHeight())
            .drawRect(paintModel, paintModel.getBackColor());
    }

    public static void paste(PaintModel paintModel, SelectRectTool a) {
            if (!(paintModel.getTool() instanceof SelectRectTool)) {
                paintModel.setTool(PaintTools.SELECT_RECT.getTool());
                paintModel.changeTool(null);
            }
            a.copyFromClipboard(paintModel);
        }

    public static void selectAll(PaintModel paintModel, SelectRectTool a) {
        if (!(paintModel.getTool() instanceof SelectRectTool)) {
            paintModel.setTool(PaintTools.SELECT_RECT.getTool());
            paintModel.changeTool(null);
        }

        a.selectArea(0, 0, (int) paintModel.getImage().getWidth(), (int) paintModel.getImage().getHeight(), paintModel);
    }

    public static void undo(PaintModel paintModel) {
        List<WritableImage> imageVersions = paintModel.getImageVersions();
        if (!imageVersions.isEmpty()) {
            WritableImage writableImage = imageVersions.remove(imageVersions.size() - 1);
            paintModel.getImageStack().getChildren().clear();
            ImageView imageView = new ImageView(writableImage);
            paintModel.setImage(writableImage);
            paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
            paintModel.getImageStack().getChildren().add(imageView);
        }
    }

}
