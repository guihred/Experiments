package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.apache.poi.util.IntList;
import simplebuilder.SimpleSliderBuilder;
import utils.DrawOnPoint;
import utils.PixelHelper;
import utils.RunnableEx;

public class BucketTool extends PaintTool {

    private int width;
    private int height;

    private IntegerProperty threshold = new SimpleIntegerProperty(PixelHelper.MAX_BYTE / 20);

    private Slider thresholdSlider;

    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("Bucket.png");
    }

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMouseClicked(e, model);
        }
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseClicked(e, model);
        }
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();

        Slider slider = getThresholdSlider();
        model.getToolOptions().getChildren().add(slider);
        Text text = new Text();
        text.textProperty().bind(threshold.divide(slider.getMax()).multiply(100).asString("%.0f%%"));
        model.getToolOptions().getChildren().add(text);
    }

    public void setColor(final int initX, final int initY, final int originalColor, final int frontColor,
        final PixelReader pixelReader, final PaintModel model) {
        final IntList toGo = new IntList();
        toGo.add(index(initX, initY));
        PixelHelper pixel = new PixelHelper();
        RunnableEx.ignore(() -> {
            while (!toGo.isEmpty()) {
                int next = toGo.remove(0);
                int x = x(next);
                int y = y(next);
                if (withinImage(x, y, model.getImage())) {
                    pixel.reset(originalColor);
                    int color = pixelReader.getArgb(x, y);
                    if (color != frontColor && (color == originalColor || closeColor(pixel, color))) {
                        if (y != 0 && y != height - 1) {
                            addIfNotIn(toGo, next + 1);
                            addIfNotIn(toGo, next - 1);
                            addIfNotIn(toGo, next + width);
                            addIfNotIn(toGo, next - width);
                        }
                        model.getImage().getPixelWriter().setArgb(x, y, frontColor);
                    }
                }
            }
        });
    }

    private void addIfNotIn(final IntList toGo, final int e) {
        if (!toGo.contains(e) && e < width * height && e >= 0) {
            toGo.add(e);
        }
    }

    private boolean closeColor(final PixelHelper pixel, final int color) {
        pixel.add(color, -1);
        return pixel.modulus() < threshold.get();
    }

    private Slider getThresholdSlider() {
        if (thresholdSlider == null) {
            thresholdSlider = new SimpleSliderBuilder(0, PixelHelper.MAX_BYTE, 0).bindBidirectional(threshold)
                .maxWidth(60).build();

        }
        return thresholdSlider;
    }

    private Integer index(final int initialX2, final int initialY2) {
        return initialX2 + initialY2 * width;
    }

    private void onMouseClicked(final MouseEvent e, final PaintModel model) {
        int initialX = (int) DrawOnPoint.getWithinRange(e.getX(), 0, model.getImage().getWidth() - 1);
        int initialY = (int) DrawOnPoint.getWithinRange(e.getY(), 0, model.getImage().getHeight() - 1);
        width = (int) model.getImage().getWidth();
        height = (int) model.getImage().getHeight();
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb(initialX, initialY);
        int frontColor = PixelHelper
            .toArgb(e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor());
        if (originalColor != frontColor) {
            Platform.runLater(() -> setColor(initialX, initialY, originalColor, frontColor, pixelReader, model));
        }
    }

    private int x(final int m) {
        return m % width;
    }

    private int y(final int m) {

        return m / width;
    }

}