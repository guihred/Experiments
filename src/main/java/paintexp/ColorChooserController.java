package paintexp;

import static simplebuilder.SimpleSliderBuilder.onChange;
import static utils.DrawOnPoint.getWithinRange;
import static utils.PixelHelper.MAX_BYTE;

import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import utils.DrawOnPoint;

public class ColorChooserController {
    private static final int MAX_THREAD_HEAP = 60;
    private static final int SQUARE_SIZE = 64;
    @FXML
    private Slider hueSlider2;
    @FXML
    private Slider hueSlider;
    @FXML
    private Slider brightnessSlider;
    @FXML
    private Slider greenSlider;
    @FXML
    private Slider redSlider;
    @FXML
    private Slider opacitySlider;
    @FXML
    private Slider blueSlider;
    @FXML
    private Slider saturationSlider;
    @FXML
    private WritableImage transparentImage;
    @FXML
    private WritableImage colorsImage;
    @FXML
    private WritableImage sliderImage;
    @FXML
    private WritableImage smallImage;
    @FXML
    private Rectangle initialColorRect;
    @FXML
    private Rectangle finalColor;
    @FXML
    private Circle circle;

    private final ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> initialColor = new SimpleObjectProperty<>(Color.WHITE);
    private Runnable onSave;
    private Runnable onUse;

    public Color getCurrentColor() {
        return currentColor.get();
    }

    public void initialize() {
        onChange(hueSlider2, (o, old, newV) -> currentColor.set(Color.hsb(newV.doubleValue(),
            currentColor.get().getSaturation(), currentColor.get().getBrightness(), currentColor.get().getOpacity())));
        onChange(saturationSlider, (o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(),
            newV.doubleValue(), currentColor.get().getBrightness(), currentColor.get().getOpacity())));
        onChange(brightnessSlider, (o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(),
            currentColor.get().getSaturation(), newV.doubleValue(), currentColor.get().getOpacity())));
        onChange(redSlider, (o, old, newV) -> currentColor.set(Color.color(newV.doubleValue(),
            currentColor.get().getGreen(), currentColor.get().getBlue(), currentColor.get().getOpacity())));
        onChange(greenSlider, (o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(),
            newV.doubleValue(), currentColor.get().getBlue(), currentColor.get().getOpacity())));
        hueSlider.valueProperty().bindBidirectional(hueSlider2.valueProperty());
        hueSlider.valueProperty().addListener(e -> drawImage());
        onChange(blueSlider, (o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(),
            currentColor.get().getGreen(), newV.doubleValue(), currentColor.get().getOpacity())));
        onChange(opacitySlider, (o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(),
            currentColor.get().getGreen(), currentColor.get().getBlue(), newV.doubleValue())));
        onChange(opacitySlider, (o, old, v) -> drawImage());
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
        DrawOnPoint.transparentImage(MAX_BYTE + 1, transparentImage);
        DrawOnPoint.transparentImage(SQUARE_SIZE, smallImage);
        setSliderImage();
        drawImage();
    }

    public void onActionSave() {
        if (onSave != null) {
            onSave.run();
        }
    }

    public void onActionUse() {
        if (onUse != null) {
            onUse.run();
        }
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
        if (Thread.currentThread().getStackTrace().length > MAX_THREAD_HEAP) {
            return;
        }
        for (int x = 0; x <= MAX_BYTE; x++) {
            for (int y = 0; y <= MAX_BYTE; y++) {
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
        final int maxDegrees = 360;
        final int maxWidth = 20;
        for (int x = 0; x < maxWidth; x++) {
            for (int y = 0; y < height; y++) {
                sliderImage.getPixelWriter().setColor(x, y,
                        Color.hsb((maxDegrees - y * maxDegrees / height) % maxDegrees, 1, 1));
            }
        }
        hueSlider.setBackground(new Background(
            new BackgroundImage(sliderImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null)));
    }

    private void updateColor(WritableImage writableImage, MouseEvent e) {
        double x = getWithinRange(e.getX(), 0, MAX_BYTE);
        circle.setCenterX(x);
        double y = getWithinRange(e.getY(), 0, MAX_BYTE);
        circle.setCenterY(y);
        Color color = writableImage.getPixelReader().getColor((int) x, (int) y);
        currentColor.set(color);
    }

    public static void changeIfDifferent(Slider slider, double saturation) {
        if (Math.abs(slider.getValue() - saturation) > 0) {
            slider.setValue(saturation);
        }
        slider.setValueChanging(true);
    }



}
