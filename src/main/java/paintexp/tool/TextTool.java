package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import java.util.Locale;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleSvgPathBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;

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
		model.createImageVersion();
	}

    public void onOptionsChanged() {
		FontWeight weight = bold.isSelected() ? FontWeight.BOLD : FontWeight.NORMAL;
		FontPosture posture = italic.isSelected() ? FontPosture.ITALIC : FontPosture.REGULAR;
		double size = fontSize.getSelectionModel().getSelectedItem();
		text.setFont(Font.font(fontFamily.getSelectionModel().getSelectedItem(), weight, posture, size));
        Effect selectedItem = effects.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            addOptionsAccordingly(selectedItem, effectsOptions);
        }
	}

    @Override
	public void onSelected(final PaintModel model) {
		model.getToolOptions().getChildren().clear();
        if (options == null) {
			options = CommonsFX.loadParent(ResourceFXUtils.toFile("TextTool.fxml"), this);
			fontFamily
					.setCellFactory(SimpleComboBoxBuilder.cellStyle(fontFamily, t -> "-fx-font-family:\"" + t + "\";"));
			fontFamily.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
			fontSize.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
            effects.getSelectionModel().selectedItemProperty().addListener(e -> onOptionsChanged());
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
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void addOptionsAccordingly(Effect selectedItem, VBox effectsOptions) {
        effectsOptions.getChildren().clear();
        Map<String, Property<?>> getters = ClassReflectionUtils.properties(selectedItem, selectedItem.getClass());
        for (Map.Entry<String, Property<?>> method : getters.entrySet()) {
            String fieldName = method.getKey();
            Property<?> value2 = method.getValue();
            Object value = value2.getValue();
            if (value == null) {
                continue;
            }
            String changeCase = StringSigaUtils.changeCase(fieldName);
            Text text2 = new Text(changeCase);
            text2.textProperty()
                .bind(Bindings.createStringBinding(() -> {
                    if (value instanceof Double) {
                        return String.format(Locale.ENGLISH, "%s %.2f", changeCase, value2.getValue());
                    }
                    return changeCase + " " + value2.getValue();
                }, value2));
            effectsOptions.getChildren().add(text2);
            if (value instanceof Double) {
                Double value3 = (Double) value;
                Slider e = new SimpleSliderBuilder(0, value3 == 1 ? 1 : Math.max(50, value3), value3).build();
                e.valueProperty().bindBidirectional((Property<Number>) value2);
                effectsOptions.getChildren().add(e);
            }
            if (value instanceof Integer) {
                Integer value3 = (Integer) value;
                Slider e = new SimpleSliderBuilder(0, value3 == 1 ? 1 : Math.max(50, value3), value3).build();
                e.valueProperty().bindBidirectional((Property<Number>) value2);
                effectsOptions.getChildren().add(e);
            }
            if (value instanceof Color) {
                ColorPicker colorPicker = new ColorPicker((Color) value);
                ((Property<Color>) value2).bind(colorPicker.valueProperty());
                effectsOptions.getChildren().add(colorPicker);
            }
            if (value instanceof Enum<?>) {
                Enum<?> value3 = (Enum<?>) value;
                ComboBox comboBox = new ComboBox<>(
                    FXCollections.observableArrayList(value3.getClass().getEnumConstants()));
                value2.bind(comboBox.getSelectionModel().selectedItemProperty());
                comboBox.setValue(value3);
                effectsOptions.getChildren().add(comboBox);
            }
        }
    }

}