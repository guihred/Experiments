package paintexp.tool;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import paintexp.PaintModel;
import paintexp.SimplePixelReader;
import simplebuilder.SimpleSliderBuilder;
import utils.HasLogging;

public class SprayTool extends PaintTool {

    private static final Logger LOG = HasLogging.log();
    private ImageView icon;
    private int centerX;
    private int centerY;
    private IntegerProperty length = new SimpleIntegerProperty(10);
    private boolean pressed;
    private Random random = new Random();
	private int frontColor;
	private PixelReader pixelReader;
	private PaintModel paintModel;
	private AnimationTimer animationTimer = new AnimationTimer() {
		@Override
		public void handle(final long currentNanoTime) {
			if (!pressed) {
				return;
			}
			int argb = frontColor;
			int nTries = 0;
			do {
				int radius = random.nextInt(length.get());
				double t = Math.random() * 2 * Math.PI;
				int x = (int) Math.round(radius * Math.cos(t));
				int y = (int) Math.round(radius * Math.sin(t));
				if (withinRange(x + centerX, y + centerY, paintModel)) {
					argb = pixelReader.getArgb(x + centerX, y + centerY);
				}
				try {
					drawPoint(paintModel, x + centerX, y + centerY);
				} catch (Exception e) {
					LOG.trace("", e);
				}
			} while (argb != frontColor && nTries++ < 10);

		}
	};


    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = getIconByURL("spray.png");
        }
        return icon;
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());

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
		frontColor = SimplePixelReader.toArgb(model.getFrontColor());
		pixelReader = model.getImage().getPixelReader();
		animationTimer.start();
    }

    @Override
	protected void onMouseReleased(final PaintModel model) {
		pressed = false;
		animationTimer.stop();
    }

}