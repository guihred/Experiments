package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import simplebuilder.SimpleSliderBuilder;
import utils.CustomList;
import utils.ImageFXUtils;
import utils.PixelHelper;

public class PatternTool extends WandTool {
    private Image patternImage;
    private Slider scaleSlider;
    private DoubleProperty scale = new SimpleDoubleProperty(1);

    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("pattern.png");
    }

    @Override
    public WritableImage createSelectedImage(final PaintModel model) {
        if (imageSelected != null) {
            return imageSelected;
        }
        Image clipboardImage = ImageFXUtils.getClipboardImage();
        if (clipboardImage != null) {
            patternImage = clipboardImage;
        }
        if (patternImage == null) {
            return null;
        }
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb((int) initialX, (int) initialY);
        WritableImage selectedImage = new WritableImage(width, height);

        List<Integer> toGo = new CustomList<>();
        toGo.add(index(initialX, initialY));
        int tries = 0;
        int selectedWidth = (int) patternImage.getWidth();
        int selectedHeight = (int) patternImage.getHeight();
        PixelHelper pixel = new PixelHelper(originalColor);
        while (!toGo.isEmpty()) {
            Integer next = toGo.get(0);
            int x = x(next);
            int y = y(next);
            if (withinImage(x, y, model.getImage())) {
                int color = pixelReader.getArgb(x, y);
                if (isCloseColor(selectedImage, pixel, x, y, color, tries++)) {
                    addNeighbors(toGo, next, y);
                    int patternX = (int) Math.round(x / scale.get()) % selectedWidth;
                    int patternY = (int) Math.round(y / scale.get()) % selectedHeight;
                    Color argb = patternImage.getPixelReader().getColor(patternX, patternY);

                    selectedImage.getPixelWriter().setColor(x, y, argb);
                    adjustArea(x, y);
                }
            }
            toGo.remove(0);
        }
        return cutArea(selectedImage);

    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
        if (scaleSlider.isFocused() && exceptionKeys.contains(e.getCode())) {
            PaintTool.handleSlider(e, scale, scaleSlider);
            return;
        }
        super.handleKeyEvent(e, model);
    }

    @Override
    public void onSelected(PaintModel model) {
        super.onSelected(model);
        if (scaleSlider == null) {
            SimpleSliderBuilder.onChange(getScaleSlider(), (ob, old, val) -> onChangeSlider(model));
        }
        addSlider(model, "Scale", getScaleSlider(), scale.multiply(10));
    }


    @Override
    protected void repositionImage(final PaintModel model) {
        imageSelected = null;
    }

    private Slider getScaleSlider() {
        if (scaleSlider == null) {
            scaleSlider = new SimpleSliderBuilder(1. / 10, 10, 1).bindBidirectional(scale).maxWidth(60).build();
        }
        return scaleSlider;
    }

}