package paintexp;

import static paintexp.tool.DrawOnPoint.getWithinRange;

import java.util.function.Function;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;
import utils.CommonsFX;

public class PaintImageUtils {
    public static void adjustColors(PaintModel paintModel) {
        Stage stage = new Stage();
        VBox root = new VBox();
        WritableImage original = paintModel.getSelectedImage();
        PixelReader reader = original.getPixelReader();
        WritableImage image = new WritableImage(reader, (int) original.getWidth(), (int) original.getHeight());
        ImageView view = new ImageView(image);
        view.setFitWidth(300);
        view.setPreserveRatio(true);
        root.getChildren().add(view);
        DoubleProperty saturate = new SimpleDoubleProperty(0);
        DoubleProperty bright = new SimpleDoubleProperty(0);
        DoubleProperty hue = new SimpleDoubleProperty(0);
        DoubleProperty opacity = new SimpleDoubleProperty(0);
        addAdjustOption(root, image, original, 1, saturate, "Saturate",
                color -> changeColor(saturate, bright, hue, opacity, color));
        addAdjustOption(root, image, original, 1, bright, "Brightness",
                color -> changeColor(saturate, bright, hue, opacity, color));
        addAdjustOption(root, image, original, 180, hue, "Hue",
                color -> changeColor(saturate, bright, hue, opacity, color));
        addAdjustOption(root, image, original, 1, opacity, "Opacity",
                color -> changeColor(saturate, bright, hue, opacity, color));
        root.getChildren().add(CommonsFX.newButton("Adjust", e -> {
            final WritableImage writableImage = image;
            paintModel.setFinalImage(writableImage);
            paintModel.createImageVersion();
            stage.close();
        }));

        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void invertColors(PaintModel paintModel) {
        WritableImage image = paintModel.getSelectedImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        updateImage(writableImage, image, Color::invert);
        final WritableImage writableImage1 = writableImage;
        paintModel.setFinalImage(writableImage1);
        paintModel.createImageVersion();
    }

    public static void mirrorHorizontally(PaintModel paintModel) {
        WritableImage image = paintModel.getSelectedImage();
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
        paintModel.setFinalImage(writableImage1);
        paintModel.createImageVersion();
    }

    public static void mirrorVertically(PaintModel paintModel) {
        WritableImage image = paintModel.getSelectedImage();
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
        final WritableImage writableImage1 = writableImage;
        paintModel.setFinalImage(writableImage1);
        paintModel.createImageVersion();
    }

    private static void addAdjustOption(final VBox root, final WritableImage image, final WritableImage original,
            double max, DoubleProperty value, final String text, final Function<Color, Color> func) {
        Text e = new Text(text);
        root.getChildren().add(e);
        Slider saturation = new SimpleSliderBuilder(-max, max, value.get()).build();
        saturation.valueChangingProperty().addListener((ob, old, v) -> updateImage(saturation, image, original, func));
        saturation.valueProperty().addListener((ob, old, v) -> updateImage(saturation, image, original, func));
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

    private static void updateImage(final Slider saturation, final WritableImage image, WritableImage original,
            final Function<Color, Color> func) {
        if (!saturation.isValueChanging()) {
            updateImage(image, original, func);
        }
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