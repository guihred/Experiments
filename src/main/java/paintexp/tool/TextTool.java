package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;

import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleSvgPathBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.RotateUtils;

public class TextTool extends PaintTool {

	private double initialX;
	private double initialY;
	private boolean pressed;
	private List<? extends Node> helpers;
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
	private ToggleButton strikeThrough;
	@FXML
	private ToggleButton underline;
	@FXML
	private ToggleGroup alignments;
	@FXML
	private ComboBox<Integer> fontSize;
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
		text.setUnderline(underline.isSelected());
		text.setStrikethrough(strikeThrough.isSelected());
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
			text.textProperty().bind(textArea.textProperty());
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
			if (containsPoint(area, e.getX(), e.getY())
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
			helpers = RotateUtils.createDraggableRectangle(area);
			area.setStroke(Color.BLACK);
			area.setManaged(false);
			area.setFill(Color.TRANSPARENT);
			area.setLayoutX(initialX);
			area.setLayoutY(initialY);
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
		model.takeSnapshotFill(text);
		textArea.setText("");
	}

}