package paintexp.tool;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import paintexp.SimplePixelReader;
import simplebuilder.SimpleSliderBuilder;

public class EraserTool extends PaintTool {

    private static final int MAX_ERASER_SIZE = 100;

    private ImageView icon;

    private Rectangle area;
    private IntegerProperty length = new SimpleIntegerProperty(10);

	private int lastX;
	private int lastY;

    public Rectangle getArea() {
        if (area == null) {
            area = new Rectangle(10, 10, Color.WHITE);
            area.setManaged(false);
            area.widthProperty().bind(length);
            area.heightProperty().bind(length);
            area.setStroke(Color.BLACK);
        }
        return area;
    }

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = getIconByURL("eraser.png");
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.NONE;
    }

    @Override
    public synchronized void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
            onMouseMoved(e, model);
        }
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
			onMousePressed(e, model);
		}
		if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
			onMouseDragged(e, model);
        }
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
        if (MouseEvent.MOUSE_EXITED.equals(eventType)) {
            getArea().setVisible(false);
        }
        if (MouseEvent.MOUSE_ENTERED.equals(eventType)) {
            getArea().setVisible(true);
        }
    }

	@Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.PLUS) {
            length.setValue(Integer.min(MAX_ERASER_SIZE,length.getValue()+1));
        }
        if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
            length.setValue(Integer.max(1, length.getValue() - 1));
        }
    
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, MAX_ERASER_SIZE, 10).bindBidirectional(length).prefWidth(50).build());
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
    	int w = (int) getArea().getWidth();
    	drawLine(model, lastX, lastY, e.getX(), e.getY(), (x,y)->{
    		if (e.getButton() == MouseButton.PRIMARY) {
    			drawSquare(model, x, y, w, model.getBackColor());
    		} else {
    			drawSquare(model, x, y, w, SimplePixelReader.toArgb(model.getFrontColor()));
    		}	
		});
		getArea().setLayoutX(e.getX());
		getArea().setLayoutY(e.getY());
		lastX = (int) e.getX();
		lastY = (int) e.getY();
    }

	@Override
	protected void onMousePressed(final MouseEvent e, final PaintModel model) {
		int y = (int) e.getY();
		int x = (int) e.getX();
		int w = (int) getArea().getWidth();
		if (e.getButton() == MouseButton.PRIMARY) {
			drawSquare(model, x, y, w, model.getBackColor());
		} else {
			drawSquare(model, x, y, w, SimplePixelReader.toArgb(model.getFrontColor()));
		}
		getArea().setLayoutX(e.getX());
		getArea().setLayoutY(e.getY());
		lastX = x;
		lastY = y;
	}

    private void onMouseMoved(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }
        getArea().setFill(model.getBackColor());
        Color invert = model.getBackColor().invert();
        Color color = new Color(invert.getRed(), invert.getGreen(), invert.getBlue(), 1);
        getArea().setStroke(color);
        getArea().setLayoutX(e.getX());
        getArea().setLayoutY(e.getY());
    }

}