package paintexp.tool;

import static utils.DrawOnPoint.withinImage;

import java.util.EnumMap;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import utils.CommonsFX;
import utils.RectBuilder;

public class BrushTool extends PaintTool {

    private boolean pressed;

    private int y;

    private int x;

    private BrushOption option = BrushOption.CIRCLE;

    private Map<BrushOption, Shape> mouseCursor;
    @FXML
    private Slider lengthSlider;
    @FXML
    private Slider opacitySlider;
    @FXML
    private ToggleGroup optionGroup;
    @FXML
	private Shape circle;
    @FXML
	private Shape rectangle;
	@FXML
	private Shape swne;
	@FXML
	private Shape nwse;

	@Override
	public Node createIcon() {
		return PaintTool.getIconByURL("brush.png");
	}

	@Override
    public Cursor getMouseCursor() {
        return Cursor.NONE;
    }
    public Map<BrushOption, Shape> getMouseCursorMap() {
        if (mouseCursor == null) {
			mouseCursor = new EnumMap<>(BrushOption.class);
            mouseCursor.put(BrushOption.CIRCLE, circle);
			mouseCursor.put(BrushOption.SQUARE, rectangle);
			mouseCursor.put(BrushOption.LINE_SW_NE, swne);
			mouseCursor.put(BrushOption.LINE_NW_SE, nwse);
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
        PaintTool.handleSlider(e, lengthSlider.valueProperty(), lengthSlider);
    }

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
        int y2 = (int) e.getY();
        int x2 = (int) e.getX();
        if (pressed && withinImage(x2, y2, model.getImage())) {
            RectBuilder.build().startX(x).startY(y).endX(x2).endY(y2).drawLine(model.getImage(),
                (x3, y3) -> drawUponOption(e, model, x3, y3, false));

            y = (int) e.getY();
            x = (int) e.getX();
        }
        onMouseMoved(e);
    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        y = (int) e.getY();
        x = (int) e.getX();
        model.createImageVersion();
        drawUponOption(e, model, x, y, true);
        pressed = true;
    }

    @Override
    public void onMouseReleased(final PaintModel model) {
        pressed = false;
        model.createImageVersion();
    }

    @SuppressWarnings("unused")
    public void onOptionChange(ObservableValue<? extends Toggle> v, Toggle old, Toggle newV) {
        option = newV == null ? BrushOption.CIRCLE : (BrushOption) newV.getUserData();
    }

    @Override
    public void onSelected(final PaintModel model) {
		mouseCursor = null;
        model.getToolOptions().getChildren().clear();
        CommonsFX.loadRoot("BrushTool.fxml", model.getToolOptions(), this);

    }

    private void drawCircleOption(final PaintModel model, final int x2, final int y2, final double r, final Color color,
        final boolean fill) {
        RectBuilder.build().startX(x2).startY(y2).width(r).height(r).drawCircle(model.getImage(),
            model.getCurrentImage(), color, opacitySlider.valueProperty().get());
        RectBuilder.build().startX(x2).startY(y2).width(r).height(r - 1).drawCircle(model.getImage(),
            model.getCurrentImage(), color, opacitySlider.valueProperty().get());
        if (fill) {
            RectBuilder.drawPointTransparency(x2, y2, color, opacitySlider.valueProperty().get(), model.getImage(),
                model.getCurrentImage());
            for (double i = 1; i < r; i++) {
                RectBuilder.build().startX(x2).startY(y2).width(i).height(i).drawCircle(model.getImage(),
                    model.getCurrentImage(), color, opacitySlider.valueProperty().get());
            }
        }
    }

    private void drawUponOption(final MouseEvent e, final PaintModel model, final int x2, final int y2,
        final boolean fill) {

        if (withinImage(x2, y2, model.getImage())) {
            double r = lengthSlider.valueProperty().doubleValue();
            Color color = e.getButton() == MouseButton.PRIMARY ? model.getFrontColor() : model.getBackColor();
            double op = opacitySlider.valueProperty().get();
            switch (option) {
                case CIRCLE:
                    drawCircleOption(model, x2, y2, r, color, fill);
                    break;
                case SQUARE:
                    drawSquare(model, x2, y2, fill, r, color, op);
                    break;
                case LINE_NW_SE:
                    RectBuilder.build().startX(x2).startY(y2).endX(x2 + r).endY(y2 + r).drawLine(model.getImage(),
                        model.getCurrentImage(), color, op);
                    break;
                case LINE_SW_NE:
                    RectBuilder.build().startX(x2).startY(y2).endX(x2 + r).endY(y2 - r).drawLine(model.getImage(),
                        model.getCurrentImage(), color, op);
                    break;
                default:
                    break;
            }
        }
    }

    private void onMouseEntered(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        children.removeAll(getMouseCursorMap().values());
        children.addAll(getMouseCursorMap().values());
        getMouseCursorMap().values().forEach(n -> n.setVisible(false));
        getMouseCursorMap().get(option).setVisible(true);
        getMouseCursorMap().get(option).setFill(model.getFrontColor());
        getMouseCursorMap().get(option).setStroke(model.getFrontColor().invert());
        getMouseCursorMap().get(option).setOpacity(opacitySlider.valueProperty().get());
    }

    private void onMouseMoved(final MouseEvent e) {
        getMouseCursorMap().get(option).setLayoutX(e.getX());
        getMouseCursorMap().get(option).setLayoutY(e.getY());
        if (option == BrushOption.LINE_SW_NE) {
            getMouseCursorMap().get(option).setLayoutY(e.getY() - lengthSlider.valueProperty().get());
        }
    }

    private static void drawSquare(final PaintModel model, final int x2, final int y2, final boolean fill, double r,
        Color color, double op) {
        RectBuilder.drawSquareLine(model.getImage(), model.getCurrentImage(), x2, y2, (int) r, color, op);
        if (fill) {
            RectBuilder.build().startX(x2).startY(y2).width(r).height(r).drawRect(color, op, model.getImage(),
                model.getCurrentImage());
        }
    }
}