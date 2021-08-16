package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import simplebuilder.SimpleAnimationBuilder;
import simplebuilder.SimpleSliderBuilder;
import utils.PixelHelper;
import utils.RectBuilder;
import utils.ex.HasLogging;

public class SprayTool extends PaintTool {

    private static final Logger LOG = HasLogging.log();
    private int centerX;
    private int centerY;
    private final IntegerProperty length = new SimpleIntegerProperty(10);
    private boolean pressed;
    private int frontColor;
    private PixelReader pixelReader;
    private PaintModel paintModel;
    private final AnimationTimer animationTimer = SimpleAnimationBuilder.timer(now -> drawPoints());
    private Slider lengthSlider;

    @Override
    public Node createIcon() {
        return PaintTool.getIconByURL("spray.png");
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
        PaintTool.handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
        centerX = (int) e.getX();
        centerY = (int) e.getY();
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        centerX = (int) e.getX();
        centerY = (int) e.getY();
        pressed = true;
        paintModel = model;
        frontColor = PixelHelper.toArgb(model.getFrontColor());
        pixelReader = model.getImage().getPixelReader();
        animationTimer.start();
    }

    @Override
    public void onMouseReleased(final PaintModel model) {
        pressed = false;
        animationTimer.stop();
    }

    @Override
    public void onSelected(final PaintModel model) {
        addSlider(model, "Length", getLengthSlider(), length);
    }

    private void drawPoints() {
        if (!pressed) {
            return;
        }
        int argb = frontColor;
        int nTries = 0;
        do {
            double radius = rnd(length.get());
            double t = rnd(2 * Math.PI);
            int x = (int) Math.round(radius * Math.cos(t));
            int y = (int) Math.round(radius * Math.sin(t));
            if (withinImage(x + centerX, y + centerY, paintModel.getImage())) {
                argb = pixelReader.getArgb(x + centerX, y + centerY);
            }
            try {
                RectBuilder.drawPoint(paintModel.getImage(), x + centerX, y + centerY, paintModel.getFrontColor());
            } catch (Exception e) {
                LOG.trace("", e);
            }
        } while (argb != frontColor && nTries++ < 10);
    }

    private Slider getLengthSlider() {
        if (lengthSlider == null) {
            final int max = 50;
            lengthSlider = new SimpleSliderBuilder(1, max, 10).bindBidirectional(length).prefWidth(150).build();
        }
        return lengthSlider;
    }

    private static double rnd(double i) {
        return Math.random() * i;
    }

}