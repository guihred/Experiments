package paintexp.tool;

import static paintexp.tool.DrawOnPoint.withinRange;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class BrushTool extends PaintTool {

    private ImageView icon;

    private boolean pressed;

    private int y;

    private int x;

    private BrushOption option = BrushOption.CIRCLE;

    private IntegerProperty length = new SimpleIntegerProperty(10);

    private Map<BrushOption, Node> mouseCursor;

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = getIconByURL("brush.png");
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.NONE;
    }

    public Map<BrushOption, Node> getMouseCursorMap() {
        if (mouseCursor == null) {
            mouseCursor = new EnumMap<>(BrushOption.class);
            Circle circle = new Circle(10);
            circle.radiusProperty().bind(length);
            mouseCursor.put(BrushOption.CIRCLE, circle);
            Rectangle square = new Rectangle(10, 10);
            square.widthProperty().bind(length);
            square.heightProperty().bind(length);
            mouseCursor.put(BrushOption.SQUARE, square);
            Line line1 = new Line(0, 10, 10, 0);
            line1.startYProperty().bind(length);
            line1.endXProperty().bind(length);
            mouseCursor.put(BrushOption.LINE_SW_NE, line1);
            Line line2 = new Line(0, 0, 10, 10);
            line2.endXProperty().bind(length);
            line2.endYProperty().bind(length);
            mouseCursor.put(BrushOption.LINE_NW_SE, line2);
            mouseCursor.values().forEach(n -> n.setManaged(false));
        }

        return mouseCursor;
    }

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_ENTERED.equals(eventType)) {
            onMouseEntered(model);
        }
        if (MouseEvent.MOUSE_EXITED.equals(eventType)) {
            getMouseCursorMap().values().forEach(n -> n.setVisible(false));
        }
        if (MouseEvent.MOUSE_MOVED.equals(eventType)) {
            onMouseMoved(e);
        }
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
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());
        List<Node> togglesAs = new SimpleToggleGroupBuilder().addToggle(new Circle(5), BrushOption.CIRCLE)
                .addToggle(new Rectangle(10, 10), BrushOption.SQUARE)
                .addToggle(new Line(0, 0, 10, 10), BrushOption.LINE_NW_SE)
                .addToggle(new Line(0, 10, 10, 0), BrushOption.LINE_SW_NE)
                .onChange(
                        (v, old, newV) -> option = newV == null ? BrushOption.CIRCLE : (BrushOption) newV.getUserData())
                .select(option).getTogglesAs(Node.class);

        model.getToolOptions().getChildren().addAll(togglesAs);

    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        if (pressed && withinRange(x2, y2, model)) {
            drawLine(model, x, y, x2, y2, (x3, y3) -> drawUponOption(e, model, x3, y3, false));

            y = (int) e.getY();
            x = (int) e.getX();
        }
        onMouseMoved(e);
    }

    protected void onMouseMoved(final MouseEvent e) {
        getMouseCursorMap().get(option).setLayoutX(e.getX());
        getMouseCursorMap().get(option).setLayoutY(e.getY());
        if (option == BrushOption.LINE_SW_NE) {
            getMouseCursorMap().get(option).setLayoutY(e.getY() - length.get());
        }
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        y = (int) e.getY();
        x = (int) e.getX();
        drawUponOption(e, model, x, y, true);
        pressed = true;
    }

    @Override
    protected void onMouseReleased(PaintModel model) {
        pressed = false;
        model.createImageVersion();
    }

    private void drawUponOption(final MouseEvent e, final PaintModel model, final int x2, final int y2, boolean fill) {

        if (withinRange(x2, y2, model)) {
            double r = length.getValue().doubleValue();
            Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
            switch (option) {
                case CIRCLE:
                    drawCircle(model, x2, y2, r, r, color);
                    if (fill) {
                        fillCircle(model, x2, y2, r, color);
                    }
                    break;
                case SQUARE:
                    drawSquareLine(model, x2, y2, (int) r, color);
                    if (fill) {
                        drawSquare(model, x2, y2, (int) r, color);
                    }
                    break;
                case LINE_NW_SE:
                    drawLine(model, x2, y2, x2 + r, y2 + r, color);
                    break;
                case LINE_SW_NE:
                    drawLine(model, x2, y2, x2 + r, y2 - r, color);
                    break;
                default:
                    break;
            }
        }
    }

    private void fillCircle(final PaintModel model, final int x2, final int y2, double r, Color color) {
        drawPoint(model, x2, y2, color);
        drawCircle(model, x2, y2, r, r - 1, color);
        for (double i = 1; i < r; i++) {
            drawCircle(model, x2, y2, i, i, color);
        }
    }

    private void onMouseEntered(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        children.removeAll(getMouseCursorMap().values());
        children.addAll(getMouseCursorMap().values());
        getMouseCursorMap().values().forEach(n -> n.setVisible(false));
        getMouseCursorMap().get(option).setVisible(true);
    }

    enum BrushOption {
        SQUARE,
        CIRCLE,
        LINE_SW_NE,
        LINE_NW_SE;
    }
}