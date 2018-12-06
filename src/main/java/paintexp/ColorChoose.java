package paintexp;

import static paintexp.tool.DrawOnPoint.getWithinRange;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ColorChoose extends Application {
    ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.WHITE);
    Rectangle finalColor = new Rectangle(50, 50);
    Runnable onUse;
    Runnable onSave;
    Circle circle = new Circle(2, Color.BLACK);

    @Override
    public void start(Stage primaryStage) throws Exception {
        FlowPane root = new FlowPane();
        Slider slider = new Slider(0, 360, 0);
        slider.setRotate(90);
        BackgroundRepeat repeatX = BackgroundRepeat.NO_REPEAT;

        finalColor.fillProperty().bind(currentColor);
        Rectangle rectangle = new Rectangle(256, 256);
        WritableImage writableImage = new WritableImage(256, 256);
        rectangle.setFill(new ImagePattern(writableImage));
        drawImage(slider, writableImage);
        slider.valueProperty().addListener(e -> drawImage(slider, writableImage));

        circle.setStroke(Color.WHITE);
        circle.setManaged(false);
        StackPane pane = new StackPane(rectangle, circle);
        pane.setOnMousePressed(e -> updateColor(writableImage, e));
        pane.setOnMouseDragged(e -> updateColor(writableImage, e));
        root.getChildren().add(pane);
        root.getChildren().add(new VBox(finalColor, slider));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        WritableImage image = newSliderBackground();
        slider.setBackground(new Background(new BackgroundImage(image, repeatX, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
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

    private WritableImage newSliderBackground() {
        WritableImage image = new WritableImage(20, 360);
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 360; y++) {
                image.getPixelWriter().setColor(x, y,
                        Color.hsb(y, 1, 1));
            }
        }
        return image;
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