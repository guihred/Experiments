package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.FunctionEx;

public class PictureTool extends PaintTool {

    private static final int PREF_WIDTH = 65;

    private SVGPath area;
    private int initialX;
    private int initialY;
    private FillOption option = FillOption.STROKE;

    @Override
    public SVGPath createIcon() {
        PictureOption star5 = PictureOption.STAR_5;
        SVGPath icon = star5.toSVG();
        icon.setContent(star5.getPath());
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(3. / 4);
        icon.setScaleY(3. / 4);
        icon.setStroke(Color.BLACK);
        return icon;
    }

    public SVGPath getArea() {
        if (area == null) {
            area = new SVGPath();
            area.setStrokeWidth(1. / 10);
            area.setFill(Color.TRANSPARENT);
            area.setStroke(Color.BLACK);
            area.setManaged(false);
        }
        if (area.getContent() == null) {
            getArea().setContent(PictureOption.TRIANGLE.getPath());
        }
        return area;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.DISAPPEAR;
    }

    @Override
    public void handleKeyEvent(KeyEvent e, PaintModel paintModel) {
        if (e.isControlDown() && (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD)) {
            TextField button = new TextField();
            new SimpleDialogBuilder().button(button).button("New Pic", () -> area.setContent("M0,0" + button.getText()))
                    .bindWindow(paintModel.getImageStack()).displayDialog();
        }
    }

    @Override
    public void onDeselected(PaintModel model) {
        onMouseReleased(model);
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        SVGPath icon2 = createIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        SVGPath icon3 = createIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        SVGPath icon4 = createIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
        List<Node> togglesAs = new SimpleToggleGroupBuilder().addToggle(icon2, FillOption.STROKE)
                .addToggle(icon3, FillOption.FILL).addToggle(icon4, FillOption.STROKE_FILL)
                .onChange((o, old, newV) -> option =
                        FunctionEx.mapIf(newV, n -> (FillOption) n.getUserData(), FillOption.STROKE))
                .select(option).getTogglesAs(Node.class);
        model.getToolOptions().getChildren().addAll(togglesAs);
        SimpleToggleGroupBuilder picOptions = new SimpleToggleGroupBuilder();
        List<PictureOption> validOptions = Arrays.asList(PictureOption.values());
        validOptions.forEach(e -> picOptions.addToggle(e.toSVG(), e));

        HBox value = new HBox();
        value.setAlignment(Pos.CENTER);
        List<Node> allOptions = picOptions
                .onChange((o, old, newV) -> getArea().setContent(
                        FunctionEx.mapIf(newV, n -> (PictureOption) n.getUserData(), PictureOption.TRIANGLE).getPath()))
                .select(PictureOption.TRIANGLE).getTogglesAs(Node.class);
        value.getChildren().addAll(allOptions);

        value.prefHeight(PREF_WIDTH);
        model.getToolOptions().getChildren().addAll(value);
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        double x = getWithinRange(e.getX(), 0, model.getImage().getWidth());
        double y = getWithinRange(e.getY(), 0, model.getImage().getHeight());
        double intendedW = Math.max(1, Math.abs(initialX - x));
        double intendedH = Math.max(1, Math.abs(initialY - y));
        double min = Math.min(x, initialX);
        double min2 = Math.min(y, initialY);
        Bounds bounds = area.getBoundsInParent();
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        area.setScaleX(area.getScaleX() * intendedW / width);
        area.setScaleY(area.getScaleY() * intendedH / height);
        if (e.isShiftDown()) {
            double max = Math.max(area.getScaleX(), area.getScaleY());
            area.setScaleX(max);
            area.setScaleY(max);
        }
        bounds = area.getBoundsInParent();
        double minX = bounds.getMinX();
        double minY = bounds.getMinY();
        double a = min - minX;
        double b = min2 - minY;
        area.setLayoutX(area.getLayoutX() + a);
        area.setLayoutY(area.getLayoutY() + b);

    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
            area.setStroke(Color.BLACK);
            getArea().setManaged(false);
            getArea().setFill(Color.TRANSPARENT);
        }

        initialX = (int) e.getX();
        getArea().setLayoutX(initialX);
        getArea().setScaleX(1. / 10);
        getArea().setScaleY(1. / 10);
        initialY = (int) e.getY();
        getArea().setLayoutY(initialY);
        getArea().setStroke(option.isStroke() ? model.getFrontColor() : Color.TRANSPARENT);
        getArea().setFill(option.isFill() ? model.getBackColor() : Color.TRANSPARENT);
    }

    @Override
    protected void onMouseReleased(final PaintModel model) {

        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea())) {
            model.takeSnapshotFill(area);
        }
        children.remove(getArea());
    }

}