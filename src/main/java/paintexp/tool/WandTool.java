package paintexp.tool;

import static utils.DrawOnPoint.within;
import static utils.DrawOnPoint.withinImage;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import simplebuilder.SimpleSliderBuilder;
import utils.PixelHelper;

public class WandTool extends SelectRectTool {

    private int width;
    private int height;
    private IntegerProperty threshold = new SimpleIntegerProperty(PixelHelper.MAX_BYTE / 20);
    private Slider thresholdSlider;

    @Override
    public Node createIcon() {
        return getIconByURL("wand.png");
    }

    @Override
    public WritableImage createSelectedImage(final PaintModel model) {
        if (imageSelected != null) {
            return imageSelected;
        }
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb((int) initialX, (int) initialY);
        WritableImage selectedImage = new WritableImage(width, height);
        int backColor = PixelHelper.toArgb(model.getBackColor());

        backColor = backColor == 0 ? PixelHelper.toArgb(model.getBackColor().invert())
            : PixelHelper.toArgb(model.getBackColor());
        List<Integer> toGo = new ArrayList<>();
        toGo.add(index((int) initialX, (int) initialY));
        int maxTries = width * height;
        int tries = 0;
        PixelHelper pixel = new PixelHelper();
        while (!toGo.isEmpty()) {
            Integer next = toGo.remove(0);
            int x = x(next);
            int y = y(next);
            if (withinImage(x, y, model.getImage())) {
                int color = pixelReader.getArgb(x, y);
                pixel.reset(originalColor);
                if (closeColor(pixel, color) && selectedImage.getPixelReader().getArgb(x, y) == 0
                    && tries++ < maxTries) {
                    if (y != 0 && y != height - 1) {
                        addIfNotIn(toGo, next + 1);
                        addIfNotIn(toGo, next - 1);
                        addIfNotIn(toGo, next + height);
                        addIfNotIn(toGo, next - height);
                    }
                    selectedImage.getPixelWriter().setArgb(x, y, color);
                    model.getImage().getPixelWriter().setArgb(x, y, backColor);
                    double x2 = getArea().getLayoutX();
                    double y2 = getArea().getLayoutY();
                    getArea().setLayoutX(Math.min(x, x2));
                    getArea().setLayoutY(Math.min(y, y2));
                    getArea().setWidth(Math.abs(Math.max(x, x2 + getArea().getWidth()) - getArea().getLayoutX()));
                    getArea().setHeight(Math.abs(Math.max(y, y2 + getArea().getHeight()) - getArea().getLayoutY()));
                }
            }
        }
        int width2 = Math.max(1, (int) getArea().getWidth() + 1);
        int height2 = Math.max(1, (int) getArea().getHeight() + 1);
        WritableImage writableImage = new WritableImage(width2, height2);
        int x = (int) getArea().getLayoutX();
        int y = (int) getArea().getLayoutY();
        new RectBuilder().startX(x).startY(y).width(width2).height(height2).copyImagePart(selectedImage, writableImage,
            Color.TRANSPARENT);
        return writableImage;
    }

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (model.getImageStack().getChildren().contains(getArea()) && imageSelected != null) {
            super.handleEvent(e, model);
        } else if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMouseClicked(e, model);
        }

    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
        super.handleKeyEvent(e, model);
        handleSlider(e, threshold, thresholdSlider);
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

    public void setImage(final PaintModel model) {
        WritableImage writableImage = createSelectedImage(model);
        int width3 = Math.max(1, (int) getArea().getWidth());
        int height3 = Math.max(1, (int) getArea().getHeight());
        int x2 = (int) getArea().getLayoutX();
        int y2 = (int) getArea().getLayoutY();
        selectArea(x2, y2, width3 + x2 + 1, height3 + y2 + 1, model);
        setImageSelected(writableImage);
        getArea().setFill(new ImagePattern(writableImage));
    }

    @Override
    protected void addRect(final PaintModel model) {
        getArea().setManaged(false);
    }

    private void addIfNotIn(final List<Integer> toGo, final int e) {
        if (!toGo.contains(e) && within(e, (double) width * height)) {
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

    private int index(final int initialX2, final int initialY2) {
        return initialX2 * height + initialY2;
    }

    private void onMouseClicked(final MouseEvent e, final PaintModel model) {
        int clickedX = (int) e.getX();
        initialX = clickedX;
        int clickedY = (int) e.getY();
        initialY = clickedY;
        width = (int) model.getImage().getWidth();
        height = (int) model.getImage().getHeight();
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
            getArea().setLayoutX(clickedX);
            getArea().setLayoutY(clickedY);
            getArea().setManaged(false);
            getArea().setWidth(0);
            getArea().setHeight(0);
            Platform.runLater(() -> setImage(model));
        }

    }

    private int x(final int m) {
        return m / height;
    }

    private int y(final int m) {

        return m % height;
    }

}