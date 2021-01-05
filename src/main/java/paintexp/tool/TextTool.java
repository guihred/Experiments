package paintexp.tool;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.IntStream.range;
import static simplebuilder.SimpleComboBoxBuilder.cellStyle;
import static utils.DrawOnPoint.getWithinRange;

import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import utils.CommonsFX;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;

public class TextTool extends PaintTool {

    private double initialX;
    private double initialY;
    private boolean pressed;
    private boolean moving;
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
    private Pane effectsOptions;
    @FXML
    private ComboBox<Integer> fontSize;
    @FXML
    private ComboBox<Effect> effects;
    @FXML
    private ComboBox<String> fontFamily;
    private Node options;

    @FXML
    private Map<String, Double> maxMap;
    private double dragX;
    private double dragY;

    @Override
    public Node createIcon() {
        loadParent();
        return getIcon();
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

    @Override
    public void onMouseDragged(final MouseEvent e, final PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        double width = model.getImage().getWidth();
        double height = model.getImage().getHeight();
        if (pressed) {
            dragTo(getWithinRange(x, 0, width), getWithinRange(y, 0, height));
        }
        if (moving) {
            area.setLayoutX(Math.max(x - dragX, -width / 4));
            area.setLayoutY(Math.max(y - dragY, -height / 4));
        }

    }

    @Override
    public void onMousePressed(final MouseEvent e, final PaintModel model) {
        if (model.getImageStack().getChildren().contains(area)) {
            if (CommonsFX.containsMouse(area, e)) {
                moving = true;
                dragX = e.getX() - area.getLayoutX();
                dragY = e.getY() - area.getLayoutY();
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
    public void onMouseReleased(final PaintModel model) {
        if (area.getWidth() < 2 && model.getImageStack().getChildren().contains(area)) {
            model.getImageStack().getChildren().remove(area);
        }
        pressed = false;
        moving = false;
        textArea.requestFocus();
        area.setStroke(Color.BLUE);
    }

    public void onOptionsChanged() {
        FontWeight weight = bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
        double size = fontSize.getSelectionModel().getSelectedItem();
        text.setFont(Font.font(fontFamily.getSelectionModel().getSelectedItem(), weight, posture, size));
        PaintToolHelper.addOptionsAccordingly(effects.getSelectionModel().getSelectedItem(),
            effectsOptions.getChildren(), maxMap, effects.getItems());
    }

    @Override
    public void onSelected(final PaintModel model) {
        model.getToolOptions().getChildren().clear();
        model.getToolOptions().getChildren().addAll(options);
    }

    private void addRect(final PaintModel model) {
        if (!model.getImageStack().getChildren().contains(area)) {
            model.getImageStack().getChildren().add(area);
            area.setStroke(Color.BLACK);
            area.setManaged(false);
            area.setFill(Color.TRANSPARENT);
            area.setLayoutX(initialX);
            area.setLayoutY(initialY);
            area.setWidth(1);
            area.setHeight(1);
        }
        if (!model.getImageStack().getChildren().contains(text)) {
            model.getImageStack().getChildren().add(text);
            text.fillProperty().bind(model.frontColorProperty());
        }
    }

    private void dragTo(final double x, final double y) {
        area.setLayoutX(Math.min(x, initialX));
        area.setLayoutY(Math.min(y, initialY));
        area.setWidth(Math.abs(x - initialX));
        area.setHeight(Math.abs(y - initialY));
    }

    private void loadParent() {
        options = CommonsFX.loadParent("TextTool.fxml", this);
        fontFamily.setItems(FXCollections.observableArrayList(Font.getFamilies()));
        FileTreeWalker.getPathByExtensionAsync(ResourceFXUtils.getUserFolder("Documents"), p -> {
            final int defaultSize = 12;
            Font loadFont = Font.loadFont(p.toUri().toURL().toExternalForm(), defaultSize);
            if (loadFont != null && !fontFamily.getItems().contains(loadFont.getFamily())) {
                fontFamily.getItems().add(loadFont.getFamily());
            }
        }, ".ttf");
        fontFamily.setCellFactory(cellStyle(fontFamily, t -> "-fx-font-family:\"" + t + "\";"));
        fontFamily.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
        fontSize.setItems(range(7, 500).boxed().collect(toCollection(FXCollections::observableArrayList)));
        fontSize.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
        effects.getItems().add(0, null);
        effects.getSelectionModel().selectedIndexProperty().addListener(e -> onOptionsChanged());
    }

    private void takeSnapshot(final PaintModel model) {
        model.takeSnapshot(text);
        textArea.setText("");
        model.createImageVersion();
    }

}