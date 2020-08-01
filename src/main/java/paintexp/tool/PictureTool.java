package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import schema.sngpc.FXMLCreatorHelper;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.ClassReflectionUtils;
import utils.FunctionEx;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class PictureTool extends PaintTool {

    private static final int PREF_WIDTH = 65;

    private SVGPath area;
    private int initialX;
    private int initialY;
    private FillOption option = FillOption.STROKE;

    private SimpleToggleGroupBuilder picOptions;

    Map<String, Double> maxMap = new HashMap<>();

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
            showNewPicDialog(paintModel);
        }
    }

    @Override
    public void onDeselected(PaintModel model) {
        onMouseReleased(model);
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        SVGPath icon2 = copyIcon();
        icon2.strokeProperty().bind(model.frontColorProperty());
        icon2.setFill(Color.TRANSPARENT);
        SVGPath icon3 = copyIcon();
        icon3.setStroke(Color.TRANSPARENT);
        icon3.fillProperty().bind(model.backColorProperty());
        SVGPath icon4 = copyIcon();
        icon4.strokeProperty().bind(model.frontColorProperty());
        icon4.fillProperty().bind(model.backColorProperty());
        List<Node> togglesAs = new SimpleToggleGroupBuilder().addToggle(icon2, FillOption.STROKE)
                .addToggle(icon3, FillOption.FILL).addToggle(icon4, FillOption.STROKE_FILL)
                .onChange((o, old, newV) -> option =
                        FunctionEx.mapIf(newV, n -> (FillOption) n.getUserData(), FillOption.STROKE))
                .select(option).getTogglesAs(Node.class);
        VBox vBox1 = new VBox(togglesAs.toArray(new Node[0]));
        model.getToolOptions().getChildren().addAll(vBox1);
        maxMap.put("strokeWidth", 10.);
        FlowPane vBox = new FlowPane(Orientation.VERTICAL, 5, 5);
        vBox.setMaxHeight(150);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        model.getToolOptions().getChildren().addAll(vBox);
        PaintToolHelper.addOptionsAccordingly(getArea(), vBox.getChildren(), maxMap,
                Arrays.asList("content", "fill", "stroke"));

        if (picOptions == null) {
            picOptions = new SimpleToggleGroupBuilder();
            Stream.of(PictureOption.values()).map(PictureOption::toSVG)
                    .forEach(e -> picOptions.addToggle(e, (Object) e.getContent()));
            picOptions
                    .onChange((o, old, newV) -> getArea().setContent(
                            FunctionEx.mapIf(newV, n -> (String) n.getUserData(), PictureOption.TRIANGLE.getPath())))
                    .select(PictureOption.TRIANGLE.getPath());
        }
        FlowPane value = new FlowPane(Orientation.VERTICAL, 5, 5);
        value.setPrefWrapLength(800);
        value.maxHeightProperty().bind(vBox1.heightProperty());
        value.setOrientation(Orientation.VERTICAL);
        List<Node> allOptions = picOptions.getTogglesAs(Node.class);
        value.getChildren().addAll(allOptions);

        value.getChildren().add(SimpleButtonBuilder.newButton("Add", e -> showNewPicDialog(model)));
        value.prefHeight(PREF_WIDTH);
        model.getToolOptions().getChildren().add(value);
        FXMLCreatorHelper.createXMLFile(model.getToolOptions(), ResourceFXUtils.getOutFile("PictureTool.fxml"));

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SVGPath copyIcon() {
        SVGPath createIcon = createIcon();
        Map<String, Property> areaProperties = ClassReflectionUtils.simpleProperties(getArea(), SVGPath.class);
        Map<String, Property> simpleProperties = ClassReflectionUtils.simpleProperties(createIcon, SVGPath.class);
        List<String> exclude = Arrays.asList("fill", "stroke");
        simpleProperties.forEach((k, v) -> {
            if (!exclude.contains(k)) {
                areaProperties.get(k).addListener((ob, o, val) -> RunnableEx.ignore(() -> v.setValue(val)));
            }
        });

        return createIcon;
    }

    private void showNewPicDialog(PaintModel paintModel) {
        TextField button = new TextField();
        SVGPath path = new SVGPath();
        path.setContent("M0,0");
        button.textProperty().addListener((ob, old, val) -> {
            String value = "M0,0" + button.getText();
            String content = path.getContent();
            RunnableEx.make(() -> path.setContent(value), e -> path.setContent(content)).run();
        });
        VBox.setVgrow(path, Priority.ALWAYS);
        new SimpleDialogBuilder().node(path).node(button).button("New Pic", () -> {
            String value = "M0,0" + button.getText();
            area.setContent(value);
            picOptions.addToggle(PictureOption.toSVG(value), (Object) value);
            ObservableList<Node> children = paintModel.getToolOptions().getChildren();
            Pane node = (Pane) children.get(children.size() - 1);
            Node node2 = node.getChildren().get(node.getChildren().size() - 1);
            node.getChildren().setAll(picOptions.getTogglesAs(Node.class));
            node.getChildren().add(node2);
        }).bindWindow(paintModel.getImageStack()).displayDialog();
    }

}