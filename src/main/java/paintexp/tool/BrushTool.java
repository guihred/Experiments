package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class BrushTool extends PaintTool {

    private boolean pressed;

    private int y;

    private int x;

    private BrushOption option = BrushOption.CIRCLE;

    private IntegerProperty length = new SimpleIntegerProperty(10);

    private Map<BrushOption, Shape> mouseCursor;

    private Slider lengthSlider;

    private DoubleProperty opacity = new SimpleDoubleProperty(1);

    @Override
    public Node createIcon() {
        return getIconByURL("brush.png");
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.NONE;
    }

    public Map<BrushOption, Shape> getMouseCursorMap() {
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
    public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
        handleSlider(e, length, lengthSlider);
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren().add(new Text("Length"));
        model.getToolOptions().getChildren().add(getLengthSlider());
        final Property<Number> radius = opacity;
        model.getToolOptions().getChildren().add(SimpleSliderBuilder.newSlider("Opacity", 0, 1, radius));
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
            .addToggle(new Circle(5), BrushOption.CIRCLE)
            .addToggle(new Rectangle(10, 10), BrushOption.SQUARE)
            .addToggle(new Line(0, 0, 10, 10), BrushOption.LINE_NW_SE)
            .addToggle(new Line(0, 10, 10, 0), BrushOption.LINE_SW_NE)
            .onChange((v, old, newV) -> option = newV == null ? BrushOption.CIRCLE : (BrushOption) newV.getUserData())
            .select(option).getTogglesAs(Node.class);

        model.getToolOptions().getChildren().addAll(togglesAs);

    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        final int x1 = x2;
        final int y1 = y2;
        if (pressed && withinImage(x1, y1, model.getImage())) {
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
        model.createImageVersion();
        drawUponOption(e, model, x, y, true);
        pressed = true;
    }

    @Override
    protected void onMouseReleased(final PaintModel model) {
        pressed = false;
        model.createImageVersion();
    }

    private void drawCircleOption(final PaintModel model, final int x2, final int y2, final double r, final Color color,
        final boolean fill) {
        drawCircle(model, x2, y2, r, r, color, opacity.get());
        drawCircle(model, x2, y2, r, r - 1, color, opacity.get());
        if (fill) {
            drawPointTransparency(model, x2, y2, color, opacity.get());
            for (double i = 1; i < r; i++) {
                drawCircle(model, x2, y2, i, i, color, opacity.get());
            }
        }
    }

    private void drawUponOption(final MouseEvent e, final PaintModel model, final int x2, final int y2,
        final boolean fill) {

        if (withinImage(x2, y2, model.getImage())) {
            double r = length.getValue().doubleValue();
            Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
            double op = opacity.get();
            switch (option) {
                case CIRCLE:
                    drawCircleOption(model, x2, y2, r, color, fill);
                    break;
                case SQUARE:
                    drawSquareLine(model, x2, y2, (int) r, color, op);
                    if (fill) {
                        new RectBuilder().startX(x2).startY(y2).width(r).height(r).drawRect(model, color, op);
                    }
                    break;
                case LINE_NW_SE:
                    drawLine(model, x2, y2, x2 + r, y2 + r, color, op);
                    break;
                case LINE_SW_NE:
                    drawLine(model, x2, y2, x2 + r, y2 - r, color, op);
                    break;
                default:
                    break;
            }
        }
    }

    private Slider getLengthSlider() {
        if (lengthSlider == null) {
            lengthSlider = new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build();
        }
        return lengthSlider;
    }

    private void onMouseEntered(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        children.removeAll(getMouseCursorMap().values());
        children.addAll(getMouseCursorMap().values());
        getMouseCursorMap().values().forEach(n -> n.setVisible(false));
        getMouseCursorMap().get(option).setVisible(true);
        getMouseCursorMap().get(option).setFill(model.getFrontColor());
        getMouseCursorMap().get(option).setStroke(model.getFrontColor());
        getMouseCursorMap().get(option).setOpacity(opacity.get());
    }

    enum BrushOption {
        SQUARE,
        CIRCLE,
        LINE_SW_NE,
        LINE_NW_SE;
    }
}