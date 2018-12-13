package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class CommonsFX {

	private CommonsFX() {
	}

	public static void displayDialog(String text, String buttonMsg, Runnable c) {
		final Stage stage1 = new Stage();
		final Button button = CommonsFX.newButton(buttonMsg, a -> {
			c.run();
			stage1.close();
		});
		final VBox group = new VBox(new Text(text), button);
		group.setAlignment(Pos.CENTER);
		stage1.setScene(new Scene(group));
		stage1.show();
	}

	public static void displayDialog(String text, String buttonMsg, Supplier<DoubleProperty> c) {
		final Stage stage1 = new Stage();
		ProgressIndicator progressIndicator = new ProgressIndicator(0);

		final Button button = CommonsFX.newButton(buttonMsg, a -> {
			DoubleProperty progress = c.get();
			progressIndicator.progressProperty().bind(progress);
			progress.addListener((v, o, n) -> {
				if (n.intValue() == 1) {
					Platform.runLater(stage1::close);
				}
			});
		});
		final VBox group = new VBox(new Text(text), progressIndicator, button);
		group.setAlignment(Pos.CENTER);
		stage1.setScene(new Scene(group));
		stage1.show();
	}


    public static List<Color> generateRandomColors(int size) {
        final int maxByte = 255;
        int max = 256;
        List<Color> availableColors = new ArrayList<>();
        int cubicRoot = Integer.max((int) Math.ceil(Math.pow(size, 1.0 / 3.0)), 2);
        for (int i = 0; i < cubicRoot * cubicRoot * cubicRoot; i++) {
            Color rgb = Color.rgb(Math.abs(maxByte - i / cubicRoot / cubicRoot % cubicRoot * max / cubicRoot) % max,
                    Math.abs(maxByte - i / cubicRoot % cubicRoot * max / cubicRoot) % max,
                    Math.abs(maxByte - i % cubicRoot * max / cubicRoot) % max);

            availableColors.add(rgb);
        }
        Collections.shuffle(availableColors);
        return availableColors;
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

    public static Button newButton(Node graphic, String id,EventHandler<ActionEvent> onAction) {
        Button button = new Button(null,graphic);
        button.setId(id);
        button.setOnAction(onAction);
        return button;
    }

    public static Button newButton(String nome, EventHandler<ActionEvent> onAction) {
		Button button = new Button(nome);
        button.setId(nome);
		button.setOnAction(onAction);
		return button;
	}

    public static CheckBox newCheck(String name, BooleanProperty showWeight) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setSelected(showWeight.get());
        showWeight.bind(checkBox.selectedProperty());
        return checkBox;
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

    public static TextField newTextField(String text, int prefColumnCount) {
		TextField textField = new TextField(text);
		textField.setPrefColumnCount(prefColumnCount);
		return textField;
	}


    public static ToggleButton newToggleButton(String id, Node graphic, EventHandler<ActionEvent> onAction) {
        ToggleButton button = new ToggleButton();
        button.setId(id);
        button.setGraphic(graphic);
        button.setOnAction(onAction);
        return button;
    }






}
