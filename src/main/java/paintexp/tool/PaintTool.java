package paintexp.tool;

import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import utils.ResourceFXUtils;

@SuppressWarnings({ "unused", "static-method" })
public abstract class PaintTool extends Group {
    @FXML
    private Node icon;

    public PaintTool() {
        setId(getClass().getSimpleName());
        icon = createIcon();
        if (icon != null) {
            getChildren().add(icon);
            icon.setScaleX(1 / (icon.getBoundsInLocal().getWidth() / 30));
            icon.setScaleY(1 / (icon.getBoundsInLocal().getHeight() / 30));
        }
    }

    public void addSlider(final PaintModel model, String string, Slider lengthSlider2) {
        Text text = new Text();
        text.textProperty().bind(lengthSlider2.valueProperty().asString(string + " %.0f"));
        model.getToolOptions().getChildren().add(new VBox(text, lengthSlider2));
    }

    public void addSlider(final PaintModel model, String string, Slider slider, DoubleExpression prop) {
        Text text = new Text();
        text.textProperty().bind(prop.divide(slider.getMax()).multiply(100).asString(string + " %.0f%%"));
        model.getToolOptions().getChildren().add(new VBox(text, slider));
    }

    public void addSlider(final PaintModel model, String string, Slider lengthSlider2, IntegerProperty prop) {
        Text text = new Text();
        text.textProperty().bind(prop.asString(string + " %d"));
        model.getToolOptions().getChildren().add(new VBox(text, lengthSlider2));
    }

    public abstract Node createIcon();

    public Node getIcon() {
        return icon;
    }

    public Cursor getMouseCursor() {
        return Cursor.DEFAULT;
    }

    public void handleEvent(MouseEvent e, PaintModel model) {
        simpleHandleEvent(e, model);
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            model.createImageVersion();
        }
    }

    public void handleKeyEvent(KeyEvent e, PaintModel paintModel) {
        // DOES NOTHING
    }

    public void onDeselected(PaintModel model) {
        // DOES NOTHING
    }

    public void onSelected(PaintModel model) {
        // DOES NOTHING
    }

    public void onSelected(PaintTool old, PaintModel model) {
        onSelected(model);
    }

    public void setIcon(Node icon) {
        this.icon = icon;
    }

    protected void onMouseDragged(MouseEvent e, PaintModel model) {
        // DOES NOTHING

    }

    protected void onMousePressed(MouseEvent e, PaintModel model) {
        // DOES NOTHING
    }

    protected void onMouseReleased(PaintModel model) {
        model.createImageVersion();
        // DOES NOTHING
    }

    protected void simpleHandleEvent(MouseEvent e, PaintModel model) {
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

    public static ImageView getIconByURL(String src) {
        return getIconByURL(src, 30);

    }

    public static ImageView getIconByURL(String src, double width) {
        ImageView icon1 = new ImageView(ResourceFXUtils.toExternalForm("paint/" + src));
        icon1.setPreserveRatio(true);
        icon1.setFitWidth(width);
        icon1.setFitHeight(width);
        icon1.maxWidth(width);
        icon1.maxHeight(width);
        return icon1;

    }

    public static void handleSlider(KeyEvent e, Property<Number> property, Slider slider) {
        if (e.getEventType() != KeyEvent.KEY_PRESSED) {
            return;
        }

        KeyCode code = e.getCode();
        double blockIncrement = property instanceof IntegerProperty ? Math.ceil(slider.getBlockIncrement())
                : slider.getBlockIncrement();
        if (code == KeyCode.ADD || code == KeyCode.EQUALS || code == KeyCode.PLUS) {
            property.setValue(Math.min(slider.getMax(), blockIncrement + property.getValue().doubleValue()));
        }
        if (code == KeyCode.SUBTRACT || code == KeyCode.MINUS) {
            property.setValue(Math.max(slider.getMin(), property.getValue().doubleValue() - blockIncrement));
        }
    }

    public static boolean moveArea(KeyCode code, Rectangle area2) {
        switch (code) {
            case RIGHT:
                area2.setLayoutX(area2.getLayoutX() + 1);
                return true;
            case LEFT:
                area2.setLayoutX(area2.getLayoutX() - 1);
                return true;
            case DOWN:
                area2.setLayoutY(area2.getLayoutY() + 1);
                return true;
            case UP:
                area2.setLayoutY(area2.getLayoutY() - 1);
                return true;
            default:
                return false;
        }
    }

}