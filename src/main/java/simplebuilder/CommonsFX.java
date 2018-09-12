package simplebuilder;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import javafx.util.StringConverter;

public final class CommonsFX {

	private CommonsFX() {
	}

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
		button.setLayoutY(layoutY);
		button.setOnAction(onAction);
		return button;
	}

	public static Button newButton(String nome, EventHandler<ActionEvent> onAction) {
		Button button = new Button(nome);
        button.setId(nome);
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
            PathTransition.OrientationType orientation, Interpolator interpolator, boolean autoReverse,
            int cycleCount) {
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

    public static void setZoomable(Node node) {

        setZoomable(node, false);
    }

    public static void setSpinnable(Node cube, Scene scene) {
        DoubleProperty mousePosX = new SimpleDoubleProperty();
        DoubleProperty mousePosY = new SimpleDoubleProperty();
        DoubleProperty mouseOldX = new SimpleDoubleProperty();
        DoubleProperty mouseOldY = new SimpleDoubleProperty();
        final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
        final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);

        cube.getTransforms().addAll(rotateX, rotateY);
        scene.setOnMousePressed(me -> {
            mouseOldY.set(me.getSceneY());
            mouseOldX.set(me.getSceneX());
        });
        scene.setOnMouseDragged(me -> {
            mousePosX.set(me.getSceneX());
            mousePosY.set(me.getSceneY());
            rotateX.setAngle(rotateX.getAngle() - (mousePosY.get() - mouseOldY.get()));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX.get() - mouseOldX.get()));
            mouseOldX.set(mousePosX.get());
            mouseOldY.set(mousePosY.get());
        });
    }

    public static CheckBox newCheck(String name, BooleanProperty showWeight) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setSelected(showWeight.get());
        showWeight.bind(checkBox.selectedProperty());
        return checkBox;
    }

    public static void setZoomable(Node node, boolean onlyClose) {
        Scale scale = new Scale(1, 1);
        Translate translate = new Translate(0, 0);
        node.getTransforms().addAll(scale, translate);
        double delta = 0.1;
        DoubleProperty iniX = new SimpleDoubleProperty(0);
        DoubleProperty iniY = new SimpleDoubleProperty(0);

        node.setOnScroll(scrollEvent -> {
            double scaleValue = scale.getX();
            double s = scaleValue;
            if (scrollEvent.getDeltaY() < 0) {
                scaleValue -= delta;
            } else {
                scaleValue += delta;
            }
            if (onlyClose && scaleValue < 1) {
                scaleValue = s;
            }

            if (scaleValue <= 0.1) {
                scaleValue = s;
            }
            scale.setX(scaleValue);
            scale.setY(scaleValue);
            scrollEvent.consume();
        });

        node.setOnMousePressed(evt -> {
            iniX.set(evt.getX());
            iniY.set(evt.getY());
        });

        node.setOnMouseDragged(evt -> {
            double deltaX = evt.getX() - iniX.get();
            double deltaY = evt.getY() - iniY.get();
            translate.setX(translate.getX() + deltaX);
            translate.setY(translate.getY() + deltaY);
        });
    }



}
