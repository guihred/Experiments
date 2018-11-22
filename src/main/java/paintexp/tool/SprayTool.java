package paintexp.tool;

import java.util.Random;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class SprayTool extends PaintTool {

    private static final Logger LOG = HasLogging.log();

    private ImageView icon;

    private int centerX;
    private int centerY;
    private IntegerProperty length = new SimpleIntegerProperty(10);

    private boolean pressed;

	private    Random random = new Random();

    @Override
	public Node getIcon() {
		if (icon == null) {
            icon = new ImageView(ResourceFXUtils.toExternalForm("spray.png"));
            icon.setPreserveRatio(true);
            icon.setFitWidth(10);
            icon.maxWidth(10);
            icon.maxHeight(10);

		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
        return Cursor.DEFAULT;
	}

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMousePressed(e, model);
        }
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged(e);
		}
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            pressed = false;
        }

	}

    @Override
	public void onSelected(final PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());

	}

    private void onMouseDragged(final MouseEvent e) {
        centerX = (int) e.getX();
        centerY = (int) e.getY();
    }

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
        centerX = (int) e.getX();
        centerY = (int) e.getY();
        pressed = true;
        new Thread(()->{
            while (pressed) {
                int radius = random.nextInt(length.get());
                double t = Math.random() * 2 * Math.PI;
                int x = (int) Math.round(radius * Math.cos(t));
                int y = (int) Math.round(radius * Math.sin(t));
                drawPoint(model, x + centerX, y + centerY);
                tryToSleep();
            }
        }).start();
    }

    private void tryToSleep() {
        try {
            Thread.sleep(10);
        } catch (Exception e1) {
            LOG.trace("Whatever", e1);
        }
    }

}