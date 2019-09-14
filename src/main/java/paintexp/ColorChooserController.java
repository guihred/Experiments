package paintexp;

import static paintexp.ColorChooser.changeIfDifferent;
import static utils.DrawOnPoint.getWithinRange;
import static utils.PixelHelper.MAX_BYTE;

import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ColorChooserController {
    public static final String PERCENT_FORMAT = "%.0f%%";
    @FXML
    private WritableImage colorsImage;
    @FXML
    private Slider hueSlider2;
    @FXML
    private WritableImage transparentImage;
    @FXML
    private Slider hueSlider;
    @FXML
    private Slider redSlider;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Slider blueSlider;
    @FXML
    private Slider saturationSlider;
    @FXML
    private WritableImage sliderImage;
    @FXML
    private WritableImage smallImage;
    @FXML
    private Slider brightnessSlider;
    @FXML
    private Text brightnessText;
    @FXML
    private Text opacityText;

    @FXML
    private Text blueText;

    @FXML
    private Text saturationText;

    @FXML
    private Text greenText;
    @FXML
    private Text hueText;

    @FXML
    private Text redText;
    @FXML
    private Slider greenSlider;
    private ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> initialColor = new SimpleObjectProperty<>(Color.WHITE);
    @FXML
    private Rectangle initialColorRect;
    @FXML
    private Rectangle finalColor;
    private Runnable onSave;
    private Runnable onUse;
    @FXML
    private Circle circle;

    public Color getCurrentColor() {
        return currentColor.get();
    }

    public void initialize() {
        hueSlider2.valueProperty().addListener((o, old, newV) -> currentColor.set(Color.hsb(newV.doubleValue(),
            currentColor.get().getSaturation(), currentColor.get().getBrightness(), currentColor.get().getOpacity())));
        saturationSlider.valueProperty()
            .addListener((o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(), newV.doubleValue(),
                currentColor.get().getBrightness(), currentColor.get().getOpacity())));
        brightnessSlider.valueProperty()
            .addListener((o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(),
                currentColor.get().getSaturation(), newV.doubleValue(), currentColor.get().getOpacity())));
        redSlider.valueProperty().addListener((o, old, newV) -> currentColor.set(Color.color(newV.doubleValue(),
            currentColor.get().getGreen(), currentColor.get().getBlue(), currentColor.get().getOpacity())));
        greenSlider.valueProperty()
            .addListener((o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(), newV.doubleValue(),
                currentColor.get().getBlue(), currentColor.get().getOpacity())));
        hueSlider.valueProperty().addListener(e -> drawImage());
        blueSlider.valueProperty()
            .addListener((o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(),
                currentColor.get().getGreen(), newV.doubleValue(), currentColor.get().getOpacity())));
        opacitySlider.valueProperty()
            .addListener((o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(),
                currentColor.get().getGreen(), currentColor.get().getBlue(), newV.doubleValue())));
        opacitySlider.valueProperty().addListener((o, old, v) -> drawImage());
        finalColor.fillProperty().bind(currentColor);
        initialColorRect.fillProperty().bind(initialColor);
        currentColor.addListener((o, old, newV) -> {
            if (Objects.equals(old, newV)) {
                return;
            }
            changeIfDifferent(saturationSlider, newV.getSaturation());
            changeIfDifferent(brightnessSlider, newV.getBrightness());
            changeIfDifferent(hueSlider, newV.getHue());
            changeIfDifferent(hueSlider2, newV.getHue());
            changeIfDifferent(redSlider, newV.getRed());
            changeIfDifferent(greenSlider, newV.getGreen());
            changeIfDifferent(blueSlider, newV.getBlue());
            changeIfDifferent(opacitySlider, newV.getOpacity());
            circle.setCenterX(newV.getSaturation() * MAX_BYTE);
            circle.setCenterY(MAX_BYTE * (1 - newV.getBrightness()));
        });
        ColorChooser.transparentImage(256, transparentImage);
        ColorChooser.transparentImage(64, smallImage);
        bindText(brightnessText, brightnessSlider);
        bindText(opacityText, opacitySlider);
        bindText(blueText, blueSlider);
        bindText(saturationText, saturationSlider);
        bindText(greenText, greenSlider);
        hueText.textProperty().bind(hueSlider.valueProperty().asString("%.0f°"));
        bindText(redText, redSlider);
        setSliderImage();
        drawImage();
    }

    public void onActionSave(ActionEvent e) {
        if (onSave != null) {
            onSave.run();
        }
        Node target = (Node) e.getTarget();
        Stage window = (Stage) target.getScene().getWindow();
        window.close();
    }

    public void onActionUse(ActionEvent e) {
        if (onUse != null) {
            onUse.run();
        }
        Node target = (Node) e.getTarget();
        Stage window = (Stage) target.getScene().getWindow();
        window.close();
    }

    public void onMouseDraggedStackPane0(MouseEvent e) {
        updateColor(colorsImage, e);
    }

    public void onMousePressedStackPane0(MouseEvent e) {
        updateColor(colorsImage, e);
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor.set(currentColor);
        initialColor.set(currentColor);
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void setOnUse(Runnable onUse) {
        this.onUse = onUse;
    }

    private void drawImage() {
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                colorsImage.getPixelWriter().setColor(x, y, Color.hsb(hueSlider.getValue(), (double) x / MAX_BYTE,
                    (MAX_BYTE - (double) y) / MAX_BYTE, opacitySlider.getValue()));
            }
        }

        currentColor.set(Color.hsb(hueSlider.getValue(), currentColor.get().getSaturation(),
            currentColor.get().getBrightness(), currentColor.get().getOpacity()));
        changeIfDifferent(hueSlider2, hueSlider.getValue());
    }

    private void setSliderImage() {
        double height = sliderImage.getHeight();
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < height; y++) {
                sliderImage.getPixelWriter().setColor(x, y, Color.hsb((360 - y * 360 / height) % 360, 1, 1));
            }
        }
        hueSlider.setBackground(new Background(new BackgroundImage(sliderImage, BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    private void updateColor(WritableImage writableImage, MouseEvent e) {
        double x = getWithinRange(e.getX(), 0, MAX_BYTE);
        circle.setCenterX(x);
        double y = getWithinRange(e.getY(), 0, MAX_BYTE);
        circle.setCenterY(y);
        Color color = writableImage.getPixelReader().getColor((int) x, (int) y);
        currentColor.set(color);
    }

    private static void bindText(Text text, Slider slider) {
        text.textProperty().bind(slider.valueProperty().multiply(100).asString(PERCENT_FORMAT));
    }
}
