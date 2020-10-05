package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleNodeBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;

public class PictureTool extends PaintTool {

    private SVGPath area;
    private int initialX;
    private int initialY;
    private FillOption option = FillOption.STROKE;

    private Map<String, Double> maxMap = new HashMap<>();

    @FXML
    private ToggleGroup fillOptionGroup;

    @FXML
    private ToggleGroup shapeOption;

    @FXML
    private FlowPane picturePane;

    @FXML
    private FlowPane propertiesPane;

    @FXML
    private SVGPath icon4;

    @FXML
    private SVGPath icon3;

    @FXML
    private SVGPath icon2;
    private Parent loadParent;

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
            area.setSmooth(false);
        }
        if (StringUtils.isBlank(area.getContent())) {
            area.setContent(PictureOption.TRIANGLE.getPath());
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
            showNewPicDialog();
        }
    }

    @Override
    public void onDeselected(PaintModel model) {
        onMouseReleased(model);
    }

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
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
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
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
    public void onMouseReleased(final PaintModel model) {

        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea())) {
            model.takeSnapshot(area);
        }
        children.remove(getArea());
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        if (icon2 == null) {

            loadParent = CommonsFX.loadParent("PictureTool.fxml", this);
            fillOptionGroup.selectedToggleProperty().addListener((o, old, newV) -> option =
                    FunctionEx.mapIf(newV, n -> (FillOption) n.getUserData(), FillOption.STROKE));
            copyIcon(icon2);
            icon2.strokeProperty().bind(model.frontColorProperty());
            icon2.setFill(Color.TRANSPARENT);
            copyIcon(icon3);
            icon3.setStroke(Color.TRANSPARENT);
            icon3.fillProperty().bind(model.backColorProperty());
            copyIcon(icon4);
            icon4.strokeProperty().bind(model.frontColorProperty());
            icon4.fillProperty().bind(model.backColorProperty());

            maxMap.put("strokeWidth", 10.);
            PaintToolHelper.addOptionsAccordingly(getArea(), propertiesPane.getChildren(), maxMap,
                    Arrays.asList("content", "fill", "stroke"));
            shapeOption.selectedToggleProperty().addListener((o, old, newV) -> getArea().setContent(
                    FunctionEx.mapIf(newV, n -> (String) n.getUserData(), PictureOption.TRIANGLE.getPath())));
            shapeOption.selectToggle(shapeOption.getSelectedToggle());
        }
        model.getToolOptions().getChildren().setAll(loadParent);

    }

    public void showNewPicDialog() {
        TextField pathText = new TextField();
        SVGPath path = new SVGPath();
        path.setContent("M0,0");
        pathText.textProperty().addListener((ob, old, val) -> {
            String value = "M0,0" + pathText.getText();
            String content = path.getContent();
            RunnableEx.make(() -> path.setContent(value), e -> path.setContent(content)).run();
        });

        VBox.setVgrow(path, Priority.ALWAYS);
        SimpleNodeBuilder.onKeyReleased(pathText, KeyCode.ENTER, () -> addNewPic(pathText.getText()));
        new SimpleDialogBuilder().node(path).node(pathText).button("New Pic", () -> addNewPic(pathText.getText()))
                .bindWindow(picturePane)
                .displayDialog();
    }

    private void addNewPic(String path) {
        String value = "M0,0" + path;
        area.setContent(value);
        ToggleButton addToggle = SimpleToggleGroupBuilder.addToggle(shapeOption, PictureOption.toSVG(value), value);
        ObservableList<Node> children = picturePane.getChildren();
        children.add(children.size() - 1, addToggle);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private SVGPath copyIcon(SVGPath createIcon) {
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

}