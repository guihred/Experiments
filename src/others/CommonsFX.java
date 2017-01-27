package others;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.ArcTo;
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

	public static PathTransition newPathTransistion(Duration duration, Shape path, Node node,
			OrientationType orientation, Interpolator interpolator, boolean autoReverse, int cycleCount) {
		PathTransition build = new PathTransition(duration, path, node);
		build.setOrientation(orientation);
		build.setInterpolator(interpolator);
		build.setAutoReverse(autoReverse);
		build.setCycleCount(cycleCount);
		return build;
	}

	public static <T> ChoiceBox<T> newSelect(ObservableList<T> nome, StringConverter<T> converter, String string) {
		ChoiceBox<T> choiceBox = new ChoiceBox<>(nome);
		Tooltip arg0 = new Tooltip(string);
		choiceBox.setTooltip(arg0);
		choiceBox.setConverter(converter);
		return choiceBox;
	}

	public static TextField newTextField(String text, int prefColumnCount) {
		TextField textField = new TextField(text);
		textField.setPrefColumnCount(prefColumnCount);
		return textField;
	}

}
