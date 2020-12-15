package paintexp;

import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Rectangle;
import paintexp.tool.*;
import utils.ImageFXUtils;
import utils.fx.PixelatedImageView;
import utils.fx.RectBuilder;
import utils.fx.ZoomableScrollPane;

public final class PaintEditUtils {
    private PaintEditUtils() {
    }

    public static void copy(PaintModel paintModel, PaintController controller) {
        controller.getTool().onDeselected(paintModel);
        WritableImage selectedImage = controller.getSelectedImage();
        AreaTool a = controller.getCurrentSelectTool();
        a.copyToClipboard(selectedImage);
    }

    public static void cut(PaintModel paintModel, PaintController paintController) {
        if (!(paintController.getTool() instanceof AreaTool)) {
            paintController.changeTool(PaintTools.SELECT_RECT.getTool());
        }
        AreaTool a = paintController.getCurrentSelectTool();
        WritableImage copyToClipboard = a.copyToClipboard(paintModel.getImage());
        Bounds bounds = a.getArea().getBoundsInParent();
        RectBuilder.build().startX(bounds.getMinX()).startY(bounds.getMinY()).width(bounds.getWidth() + 1)
            .height(bounds.getHeight() + 1).drawRect(paintModel.getImage(), copyToClipboard, paintModel.getBackColor());
        a.deleteImage(paintModel, bounds);
    }

    public static void paste(PaintModel paintModel, PaintController paintController) {
        if (!(paintController.getTool() instanceof AreaTool)) {
            paintController.changeTool(PaintTools.SELECT_RECT.getTool());
        }
        Image pastedImg = ImageFXUtils.getClipboardImage();
        if (pastedImg != null) {
            WritableImage image = paintModel.getImage();
            double width = image.getWidth();
            double height = image.getHeight();
            if (pastedImg.getWidth() > width || pastedImg.getHeight() > height) {
                WritableImage writableImage = ImageFXUtils.copyImage(pastedImg, (int) width, (int) height);
                SimplePixelReader.paintColor(writableImage, paintModel.getBackColor());
                RectBuilder.copyImagePart(image, writableImage,
                        new Rectangle(width, height));
                changeCurrentImage(paintModel, writableImage);
            }
        }
        paintController.getCurrentSelectTool().pasteFromClipboard(paintModel);

    }

    public static void redo(PaintModel paintModel, PaintController controller) {
        List<WritableImage> imageVersions = paintModel.getImageVersions();
        if (imageVersions.isEmpty()) {
            return;
        }
        if (paintModel.getCurrentVersion() + 1 >= paintModel.getImageVersions().size()) {
            return;
        }
        controller.getTool().onDeselected(paintModel);
        WritableImage writableImage = paintModel.getCurrentImage();
        if (!imageVersions.isEmpty() && RectBuilder.isEqualImage(paintModel.getImage(), writableImage)) {
            paintModel.incrementCurrentVersion();
            writableImage = imageVersions.get(paintModel.getCurrentVersion());
        }
        WritableImage e = new WritableImage(writableImage.getPixelReader(), (int) writableImage.getWidth(),
            (int) writableImage.getHeight());

        changeCurrentImage(paintModel, e);
    }

    public static void selectAll(PaintModel paintModel, PaintController paintController) {
        paintController.changeTool(PaintTools.SELECT_RECT.getTool());
        paintController.getCurrentSelectTool().selectArea(0, 0, (int) paintModel.getImage().getWidth(),
                (int) paintModel.getImage().getHeight(), paintModel);
    }

    public static void undo(PaintModel paintModel, PaintController controller) {
        List<WritableImage> imageVersions = paintModel.getImageVersions();
        if (imageVersions.isEmpty()) {
            return;
        }
        controller.getTool().onDeselected(paintModel);
        WritableImage writableImage = paintModel.getCurrentImage();
        if (!imageVersions.isEmpty() && RectBuilder.isEqualImage(paintModel.getImage(), writableImage)) {
            paintModel.decrementCurrentVersion();
            writableImage = imageVersions.get(paintModel.getCurrentVersion());
        }
        WritableImage e = new WritableImage(writableImage.getPixelReader(), (int) writableImage.getWidth(),
            (int) writableImage.getHeight());

        changeCurrentImage(paintModel, e);
    }

    private static void changeCurrentImage(PaintModel paintModel, WritableImage e) {
        ZoomableScrollPane scrollPane = paintModel.getScrollPane();
        double hvalue = scrollPane.getHvalue();
        double vvalue = scrollPane.getVvalue();

        paintModel.getImageStack().getChildren().clear();
        ImageView imageView = new PixelatedImageView(e);
        paintModel.setImage(e);
        paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        paintModel.getImageStack().getChildren().add(imageView);
        scrollPane.setHvalue(hvalue);
        scrollPane.setVvalue(vvalue);
    }

}
