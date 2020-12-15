package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;
import static utils.DrawOnPoint.withinImage;

import java.util.Arrays;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleSvgPathBuilder;
import utils.PixelHelper;
import utils.fx.RectBuilder;

public class BlurTool extends PaintTool {

    private int y;
    private int x;
    private IntegerProperty length = new SimpleIntegerProperty(10);
    private Color[] colors = new Color[length.get() * length.get() * 4];
    private Slider lengthSlider;

    @Override
    public Node createIcon() {
        return new SimpleSvgPathBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
                .content("m18 0 l-12 18 a15 15 0 1 0 24 0l-12 -18 z").build();
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        PaintTool.handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        if (withinImage(x2, y2, model.getImage())) {
            RectBuilder.build().startX(x).startY(y).endX(x2).endY(y2).drawLine(model.getImage(),
                    (x3, y3) -> drawBlur(x3, y3, model));
            y = (int) e.getY();
            x = (int) e.getX();
        }
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        y = (int) e.getY();
        x = (int) e.getX();
        drawBlur(x, y, model);
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().setSpacing(5);
        addSlider(model, "Length", getLengthSlider(), length);
        length.addListener((o, old, value) -> colors = new Color[value.intValue() * value.intValue() * 4]);

    }

    private void drawBlur(final int centerX, final int centerY, final PaintModel model) {
        final int radius = length.get();
        final int diameter = radius * 2;
        final int height = (int) model.getImage().getHeight();
        final int width = (int) model.getImage().getWidth();
        Arrays.fill(colors, null);
        PixelHelper pixel = new PixelHelper();
        for (double i = 0; i <= radius; i++) {
            double nPoints = 4 * i + 1;
            for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
                int x2 = (int) Math.round(i * Math.cos(t));
                int y2 = (int) Math.round(i * Math.sin(t));
                pixel.reset();
                if (withinImage(x2 + centerX, y2 + centerY, model.getImage())) {
                    int pix = model.getImage().getPixelReader().getArgb(x2 + centerX, y2 + centerY);
                    pixel.add(pix, 5);
                    for (int j = -1; j < 2; j += 2) {
                        int argb = model.getImage().getPixelReader().getArgb(x2 + centerX,
                                getWithinRange(y2 + j + centerY, 0, height - 1));
                        pixel.add(argb);
                        int argb2 = model.getImage().getPixelReader()
                                .getArgb(getWithinRange(x2 + j + centerX, 0, width - 1), y2 + centerY);
                        pixel.add(argb2);
                    }
                    int j = getWithinRange(x2 + radius, 0, diameter - 1);
                    int c = getWithinRange(y2 + radius, 0, diameter - 1);
                    colors[j * diameter + c] = pixel.toColor();
                }
            }
        }
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != null) {
                RectBuilder.drawPoint(model.getImage(), i / diameter - radius + centerX,
                        i % diameter - radius + centerY, colors[i]);
            }
        }
    }

    private Slider getLengthSlider() {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(150).build();
        }
        return lengthSlider;
    }
}
