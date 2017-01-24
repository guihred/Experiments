package others;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class CommonsFX {

	public static ArcTo newArcTo(int x, int y, int radiusX, int radiusY, boolean sweepFlag) {
		ArcTo arcto = new ArcTo();
		arcto.setX(x);
		arcto.setY(y);
		arcto.setRadiusX(radiusX);
		arcto.setRadiusY(radiusY);
		arcto.setSweepFlag(sweepFlag);
		return arcto;
	}

	public static Button newButton(double layoutX, double layoutY, String nome, EventHandler<ActionEvent> onAction) {
		Button button = new Button(nome);
		button.setLayoutX(layoutX);
		button.setLayoutY(layoutX);
		button.setOnAction(onAction);
		return button;
	}

	public static Button newButton(String nome, EventHandler<ActionEvent> onAction) {
		Button button = new Button(nome);
		button.setOnAction(onAction);
		return button;
	}

	public static CheckBox newCheckBox(int x, int y) {
		CheckBox build = new CheckBox();
		build.setLayoutX(x);
		build.setLayoutY(y);
		return build;
	}

	public static CheckBox newCheckBox(String text, boolean disabled) {
		CheckBox build = new CheckBox(text);
		build.setDisable(disabled);
		return build;
	}

	public static Ellipse newEllipse(double centerX, double centerY, double radiusX, double radiusY, Paint fill) {
		Ellipse build = new Ellipse(centerX, centerY, radiusX, radiusY);
		build.setFill(fill);
		return build;
	}

	public static HBox newHBox(double x, double y, double spacing, Node... children) {
		HBox build = new HBox(spacing, children);
		build.setLayoutX(x);
		build.setLayoutY(y);
		return build;

	}

	public static Line newLine(int startX, int startY, int endX, int endY, Color color) {
		Line line = new Line(startX, startY, endX, endY);
		line.setStroke(color);
		return line;
	}

	public static Line newLine(int startX, int startY, int endX, int endY, int strokeWidth, Color color) {
		Line line = new Line(startX, startY, endX, endY);
		line.setStroke(color);
		line.setStrokeWidth(strokeWidth);
		return line;
	}

	public static PathTransition newPathTransistion(Duration duration, Shape path, Node node,
			OrientationType orientation, Interpolator interpolator, boolean autoReverse, int cycleCount) {
		PathTransition build = new PathTransition(duration, path, node);
		build.setOrientation(orientation);
		build.setInterpolator(interpolator);
		build.setAutoReverse(autoReverse);
		build.setCycleCount(cycleCount);
		return build;
	}

	public static Rectangle newRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight, Color fill,
			Color stroke) {
		Rectangle rectangle = new Rectangle(x, y, width, height);
		rectangle.setArcWidth(arcWidth);
		rectangle.setArcHeight(arcHeight);
		rectangle.setFill(fill);
		rectangle.setStroke(stroke);
		return rectangle;
	}

	public static <T> ChoiceBox<T> newSelect(ObservableList<T> nome, StringConverter<T> converter, String string) {
		ChoiceBox<T> choiceBox = new ChoiceBox<>(nome);
		Tooltip arg0 = new Tooltip(string);
		choiceBox.setTooltip(arg0);
		choiceBox.setConverter(converter);
		return choiceBox;
	}

	public static Slider newSlider(int layoutX, int layoutY, int prefWidth, double minDecibels, double maxDecibels) {
		Slider slider = new Slider();
		slider.setLayoutX(layoutX);
		slider.setLayoutY(layoutY);
		slider.setPrefWidth(prefWidth);
		slider.setMin(minDecibels);
		slider.setMax(maxDecibels);
		return slider;
	}

	public static TextField newTextField(String text, int prefColumnCount) {
		TextField textField = new TextField(text);
		textField.setPrefColumnCount(prefColumnCount);
		return textField;
	}

	public static VBox newVBox(double x, double y, double spacing, Node... children) {
		VBox build = new VBox(spacing, children);
		build.setLayoutX(x);
		build.setLayoutY(y);
		return build;
	}

	public static VBox newVBox(Pos alignment, double spacing, Node... children) {
		VBox build = new VBox(spacing, children);
		build.setAlignment(alignment);
		return build;
	}

}
