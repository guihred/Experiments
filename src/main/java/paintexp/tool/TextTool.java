package paintexp.tool;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;
import utils.HasLogging;

public class TextTool extends PaintTool {

    private Text icon;
    private Text text;
    private Rectangle area;
    private WritableImage textImage;
    private double initialX;
    private double initialY;

    public Rectangle getArea() {
        if (area == null) {
            area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK).cursor(Cursor.MOVE)
                    .strokeDashArray(1, 2, 1, 2).build();
        }
        return area;
    }

    @Override
    public Text getIcon() {
        if (icon == null) {
            icon = new Text("A");
            icon.setFont(Font.font("Times New Roman", FontWeight.BOLD, 18));
        }
        return icon;
    }
    @Override
    public Cursor getMouseCursor() {
        return Cursor.TEXT;
    }

    public Text getText() {
        if (text == null) {
            text = new Text();
            text.setManaged(false);
        }
        return text;
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
            onMouseReleased(model);
        }
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
        KeyCode code = e.getCode();
        EventType<KeyEvent> eventType = e.getEventType();
		HasLogging.log().info("{}={}", eventType, code);
		if (KeyEvent.KEY_RELEASED.equals(eventType)) {
            switch (code) {
                case BACK_SPACE:
					text.setText(text.getText().length() > 0 ? text.getText().substring(0, text.getText().length() - 1)
							: "");
                    break;
                case SPACE:
                    text.setText(text.getText() + " ");
                    break;
				case ENTER:
					text.setText(text.getText() + "\n");
					break;
                default:
                    text.setText(text.getText() + e.getText());
                    break;
            }
			getArea().setWidth(text.getBoundsInParent().getWidth() + 10);
			getArea().setHeight(
					(text.getText().chars().filter(c -> c == '\n').count() + 1) * (text.getFont().getSize() + 1));
        }
    }

    private void addRect(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }
        if (!children.contains(getText())) {
            children.add(getText());
        }
        area.setStroke(Color.BLACK);
        getArea().setManaged(false);
        getArea().setFill(Color.TRANSPARENT);
        getArea().setLayoutX(initialX);
        getArea().setLayoutY(initialY);
        getText().setLayoutX(initialX);
        getText().setLayoutY(initialY + text.getFont().getSize());
		getArea().setWidth((text.getFont().getSize() + 1) * text.getText().length());
        getArea().setHeight(text.getFont().getSize());
    }

    private void dragTo(final double x, final double y) {
        getArea().setLayoutX(Double.min(x, initialX));
        getArea().setLayoutY(Double.min(y, initialY));
        getArea().setWidth(Math.abs(x - initialX));
        getArea().setHeight(Math.abs(y - initialY));
    }

    private void onMouseDragged(final MouseEvent e, final PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        double width = model.getImage().getWidth();
        double height = model.getImage().getHeight();
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea()) && textImage != null) {
            getArea().setLayoutX(Double.max(x - initialX, -width / 4));
            getArea().setLayoutY(Double.max(y - initialY, -height / 4));
            return;
        }
        dragTo(setWithinRange(x, 0, width), setWithinRange(y, 0, height));
    }

    private void onMousePressed(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea())) {
            if (containsPoint(getArea(), e.getX(), e.getY())) {
                if (textImage == null) {
                    int width = (int) area.getWidth();
                    int height = (int) area.getHeight();
                    textImage = new WritableImage(width, height);
                    int layoutX = (int) area.getLayoutX();
                    int layoutY = (int) area.getLayoutY();
                    copyImagePart(model.getImage(), textImage, layoutX, layoutY, width, height);
                    getArea().setFill(new ImagePattern(textImage));
                    drawRect(model, layoutX, layoutY, width, height);
                }
                return;
            }
            if (textImage != null) {
                setIntoImage(model);
            }
        }
        initialX = e.getX();
        initialY = e.getY();
        addRect(model);

    }

    private void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getWidth() < 2 && children.contains(getArea()) && textImage != null) {

            children.remove(getArea());
        }
        area.setStroke(Color.BLUE);
    }

    private void setIntoImage(final PaintModel model) {
        int x = (int) getArea().getLayoutX();
        int y = (int) getArea().getLayoutY();
        double width = getArea().getWidth();
        double height = getArea().getHeight();
        copyImagePart(textImage, model.getImage(), 0, 0, width, height, x, y);
        textImage = null;
        model.getImageStack().getChildren().remove(getArea());
    }

}