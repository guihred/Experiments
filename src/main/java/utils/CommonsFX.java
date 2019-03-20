package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;

public final class CommonsFX {

    private CommonsFX() {
    }

    public static void displayDialog(final String text, final String buttonMsg, final Runnable c) {
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

    public static void displayDialog(final String text, final String buttonMsg, final Supplier<DoubleProperty> c) {
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

    public static List<Color> generateRandomColors(final int size) {
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

    public static Button newButton(final double layoutX, final double layoutY, final String nome,
	    final EventHandler<ActionEvent> onAction) {
	Button button = new Button(nome);
	button.setLayoutX(layoutX);
	button.setLayoutY(layoutY);
	button.setOnAction(onAction);
	return button;
    }

    public static Button newButton(final Node graphic, final String id, final EventHandler<ActionEvent> onAction) {
	Button button = new Button(null, graphic);
	button.setId(id);
	button.setOnAction(onAction);
	return button;
    }

    public static Button newButton(final String nome, final EventHandler<ActionEvent> onAction) {
	Button button = new Button(nome);
	button.setId(nome);
	button.setOnAction(onAction);
	return button;
    }

    public static CheckBox newCheck(final String name, final BooleanProperty showWeight) {
	CheckBox checkBox = new CheckBox(name);
	checkBox.setSelected(showWeight.get());
	showWeight.bind(checkBox.selectedProperty());
	return checkBox;
    }

    public static CheckBox newCheckBox(final String text, final boolean disabled) {
	CheckBox build = new CheckBox(text);
	build.setDisable(disabled);
	return build;
    }

    public static VBox newSlider(final String string, final double min, final double max, int block,
	    final Property<Number> radius) {
	Slider build = new SimpleSliderBuilder().min(min).max(max).blocks(block).build();
	build.valueProperty().bindBidirectional(radius);
	return new VBox(new Text(string), build);
    }

    public static VBox newSlider(final String string, final double min, final double max,
	    final Property<Number> radius) {
	Slider build = new SimpleSliderBuilder().min(min).max(max).build();
	build.valueProperty().bindBidirectional(radius);
	return new VBox(new Text(string), build);
    }

    public static TextField newTextField(final String text, final int prefColumnCount) {
	TextField textField = new TextField(text);
	textField.setPrefColumnCount(prefColumnCount);
	return textField;
    }

    public static ToggleButton newToggleButton(final String id, final Node graphic,
	    final EventHandler<ActionEvent> onAction) {
	ToggleButton button = new ToggleButton();
	button.setId(id);
	button.setGraphic(graphic);
	button.setOnAction(onAction);
	return button;
    }

}
