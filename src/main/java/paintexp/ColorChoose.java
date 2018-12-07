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
import utils.ClassReflectionUtils;

public class ColorChoose extends Application {
    ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.WHITE);
    Rectangle finalColor = new Rectangle(50, 50);
    Runnable onUse;
    Runnable onSave;
    Circle circle = new Circle(2, Color.BLACK);

    private Slider hueSlider;
    private Slider saturationSlider;
    private Slider brightnessSlider;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FlowPane root = new FlowPane();
        SimpleSliderBuilder simpleSliderBuilder = new SimpleSliderBuilder(0, 359, 0);
        Slider 
        mainHueslider = simpleSliderBuilder.build();
        mainHueslider.setId("hueSlider");
        BackgroundRepeat repeatX = BackgroundRepeat.NO_REPEAT;
        finalColor.fillProperty().bind(currentColor);
        Rectangle rectangle = new Rectangle(256, 256);
        WritableImage writableImage = new WritableImage(256, 256);
        rectangle.setFill(new ImagePattern(writableImage));
        drawImage(mainHueslider, writableImage);
        simpleSliderBuilder.onChange((a, b, c) -> drawImage(mainHueslider, writableImage));
        circle.setStroke(Color.WHITE);
        circle.setManaged(false);
        StackPane pane = new StackPane(rectangle, circle);
        pane.setOnMousePressed(e -> updateColor(writableImage, e));
        pane.setOnMouseDragged(e -> updateColor(writableImage, e));
        root.getChildren().add(pane);
        root.getChildren().addAll(mainHueslider);
        root.getChildren().addAll(new VBox(finalColor, buildTabs()));
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
            if (saturationSlider.getValue() != newV.getSaturation()) {
                saturationSlider.setValue(newV.getSaturation());
                saturationSlider.setValueChanging(true);
            }
            if (brightnessSlider.getValue() != newV.getBrightness()) {
                brightnessSlider.setValue(newV.getBrightness());
                brightnessSlider.setValueChanging(true);
            }
            if (hueSlider.getValue() != newV.getHue()) {
                hueSlider.setValue(newV.getHue());
                hueSlider.setValueChanging(true);
            }
            if (mainHueslider.getValue() != newV.getHue()) {
                mainHueslider.setValue(newV.getHue());
                mainHueslider.setValueChanging(true);
            }
            circle.setCenterX(newV.getSaturation() * 255.0);
            circle.setCenterY(255 - newV.getBrightness() * 255.0);
        });

        ClassReflectionUtils.displayStyleClass(root);
        ClassReflectionUtils.displayCSSStyler(value, "colorChooser.css");

    }

    private HBox brightnessSlider() {
        brightnessSlider = new SimpleSliderBuilder(0, 1, 0)
                .onChange((o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(),
                        currentColor.get().getSaturation(), newV.doubleValue(), currentColor.get().getOpacity())))
                .build();

        Text text = new Text();
        text.textProperty().bind(brightnessSlider.valueProperty().multiply(100).asString("%.0f%%"));
        return new HBox(new Text("Brightness"), brightnessSlider, text);
    }

    private TabPane buildTabs() {
        return new SimpleTabPaneBuilder().addTab("HSB", hsbSliders()).allClosable(false).build();
    }
    private void drawImage(Slider slider, WritableImage writableImage) {
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                writableImage.getPixelWriter().setColor(x, y,
                        Color.hsb(slider.getValue(), x / 255.0, (255 - y) / 255.0));
            }
        }
        Color color = writableImage.getPixelReader().getColor((int) circle.getCenterX(), (int) circle.getCenterY());
        currentColor.set(color);
    }

    private VBox hsbSliders() {
        VBox vBox = new VBox();
        vBox.getChildren().add(hueSlider());
        vBox.getChildren().add(saturationSlider());
        vBox.getChildren().add(brightnessSlider());

        return vBox;
    }

    private HBox hueSlider() {
        hueSlider = new SimpleSliderBuilder(0, 359, 0)
                .onChange((o, old, newV) -> currentColor
                        .set(Color.hsb(newV.doubleValue(),
                                currentColor.get().getSaturation(),
                                currentColor.get().getBrightness(),
                                currentColor.get().getOpacity())))
                .build();
        Text text = new Text();
        text.textProperty().bind(hueSlider.valueProperty().asString("%.0f°"));
        return new HBox(new Text("Hue"), hueSlider, text);
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

    private HBox saturationSlider() {
        saturationSlider = new SimpleSliderBuilder(0, 1, 0)
                .onChange((o, old, newV) -> currentColor.set(Color.hsb(currentColor.get().getHue(), newV.doubleValue(),
                        currentColor.get().getBrightness(), currentColor.get().getOpacity())))
                .build();

        Text text = new Text();
        text.textProperty().bind(saturationSlider.valueProperty().multiply(100).asString("%.0f%%"));
        return new HBox(new Text("Saturation"), saturationSlider, text);
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