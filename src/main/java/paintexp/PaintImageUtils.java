package paintexp;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import paintexp.tool.PaintModel;

public final class PaintImageUtils {


    private PaintImageUtils() {
    }

    public static void addEffect(PaintModel paintModel, PaintController paintController) {
        new EffectsController().show(paintController, paintModel);
    }

    public static void adjustColors(PaintModel paintModel, PaintController paintController) {
        new AdjustImageController().adjustColors(paintModel, paintController);
    }

    public static void invertColors(PaintModel paintModel, PaintController paintController) {
        WritableImage image = paintController.getSelectedImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        AdjustImageController.updateImage(writableImage, image, Color::invert);
        paintController.setFinalImage(writableImage);
        paintModel.createImageVersion();
    }

    public static void mirrorHorizontally(PaintModel paintModel, PaintController paintController) {
        WritableImage image = paintController.getSelectedImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setColor(width - i - 1, j, pixelReader.getColor(i, j));
            }
        }
        paintController.setFinalImage(writableImage);
        paintModel.createImageVersion();
    }

    public static void mirrorVertically(PaintModel paintModel, PaintController paintController) {
        WritableImage image = paintController.getSelectedImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setColor(i, height - j - 1, pixelReader.getColor(i, j));
            }
        }
        paintController.setFinalImage(writableImage);
        paintModel.createImageVersion();
    }


}
