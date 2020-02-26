package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import utils.PixelHelper;

public class BorderTool extends WandTool {


    @Override
    public ImageView createIcon() {
        return PaintTool.getIconByURL("Border.png");
    }

    @Override
    public WritableImage createSelectedImage(final PaintModel model) {
        PixelReader pixelReader = model.getImage().getPixelReader();
        int originalColor = pixelReader.getArgb((int) initialX, (int) initialY);
        List<Integer> toGo = new IntArrayList();
        List<Integer> nextGo = new IntArrayList();
        toGo.add(index(initialX, initialY));
        int maxTries = width * height;
        int tries = 0;
        WritableImage selectedImage = new WritableImage(width, height);
        PixelHelper pixel = new PixelHelper();
        PixelHelper newColor = new PixelHelper();
        while (!toGo.isEmpty()) {
            int next = toGo.remove(0);
            int x = x(next);
            int y = y(next);
            if (withinImage(x, y, width, height)) {
                int color = pixelReader.getArgb(x, y);
                pixel.reset(originalColor);
                if (selectedImage.getPixelReader().getArgb(x, y) == 0 && closeColor(pixel, color)
                    && tries++ < maxTries) {
                    newColor.add(color);
                    if (y != 0 && y != height - 1) {
                        addIfNotIn(toGo, next + 1);
                        addIfNotIn(toGo, next - 1);
                        addIfNotIn(toGo, next + height);
                        addIfNotIn(toGo, next - height);
                    }
                    nextGo.add(next);
                    double x2 = getArea().getLayoutX();
                    double y2 = getArea().getLayoutY();
                    getArea().setLayoutX(Math.min(x, x2));
                    getArea().setLayoutY(Math.min(y, y2));
                    getArea().setWidth(Math.abs(Math.max(x, x2 + getArea().getWidth()) - getArea().getLayoutX()));
                    getArea().setHeight(Math.abs(Math.max(y, y2 + getArea().getHeight()) - getArea().getLayoutY()));
                }
            }
            if (nextGo.size() % 50 == 0) {
                paintInParallel(selectedImage.getPixelWriter(), nextGo, newColor);

            }
        }
        paintInParallel(selectedImage.getPixelWriter(), nextGo, newColor);
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
    private void paintInParallel(PixelWriter pixelWriter, List<Integer> nextGo,
        PixelHelper newColor) {
        while (!nextGo.isEmpty()) {
            Integer next0 = nextGo.remove(0);
            if (next0 == null) {
                return;
            }
            int x0 = x(next0);
            int y0 = y(next0);
            pixelWriter.setArgb(x0, y0, newColor.toArgb());
        }
    }



}
