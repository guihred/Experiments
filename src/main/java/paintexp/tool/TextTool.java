package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import java.io.File;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleSvgPathBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.RotateUtils;

public class TextTool extends PaintTool {

    private Text text;
    private Rectangle area;
    private double initialX;
    private double initialY;
    private boolean pressed;
    private List<? extends Node> helpers;
    @FXML
    private TextArea textArea;
    @FXML
    private ToggleButton bold;
    @FXML
    private ToggleButton italic;
    @FXML
    private ToggleButton strikeThrough;
    @FXML
    private ToggleButton underline;
    @FXML
    private ToggleGroup alignments;
    @FXML
    private ComboBox<Integer> fontSize;
    @FXML
    private ComboBox<String> fontFamily;

    @Override
    public Node createIcon() {
        return new SimpleSvgPathBuilder()
            .content("M108 40 L 150.0 140.0 H 128.0 L 115.0 110.0 H 85.0 L 70.0 140.0 H 60.0"
                + " Z  M 100.0 75.0 L 112.0 103.0 H 87.0 Z")
            .stroke(Color.BLACK).fillRule(FillRule.EVEN_ODD).fill(Color.BLACK).build();
    }

    public Rectangle getArea() {
        if (area == null) {
            area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK).cursor(Cursor.MOVE)
                .width(10).height(10).strokeDashArray(1, 2, 1, 2).build();
        }
        return area;
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
        simpleHandleEvent(e, model);
    }

    @SuppressWarnings("unused")
    public void onAlignmentChange(ObservableValue<? extends Toggle> ob, Toggle old, Toggle newV) {
        getText().setTextAlignment(newV == null ? TextAlignment.LEFT : (TextAlignment) newV.getUserData());

    }

    @Override
    public void onDeselected(final PaintModel model) {
        takeSnapshot(model);
        model.createImageVersion();
    }

    public void onOptionsChanged() {
        FontWeight weight = bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        double size = fontSize.getSelectionModel().getSelectedItem();
        text.setFont(Font.font(fontFamily.getSelectionModel().getSelectedItem(), weight, posture, size));
        getText().setUnderline(underline.isSelected());
        getText().setStrikethrough(strikeThrough.isSelected());
    }

    @Override
    public void onSelected(final PaintModel model) {
        displayTextOptions(model);
    }

    @Override
    protected void onMouseDragged(final MouseEvent e, final PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        double width = model.getImage().getWidth();
        double height = model.getImage().getHeight();
        if (pressed) {
            dragTo(getWithinRange(x, 0, width), getWithinRange(y, 0, height));
        }
    }

    @Override
    protected void onMousePressed(final MouseEvent e, final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea())) {
            if (containsPoint(getArea(), e.getX(), e.getY())
                || helpers.stream().anyMatch(n -> n.contains(e.getX(), e.getY()))) {
                return;
            }
            takeSnapshot(model);
            return;
        }
        initialX = e.getX();
        initialY = e.getY();
        pressed = true;
        addRect(model);

    }

    @Override
    protected void onMouseReleased(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getWidth() < 2 && children.contains(getArea())) {

            children.remove(getArea());
        }
        pressed = false;
        textArea.requestFocus();
        area.setStroke(Color.BLUE);
    }

    private void addRect(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
            helpers = RotateUtils.createDraggableRectangle(area);
            area.setStroke(Color.BLACK);
            getArea().setManaged(false);
            getArea().setFill(Color.TRANSPARENT);
            getArea().setLayoutX(initialX);
            getArea().setLayoutY(initialY);
        }
        if (!children.contains(getText())) {
            children.add(getText());
            getText().fillProperty().bind(model.frontColorProperty());
            getText().layoutXProperty().bind(area.layoutXProperty());
            getText().layoutYProperty()
                .bind(Bindings.createDoubleBinding(() -> area.layoutYProperty().get() + text.getFont().getSize(),
                    area.layoutYProperty(), text.fontProperty()));
            getText().wrappingWidthProperty().bind(area.widthProperty());
        }
    }

    private void displayTextOptions(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        File file = ResourceFXUtils.toFile("TextTool.fxml");
        Parent parent2 = CommonsFX.loadParent(file, this);
        fontFamily.setCellFactory(SimpleComboBoxBuilder.cellStyle(fontFamily, t -> "-fx-font-family:\"" + t + "\";"));
        fontFamily.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
        fontSize.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
        getText().textProperty().bind(textArea.textProperty());
        model.getToolOptions().getChildren().addAll(parent2);
    }

    private void dragTo(final double x, final double y) {
        getArea().setLayoutX(Math.min(x, initialX));
        getArea().setLayoutY(Math.min(y, initialY));
        getArea().setWidth(Math.abs(x - initialX));
        getArea().setHeight(Math.abs(y - initialY));
    }

    private void takeSnapshot(final PaintModel model) {
        model.takeSnapshotFill(text);
        textArea.setText("");
    }

}