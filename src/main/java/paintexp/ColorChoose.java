package paintexp;

import static paintexp.tool.DrawOnPoint.getWithinRange;

import java.util.Objects;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleTabPaneBuilder;
import utils.ResourceFXUtils;

public class ColorChoose extends Application {
    private static final String PERCENT_FORMAT = "%.0f%%";
    private ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.WHITE);
    private Rectangle finalColor = new Rectangle(50, 50);
    //    private Runnable onUse;
    //    private Runnable onSave;
    private Circle circle = new Circle(2, Color.BLACK);

    private final Slider hueSlider = new SimpleSliderBuilder(0, 359, 0).onChange(
            (o, old, newV) -> currentColor.set(Color.hsb(newV.doubleValue(), currentColor.get().getSaturation(),
                    currentColor.get().getBrightness(), currentColor.get().getOpacity())))
            .build();
    private final Slider saturationSlider = new SimpleSliderBuilder(0, 1, 0)
            .onChange((o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(), newV.doubleValue(),
                    currentColor.get().getBrightness(), currentColor.get().getOpacity())))
            .build();
    private final Slider brightnessSlider = new SimpleSliderBuilder(0, 1, 0)
            .onChange((o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(),
                    currentColor.get().getSaturation(), newV.doubleValue(), currentColor.get().getOpacity())))
            .build();
    private final Slider redSlider = new SimpleSliderBuilder(0, 1, 0)
            .onChange((o, old, newV) -> currentColor.set(Color.color(newV.doubleValue(), currentColor.get().getGreen(),
                    currentColor.get().getBlue(), currentColor.get().getOpacity())))
            .build();
    private final Slider greenSlider = new SimpleSliderBuilder(0, 1, 0)
            .onChange((o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(), newV.doubleValue(),
                    currentColor.get().getBlue(), currentColor.get().getOpacity())))
            .build();
    private final Slider blueSlider = new SimpleSliderBuilder(0, 1, 0)
            .onChange((o, old, newV) -> currentColor.set(Color.color(currentColor.get().getRed(),
                    currentColor.get().getGreen(), newV.doubleValue(), currentColor.get().getOpacity())))
            .build();
    private final Slider opacitySlider = new SimpleSliderBuilder(0, 1, 1)
            .onChange((o, old,
                    newV) -> currentColor.set(Color.color(currentColor.get().getRed(), currentColor.get().getGreen(),
                            currentColor.get().getBlue(), newV.doubleValue())))
            .onChange((o, old, v) -> drawImage()).build();
    private Slider mainHueslider;
    private WritableImage colorsImage = new WritableImage(256, 256);

    @Override
    public void start(Stage primaryStage) throws Exception {
        FlowPane root = new FlowPane();
        SimpleSliderBuilder simpleSliderBuilder = new SimpleSliderBuilder(0, 359, 0);
        mainHueslider = simpleSliderBuilder.build();
        mainHueslider.setId("hueSlider");
        BackgroundRepeat repeatX = BackgroundRepeat.NO_REPEAT;
        finalColor.fillProperty().bind(currentColor);
        Rectangle rectangle = new Rectangle(256, 256);
        rectangle.setFill(new ImagePattern(colorsImage));
        circle.setStroke(Color.WHITE);
        circle.setManaged(false);
        StackPane pane = new StackPane(rectangle, circle);
        pane.setOnMousePressed(e -> updateColor(colorsImage, e));
        pane.setOnMouseDragged(e -> updateColor(colorsImage, e));
        root.getChildren().add(pane);
        root.getChildren().addAll(mainHueslider);
        root.getChildren().addAll(new VBox(finalColor, buildTabs(), sliderOptions("Opacity", opacitySlider, PERCENT_FORMAT, 100)));
        drawImage();
        simpleSliderBuilder.onChange((a, b, c) -> drawImage());
        Scene value = new Scene(root, 600, 300);
        primaryStage.setScene(value);
        primaryStage.show();
        mainHueslider.prefHeightProperty().bind(rectangle.widthProperty());
        WritableImage image = newSliderBackground(mainHueslider);
        mainHueslider.setBackground(new Background(new BackgroundImage(image, repeatX, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        currentColor.addListener((o, old, newV) -> {
            if (Objects.equals(old, newV)) {
                return;
            }
            changeIfDifferent(saturationSlider, newV.getSaturation());
            changeIfDifferent(brightnessSlider, newV.getBrightness());
            changeIfDifferent(mainHueslider, newV.getHue());
            changeIfDifferent(hueSlider, newV.getHue());
            changeIfDifferent(redSlider, newV.getRed());
            changeIfDifferent(greenSlider, newV.getGreen());
            changeIfDifferent(blueSlider, newV.getBlue());
            changeIfDifferent(opacitySlider, newV.getOpacity());
            circle.setCenterX(newV.getSaturation() * 255.0);
            circle.setCenterY(255 * (1 - newV.getBrightness()));
        });

        value.getStylesheets().add(ResourceFXUtils.toExternalForm("colorChooser.css"));

    }

    private TabPane buildTabs() {
        return new SimpleTabPaneBuilder().addTab("HSB", hsbSliders()).addTab("RGB", rgbSliders()).allClosable(false)
                .build();
    }

    private void changeIfDifferent(Slider slider,double saturation) {
        if (Math.abs(slider.getValue() - saturation) > 0) {
            slider.setValue(saturation);
        }
        slider.setValueChanging(true);
    }

    private void drawImage() {
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                colorsImage.getPixelWriter().setColor(x, y,
                        Color.hsb(mainHueslider.getValue(), x / 255.0, (255 - y) / 255.0, opacitySlider.getValue()));
            }
        }
    }
    private VBox hsbSliders() {
        VBox vBox = new VBox();
        vBox.getChildren().add(sliderOptions("Hue", hueSlider, "%.0f°", 1));
        vBox.getChildren().add(sliderOptions("Saturation", saturationSlider, PERCENT_FORMAT, 100));
        vBox.getChildren().add(sliderOptions("Brightness", brightnessSlider, PERCENT_FORMAT, 100));
        return vBox;
    }

    private WritableImage newSliderBackground(Slider slider) {
        double height = slider.getPrefHeight();
        WritableImage image = new WritableImage(20, (int) height);

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(x, y,
                        Color.hsb((360 - y * 360 / height) % 360, 1, 1));
            }
        }
        return image;
    }


    private VBox rgbSliders() {
        VBox vBox = new VBox();
        vBox.getChildren().add(sliderOptions("Red", redSlider, PERCENT_FORMAT, 100));
        vBox.getChildren().add(sliderOptions("Green", greenSlider, PERCENT_FORMAT, 100));
        vBox.getChildren().add(sliderOptions("Blue", blueSlider, PERCENT_FORMAT, 100));
        return vBox;
    }

    private HBox sliderOptions(String field,Slider slider ,String format,int multiplier) {
        Text text = new Text();
        text.textProperty().bind(slider.valueProperty().multiply(multiplier).asString(format));
        return new HBox(new Text(field), slider, text);
    }

    private void updateColor(WritableImage writableImage, MouseEvent e) {
        double x = getWithinRange(e.getX(), 0, 255);
        circle.setCenterX(x);
        double y = getWithinRange(e.getY(), 0, 255);
        circle.setCenterY(y);
        Color color = writableImage.getPixelReader().getColor((int)x, (int)y);
        currentColor.set(color);
    }

    public static void main(String[] args) {
        launch(args);
    }
}