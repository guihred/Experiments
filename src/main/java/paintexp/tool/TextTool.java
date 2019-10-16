package paintexp.tool;

import static simplebuilder.SimpleComboBoxBuilder.cellStyle;
import static utils.DrawOnPoint.getWithinRange;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import simplebuilder.SimpleSvgPathBuilder;
import utils.CommonsFX;

public class TextTool extends PaintTool {

    private double initialX;
    private double initialY;
    private boolean pressed;
    @FXML
    private Text text;
    @FXML
    private Rectangle area;
    @FXML
    private TextArea textArea;
    @FXML
    private ToggleButton bold;
    @FXML
    private ToggleButton italic;
    @FXML
    private ToggleGroup alignments;
    @FXML
    private VBox effectsOptions;
    @FXML
    private ComboBox<Integer> fontSize;
    @FXML
    private ComboBox<Effect> effects;
    @FXML
    private ComboBox<String> fontFamily;
    private Parent options;

    private Map<Object, Double> maxMap = new HashMap<>();

    @Override
    public Node createIcon() {
        return new SimpleSvgPathBuilder()
            .content("M108 40 L 150.0 140.0 H 128.0 L 115.0 110.0 H 85.0 L 70.0 140.0 H 60.0"
                + " Z  M 100.0 75.0 L 112.0 103.0 H 87.0 Z")
            .stroke(Color.BLACK).fillRule(FillRule.EVEN_ODD).fill(Color.BLACK).build();
    }

    @Override
    public void handleEvent(final MouseEvent e, final PaintModel model) {
        simpleHandleEvent(e, model);
    }

    @SuppressWarnings("unused")
    public void onAlignmentChange(ObservableValue<? extends Toggle> ob, Toggle old, Toggle newV) {
        text.setTextAlignment(newV == null ? TextAlignment.LEFT : (TextAlignment) newV.getUserData());

    }

    @Override
    public void onDeselected(final PaintModel model) {
        takeSnapshot(model);
    }

    public void onOptionsChanged() {
        FontWeight weight = bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        double size = fontSize.getSelectionModel().getSelectedItem();
        text.setFont(Font.font(fontFamily.getSelectionModel().getSelectedItem(), weight, posture, size));
        Effect selectedItem = effects.getSelectionModel().getSelectedItem();
        PaintToolHelper.addOptionsAccordingly(selectedItem, effectsOptions, maxMap, effects.getItems());
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        if (options == null) {
            options = CommonsFX.loadParent("TextTool.fxml", this);
            fontFamily.setCellFactory(cellStyle(fontFamily, t -> "-fx-font-family:\"" + t + "\";"));
            fontFamily.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
            fontSize.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
            effects.getItems().add(0, null);
            effects.getProperties().put("anchor", 0);
            effects.getProperties().put("isDefaultAnchor", true);
            effects.getSelectionModel().selectedIndexProperty().addListener(e -> onOptionsChanged());
        }

        model.getToolOptions().getChildren().addAll(options);
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
        if (children.contains(area)) {
            if (containsPoint(area, e.getX(), e.getY())) {
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
        if (area.getWidth() < 2 && children.contains(area)) {

            children.remove(area);
        }
        pressed = false;
        textArea.requestFocus();
        area.setStroke(Color.BLUE);
    }

    private void addRect(final PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(area)) {
            children.add(area);
            area.setStroke(Color.BLACK);
            area.setManaged(false);
            area.setFill(Color.TRANSPARENT);
            area.setLayoutX(initialX);
            area.setLayoutY(initialY);
            area.setWidth(1);
            area.setHeight(1);
        }
        if (!children.contains(text)) {
            children.add(text);
            text.fillProperty().bind(model.frontColorProperty());

            text.layoutXProperty().bind(area.layoutXProperty());
            text.layoutYProperty()
                .bind(Bindings.createDoubleBinding(() -> area.layoutYProperty().get() + text.getFont().getSize(),
                    area.layoutYProperty(), text.fontProperty()));
            text.wrappingWidthProperty().bind(area.widthProperty());
        }
    }

    private void dragTo(final double x, final double y) {
        area.setLayoutX(Math.min(x, initialX));
        area.setLayoutY(Math.min(y, initialY));
        area.setWidth(Math.abs(x - initialX));
        area.setHeight(Math.abs(y - initialY));
    }

    private void takeSnapshot(final PaintModel model) {
        model.takeSnapshot(text);
        textArea.setText("");
        model.createImageVersion();
    }

}