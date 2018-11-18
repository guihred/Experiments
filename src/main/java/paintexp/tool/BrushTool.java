package paintexp.tool;

import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.ResourceFXUtils;

public class BrushTool extends PaintTool {

    private ImageView icon;

    boolean pressed;

    private int y;

    private int x;

    private BrushOption option = BrushOption.CIRCLE;

    private Property<Number> length = new SimpleIntegerProperty(10);

	@Override
	public Node getIcon() {
		if (icon == null) {
            icon = new ImageView(ResourceFXUtils.toExternalForm("brush.png"));
            icon.setPreserveRatio(true);
            icon.setFitWidth(10);
            icon.maxWidth(10);
            icon.maxHeight(10);
		}
		return icon;
	}

    @Override
	public Cursor getMouseCursor() {
        return Cursor.NONE;
	}
	@Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
			onMousePressed(e, model);
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			onMouseDragged(e, model);
		}
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            pressed = false;
        }

	}

	@Override
	public void onSelected(PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
                .addToggle(new Circle(5), BrushOption.CIRCLE)
                .addToggle(new Rectangle(10, 10), BrushOption.SQUARE)
                .addToggle(new Line(0, 10, 10, 0), BrushOption.LINE_NW_SE)
                .addToggle(new Line(0, 0, 10, 10), BrushOption.LINE_SW_NE)
                .onChange((v, old, newV) -> option = (BrushOption) newV.getUserData())
                .select(0)
                .getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);
	
	}

    private void drawUponOption(final PaintModel model, int x2, int y2) {

        if (withinRange(x2, y2, model)) {
            double r = length.getValue().doubleValue();
            switch (option) {
                case CIRCLE:
                    for (int i = 1; i <= r; i++) {
                        drawCircle(model, x2, y2, i, i, i <= 5 ? 110 : 1.5 * i * i);
                    }
                    break;
                case SQUARE:
                    drawSquare(model, x2, y2, (int) r, model.getFrontColor());
                    break;
                case LINE_SW_NE:
                    drawLine(model, x2, y2, x2 + r, y2 + r);
                    break;
                case LINE_NW_SE:
                    drawLine(model, x2, y2, x2 + r, y2 - r);
                    break;
                default:
                    break;
            }
        }
    }

    private void onMouseDragged(final MouseEvent e, final PaintModel model) {
		int y2 = (int) e.getY();
		int x2 = (int) e.getX();
		if (pressed && withinRange(x2, y2, model)) {
            drawLine(model, x, y, x2, y2, (x3, y3) -> drawUponOption(model, x3, y3));

			y = (int) e.getY();
			x = (int) e.getX();
		}
	}

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
        y = (int) e.getY();
        x = (int) e.getX();
        drawUponOption(model, x, y);
        pressed = true;
    }

    enum BrushOption {
        SQUARE,
        CIRCLE,
        LINE_SW_NE,
        LINE_NW_SE;
    }
}