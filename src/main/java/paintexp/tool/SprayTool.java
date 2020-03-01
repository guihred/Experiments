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
import simplebuilder.SimpleSliderBuilder;
import utils.HasLogging;
import utils.PixelHelper;

public class SprayTool extends PaintTool {

    private static final Logger LOG = HasLogging.log();
    private int centerX;
    private int centerY;
    private IntegerProperty length = new SimpleIntegerProperty(10);
    private boolean pressed;
    private int frontColor;
    private PixelReader pixelReader;
    private PaintModel paintModel;
    private AnimationTimer animationTimer = new AnimationTimer() {
        @Override
        public void handle(final long currentNanoTime) {
            drawPoints();
        }
    };
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
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren().add(getLengthSlider());

    }

    protected void drawPoints() {
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

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        centerX = (int) e.getX();
        centerY = (int) e.getY();
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        centerX = (int) e.getX();
        centerY = (int) e.getY();
        pressed = true;
        paintModel = model;
        frontColor = PixelHelper.toArgb(model.getFrontColor());
        pixelReader = model.getImage().getPixelReader();
        animationTimer.start();
    }

    @Override
    protected void onMouseReleased(final PaintModel model) {
        pressed = false;
        animationTimer.stop();
    }

    private Slider getLengthSlider() {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build();
        }
        return lengthSlider;
    }

    protected static double rnd(double i) {
        return Math.random() * i;
    }

}