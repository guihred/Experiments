package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;
import static utils.DrawOnPoint.within;
import static utils.DrawOnPoint.withinImage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import simplebuilder.SimpleSliderBuilder;
import utils.PixelHelper;

public class BorderTool extends PaintTool {

    private IntegerProperty length = new SimpleIntegerProperty(PixelHelper.MAX_BYTE / 20);
    private Slider lengthSlider;

    @Override
    public ImageView createIcon() {
        return PaintTool.getIconByURL("Border.png");

    }

    public void createSelectedImage(int initialX, int initialY, final PaintModel model) {
        PixelReader pixelReader = model.getImage().getPixelReader();
        PixelWriter pixelWriter = model.getImage().getPixelWriter();
        int originalColor = pixelReader.getArgb(initialX, initialY);
        final int width = (int) model.getImage().getWidth();
        final int height = (int) model.getImage().getHeight();
        List<Integer> toGo = new IntArrayList();
        List<Integer> nextGo = new IntArrayList();
        toGo.add(index(initialX, initialY, height));
        int maxTries = width * height;
        int tries = 0;
        PixelHelper pixel = new PixelHelper();
        PixelHelper newColor = new PixelHelper();
        while (!toGo.isEmpty()) {
            int next = toGo.remove(0);
            int x = x(next, height);
            int y = y(next, height);
            if (withinImage(x, y, width, height)) {
                int color = pixelReader.getArgb(x, y);
                pixel.reset(originalColor);
                if (closeColor(pixel, color) && tries++ < maxTries) {
                    newColor.add(color);
                    if (y != 0 && y != height - 1) {
                        addIfNotIn(toGo, next + 1, width, height);
                        addIfNotIn(toGo, next - 1, width, height);
                        addIfNotIn(toGo, next + height, width, height);
                        addIfNotIn(toGo, next - height, width, height);
                    }
                    nextGo.add(next);
                }
            }
            if (nextGo.size() % 50 == 0) {
                paintInParallel(pixelWriter, height, nextGo, newColor);
            }
        }
        paintInParallel(pixelWriter, height, nextGo, newColor);
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        PaintTool.handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren().add(getLengthSlider());
        Text text = new Text();
        text.textProperty().bind(length.divide(getLengthSlider().getMax()).multiply(100).asString("%.0f%%"));
        model.getToolOptions().getChildren().add(text);
    }

    @Override
    protected void onMouseDragged(MouseEvent e, PaintModel model) {
        super.onMousePressed(e, model);
    }

    @Override
    protected void onMousePressed(MouseEvent e, PaintModel model) {
        double x = getWithinRange(e.getX(), 0, model.getImage().getWidth());
        double y = getWithinRange(e.getY(), 0, model.getImage().getHeight());
        createSelectedImage((int) x, (int) y, model);
    }

    private boolean closeColor(final PixelHelper pixel, final int color) {
        pixel.add(color, -1);
        return pixel.modulus() < length.get();
    }

    private Slider getLengthSlider() {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(0, PixelHelper.MAX_BYTE, 0).bindBidirectional(length).maxWidth(60)
                .build();
        }
        return lengthSlider;
    }

    private static void addIfNotIn(final List<Integer> toGo, final int e, final int width, final int height) {
        if (within(e, (double) width * height) && !toGo.contains(e)) {
            toGo.add(e);
        }
    }

    private static int index(final int initialX2, final int initialY2, final int height) {
        return initialX2 * height + initialY2;
    }

    private static void paintInParallel(PixelWriter pixelWriter, final int height, List<Integer> nextGo,
        PixelHelper newColor) {
        while (!nextGo.isEmpty()) {
            Integer next0 = nextGo.remove(0);
            if (next0 == null) {
                return;
            }
            int x0 = x(next0, height);
            int y0 = y(next0, height);
            pixelWriter.setColor(x0, y0, newColor.toColor());
        }
    }

    private static int x(final int m, final int height) {
        return m / height;
    }

    private static int y(final int m, final int height) {

        return m % height;
    }

}
