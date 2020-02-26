package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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
        int tries = 0;
        WritableImage selectedImage = new WritableImage(width, height);
        PixelHelper pixel = new PixelHelper(originalColor);
        PixelHelper newColor = new PixelHelper();
        while (!toGo.isEmpty()) {
            int next = toGo.remove(0);
            int x = x(next);
            int y = y(next);
            if (withinImage(x, y, width, height)) {
                int color = pixelReader.getArgb(x, y);
                if (isCloseColor(selectedImage, pixel, x, y, color, tries++)) {
                    newColor.add(color);
                    addNeighbors(toGo, next, y);
                    nextGo.add(next);
                    adjustArea(x, y);
                }
            }
            if (nextGo.size() % 50 == 0) {
                paintInParallel(selectedImage.getPixelWriter(), nextGo, newColor);
            }
        }
        paintInParallel(selectedImage.getPixelWriter(), nextGo, newColor);

        return cutArea(selectedImage);

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
