package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import simplebuilder.SimpleSliderBuilder;
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
        Image clipboardImage = PaintToolHelper.getClipboardImage();
        if (clipboardImage != null) {
            patternImage = clipboardImage;
        }
        if (patternImage == null) {
            return null;
        }
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb((int) initialX, (int) initialY);
        WritableImage selectedImage = new WritableImage(width, height);

        List<Integer> toGo = new IntArrayList();
        toGo.add(index((int) initialX, (int) initialY));
        int maxTries = width * height;
        int tries = 0;
        int selectedWidth = (int) patternImage.getWidth();
        int selectedHeight = (int) patternImage.getHeight();
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
                    int patternX = (int) Math.round(x / scale.get()) % selectedWidth;
                    int patternY = (int) Math.round(y / scale.get()) % selectedHeight;
                    int argb = patternImage.getPixelReader().getArgb(patternX, patternY);
                    selectedImage.getPixelWriter().setArgb(x, y, argb);

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
        RectBuilder.build().startX(x).startY(y).width(width2).height(height2).copyImagePart(selectedImage,
            writableImage, Color.TRANSPARENT);
        return writableImage;

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
            SimpleSliderBuilder.onChange(getScaleSlider(), (observable, oldValue, newValue) -> onChangeSlider(model));
        }

        Slider slider = getScaleSlider();
        Text text = new Text();
        text.textProperty().bind(scale.multiply(100).asString("Scale %.0f%%"));
        model.getToolOptions().getChildren().add(text);
        model.getToolOptions().getChildren().add(slider);

    }

    @Override
    protected void onChangeSlider(final PaintModel model) {
        if (model.getImageStack().getChildren().contains(getArea()) && imageSelected != null) {
            Platform.runLater(() -> {
                imageSelected = null;
                getArea().setWidth(1);
                getArea().setHeight(1);
                WritableImage writableImage = createSelectedImage(model);
                addRect(model);

                onMouseReleased(model);
                setImageSelected(writableImage);
                getArea().setFill(new ImagePattern(writableImage));
                if (!model.getImageStack().getChildren().contains(getArea())) {
                    model.getImageStack().getChildren().add(getArea());
                }
            });
        }
    }

    private Slider getScaleSlider() {
        if (scaleSlider == null) {
            scaleSlider = new SimpleSliderBuilder(0.1, 10, 1).bindBidirectional(scale).maxWidth(60).build();
        }
        return scaleSlider;
    }

}