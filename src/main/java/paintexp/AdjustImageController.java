package paintexp;

import static utils.DrawOnPoint.getWithinRange;

import java.util.function.Function;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import paintexp.tool.PaintModel;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.StageHelper;
import utils.CommonsFX;
import utils.RectBuilder;

public class AdjustImageController {
    private PaintModel paintModel;
    private PaintController paintController;
    @FXML
    private ImageView view;
    @FXML
    private ColorAdjust colorAdjust;
    @FXML
    private VBox root;
    private WritableImage image;
    @FXML
    private Text saturation;
    @FXML
    private Text brightness;
    @FXML
    private Text hue;
    @FXML
    private Text contrast;
    @FXML
    private Text opacity;
    @FXML
    private Slider saturationSlider;
    @FXML
    private Slider brightnessSlider;
    @FXML
    private Slider hueSlider;
    @FXML
    private Slider contrastSlider;
    @FXML
    private Slider opacitySlider;

    @FXML
    private DoubleProperty opacityProp;

    public void adjustColors(PaintModel paintModel1, PaintController paintController1) {
        paintModel = paintModel1;
        paintController = paintController1;

        new SimpleDialogBuilder().bindWindow(paintModel1.getImageSize()).title("Adjust Image")
                .node(CommonsFX.loadParent("AdjustImage.fxml", this)).displayDialog();
    }

    public void initialize() {
        WritableImage original = paintController.getSelectedImage();
        PixelReader reader = original.getPixelReader();
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        addAdjustOption(colorAdjust.saturationProperty(), saturation, saturationSlider);
        addAdjustOption(colorAdjust.brightnessProperty(), brightness, brightnessSlider);
        addAdjustOption(colorAdjust.hueProperty(), hue, hueSlider);
        addAdjustOption(colorAdjust.contrastProperty(), contrast, contrastSlider);
        image = new WritableImage(reader, width, height);
        view.setImage(image);
        addAdjustOption(image, original, opacitySlider, opacityProp, opacity,
                color -> changeColor(colorAdjust.saturationProperty(), colorAdjust.brightnessProperty(),
                        colorAdjust.hueProperty(), opacityProp, color));
    }

    public void onActionAdjust() {
        WritableImage writableImage = RectBuilder.printNodeToImage(view, image);
        WritableImage original = paintController.getSelectedImage();
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        view.setFitWidth(width);
        view.setFitHeight(height);
        paintController.setFinalImage(writableImage);
        paintModel.createImageVersion();
        StageHelper.closeStage(root);
    }

    public static void updateImage(WritableImage image, WritableImage original, Function<Color, Color> func) {
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

    private static void addAdjustOption(DoubleProperty value, Text e, Slider saturation) {
        String text = e.getText();
        value.bind(saturation.valueProperty());
        e.textProperty()
                .bind(saturation.valueProperty().divide(saturation.getMax()).multiply(100).asString(text + " %.1f%%"));
    }

    private static void addAdjustOption(WritableImage image, WritableImage original, Slider saturation,
            DoubleProperty value, Text e, Function<Color, Color> func) {
        String text = e.getText();
        SimpleSliderBuilder.onChange(saturation, (ob, old, v) -> updateImage(image, original, func));
        value.bind(saturation.valueProperty());
        e.textProperty()
                .bind(saturation.valueProperty().divide(saturation.getMax()).multiply(100).asString(text + " %.1f%%"));
    }

    private static Color changeColor(DoubleProperty saturate, DoubleProperty bright, DoubleProperty hue,
            DoubleProperty opacity, Color color) {
        return Color.hsb(getWithinRange(color.getHue() + hue.get(), 0, 360),
                getWithinRange(color.getSaturation() + saturate.get(), 0, 1),
                getWithinRange(color.getBrightness() + bright.get(), 0, 1),
                getWithinRange(color.getOpacity() + opacity.get(), 0, 1));
    }

}
