/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch02;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import others.*;
import simplebuilder.*;

public class CSSStylingExample extends Application {

	DoubleProperty fillVals = new SimpleDoubleProperty(255.0);
	Scene sceneRef;
	ObservableList<Cursor> cursors = FXCollections.observableArrayList(Cursor.DEFAULT, Cursor.CROSSHAIR, Cursor.WAIT,
			Cursor.TEXT, Cursor.HAND, Cursor.MOVE, Cursor.N_RESIZE, Cursor.NE_RESIZE, Cursor.E_RESIZE, Cursor.SE_RESIZE,
			Cursor.S_RESIZE, Cursor.SW_RESIZE, Cursor.W_RESIZE, Cursor.NW_RESIZE, Cursor.NONE);

	Slider sliderRef = new SimpleSliderBuilder().min(0).max(255).value(255).orientation(Orientation.VERTICAL).build();
	ChoiceBox<Cursor> choiceBoxRef = new ChoiceBox<>(cursors);
	Text textSceneX = new SimpleTextBuilder().styleClass("emphasized-text").build();
	Text textSceneY = new SimpleTextBuilder().styleClass("emphasized-text").build();
	Text textSceneW = new SimpleTextBuilder().styleClass("emphasized-text").build();
	Text textSceneH = new SimpleTextBuilder().styleClass("emphasized-text").id("sceneHeightText").build();
	public static void main(String[] args) {
		Application.launch(args);
	}

	Label labelStageX = new SimpleLabelBuilder().id("stageX").build();

	Label labelStageY = new SimpleLabelBuilder().id("stageY").build();
	Label labelStageW = new Label();
	Label labelStageH = new Label();
	@Override
	public void start(Stage stage) {
		final ToggleGroup toggleGrp = new ToggleGroup();


		Hyperlink build = new SimpleHyperlinkBuilder().text("lookup()").onAction((e) -> {
			System.out.println("sceneRef:" + sceneRef);
			Text textRef = (Text) sceneRef.lookup("#sceneHeightText");
			System.out.println(textRef.getText());
		}).build();

		FlowPane sceneRoot = new SimpleFlowPaneBuilder()
				.layoutX(20)
				.layoutY(40)
				.padding(new Insets(0, 20, 40, 0))
				.orientation(Orientation.VERTICAL)
				.vgap(10)
				.hgap(20)
				.columnHalignment(HPos.LEFT)
				.children(new HBox(10, sliderRef, choiceBoxRef)
						, textSceneX, textSceneY, textSceneW, textSceneH,
						build,
						new SimpleRadioButtonBuilder().text("onTheScene.css")
								.toggleGroup(toggleGrp).selected(true)
								.build(),
						new SimpleRadioButtonBuilder().text("changeOfScenes.css")
								.toggleGroup(toggleGrp).build(),
						labelStageX, labelStageY, labelStageW, labelStageH)
				.build();

		sceneRef = new Scene(sceneRoot, 600, 250);
		sceneRef.getStylesheets().addAll(CSSStylingExample.class.getResource("onTheScene.css").toExternalForm());
		stage.setScene(sceneRef);
		choiceBoxRef.getSelectionModel().selectFirst();
		// Setup various property binding
		textSceneX.textProperty().bind(new SimpleStringProperty("Scene x: ").concat(sceneRef.xProperty().asString()));
		textSceneY.textProperty().bind(new SimpleStringProperty("Scene y: ").concat(sceneRef.yProperty().asString()));
		textSceneW.textProperty()
				.bind(new SimpleStringProperty("Scene width: ").concat(sceneRef.widthProperty().asString()));
		textSceneH.textProperty()
				.bind(new SimpleStringProperty("Scene height: ").concat(sceneRef.heightProperty().asString()));
		labelStageX.textProperty()
				.bind(new SimpleStringProperty("Stage x: ").concat(sceneRef.getWindow().xProperty().asString()));
		labelStageY.textProperty()
				.bind(new SimpleStringProperty("Stage y: ").concat(sceneRef.getWindow().yProperty().asString()));
		labelStageW.textProperty().bind(
				new SimpleStringProperty("Stage width: ").concat(sceneRef.getWindow().widthProperty().asString()));
		labelStageH.textProperty().bind(
				new SimpleStringProperty("Stage height: ").concat(sceneRef.getWindow().heightProperty().asString()));
		sceneRef.cursorProperty().bind(choiceBoxRef.getSelectionModel().selectedItemProperty());
		fillVals.bind(sliderRef.valueProperty());
		// When fillVals changes, use that value as the RGB to fill the scene
		fillVals.addListener((ov, oldValue, newValue) -> {
			Double fillValue = fillVals.getValue() / 256.0;
			sceneRef.setFill(new Color(fillValue, fillValue, fillValue, 1.0));
		});
		// When the selected radio button changes, set the appropriate
		// stylesheet
		toggleGrp.selectedToggleProperty().addListener((ov, oldValue, newValue) -> {
			String radioButtonText = ((RadioButton) toggleGrp.getSelectedToggle()).getText();
			sceneRef.getStylesheets().addAll(CSSStylingExample.class.getResource(radioButtonText).toExternalForm());
		});
		stage.setTitle("On the Scene");
		stage.show();
		// Define an unmanaged node that will display Text
		Text addedTextRef = new SimpleTextBuilder().x(0).y(-30).textOrigin(VPos.TOP).fill(Color.BLUE)
				.font(Font.font("Sans Serif", FontWeight.BOLD, 16)).managed(false).build();
		// Bind the text of the added Text node to the fill property of the
		// Scene
		addedTextRef.textProperty().bind(new SimpleStringProperty("Scene fill: ").concat(sceneRef.fillProperty()));
		// Add to the Text node to the FlowPane
		((FlowPane) sceneRef.getRoot()).getChildren().add(addedTextRef);
	}
}
