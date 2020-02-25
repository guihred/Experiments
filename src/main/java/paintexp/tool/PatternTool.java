package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import utils.PixelHelper;

public class PatternTool extends WandTool {
    private Image patternImage;
    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("wand.png");
    }

    @Override
    public WritableImage createSelectedImage(final PaintModel model) {
        if (imageSelected != null) {
            return imageSelected;
        }
        if (patternImage == null) {
            patternImage = PaintToolHelper.getClipboardImage();
            if (patternImage == null) {
                return null;
            }
        }
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb((int) initialX, (int) initialY);
        WritableImage selectedImage = new WritableImage(width, height);

        List<Integer> toGo = new ArrayList<>();
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
                    int argb = patternImage.getPixelReader().getArgb(x % selectedWidth, y % selectedHeight);
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

}