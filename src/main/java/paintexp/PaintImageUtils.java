package paintexp;

import static utils.DrawOnPoint.getWithinRange;

import java.util.function.Function;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import paintexp.tool.PaintModel;
import paintexp.tool.RectBuilder;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.StageHelper;

public final class PaintImageUtils {
    private static final int PREF_WIDTH = 300;

    private PaintImageUtils() {
    }

	public static void adjustColors(PaintModel paintModel, PaintController paintController) {
        VBox root = new VBox();
		WritableImage original = paintController.getSelectedImage();
        PixelReader reader = original.getPixelReader();
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        WritableImage image = new WritableImage(reader, width, height);
        ImageView view = new ImageView(image);
        view.setFitWidth(PREF_WIDTH);
        view.setPreserveRatio(true);
        root.getChildren().add(view);
        DoubleProperty saturate = new SimpleDoubleProperty(0);
        DoubleProperty bright = new SimpleDoubleProperty(0);
        DoubleProperty hue = new SimpleDoubleProperty(0);
        DoubleProperty opacity = new SimpleDoubleProperty(0);
        ColorAdjust colorAdjust = new ColorAdjust();
        view.setEffect(colorAdjust);
        addAdjustOption(root, 1, colorAdjust.saturationProperty(), "Saturation");
        addAdjustOption(root, 1, colorAdjust.brightnessProperty(), "Brightness");
        addAdjustOption(root, 1, colorAdjust.hueProperty(), "Hue");
        addAdjustOption(root, 1, colorAdjust.contrastProperty(), "Contrast");
        addAdjustOption(root, image, original, 1, opacity, "Opacity",
            color -> changeColor(saturate, bright, hue, opacity, color));
        root.getChildren().add(SimpleButtonBuilder.newButton("Adjust", e -> {
            final WritableImage writableImage = RectBuilder.printNodeToImage(view, image);
            view.setFitWidth(width);
            view.setFitHeight(height);
            paintController.setFinalImage(writableImage);
            paintModel.createImageVersion();
            StageHelper.closeStage(root);
        }));

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.show();
    }

	public static void invertColors(PaintModel paintModel, PaintController paintController) {
		WritableImage image = paintController.getSelectedImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        updateImage(writableImage, image, Color::invert);
        final WritableImage writableImage1 = writableImage;
		paintController.setFinalImage(writableImage1);
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
        final WritableImage writableImage1 = writableImage;
		paintController.setFinalImage(writableImage1);
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

    private static void addAdjustOption(final VBox root, double max, DoubleProperty value,
            final String text) {
        Text e = new Text(text);
        root.getChildren().add(e);
        Slider saturation = new SimpleSliderBuilder(-max, max, value.get()).build();
        value.bind(saturation.valueProperty());
        e.textProperty().bind(saturation.valueProperty().divide(max).multiply(100).asString(text + " %.1f%%"));
        root.getChildren().add(saturation);
    }

    private static void addAdjustOption(final VBox root, final WritableImage image, final WritableImage original,
        double max, DoubleProperty value, final String text, final Function<Color, Color> func) {
        Text e = new Text(text);
        root.getChildren().add(e);
        Slider saturation = new SimpleSliderBuilder(-max, max, value.get()).build();
        SimpleSliderBuilder.onChange(saturation, (ob, old, v) -> updateImage(image, original, func));
        value.bind(saturation.valueProperty());
        e.textProperty().bind(saturation.valueProperty().divide(max).multiply(100).asString(text + " %.1f%%"));
        root.getChildren().add(saturation);
    }

    private static Color changeColor(DoubleProperty saturate, DoubleProperty bright, DoubleProperty hue,
        DoubleProperty opacity, Color color) {
        return Color.hsb(getWithinRange(color.getHue() + hue.get(), 0, 360),
            getWithinRange(color.getSaturation() + saturate.get(), 0, 1),
            getWithinRange(color.getBrightness() + bright.get(), 0, 1),
            getWithinRange(color.getOpacity() + opacity.get(), 0, 1));
    }

    private static void updateImage(final WritableImage image, WritableImage original,
        final Function<Color, Color> func) {
        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                if (original.getPixelReader().getArgb(x, y) != 0) {
                    Color color = original.getPixelReader().getColor(x, y);
                    Color deriveColor = func.apply(color);
                    image.getPixelWriter().setColor(x, y, deriveColor);
                }
            }
        }
    }

}
