/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch02;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.ex.HasLogging;

public class CSSStylingExample extends Application {
    private static final Logger LOGGER = HasLogging.log();
    @FXML
    private Text textSceneW;
    @FXML
    private RadioButton radioButton6;
    @FXML
    private ToggleGroup toggleGroup5;
    @FXML
    private Text sceneHeightText;
    @FXML
    private Label labelStageH;
    @FXML
    private ChoiceBox<Cursor> choiceBoxRef;
    @FXML
    private Label labelStageW;
    @FXML
    private Hyperlink hyperlink4;
    @FXML
    private Text textSceneX;
    @FXML
    private Text textSceneY;
    @FXML
    private RadioButton radioButton7;
    @FXML
    private Label stageX;
    @FXML
    private Slider sliderRef;
    @FXML
    private Label stageY;

    @FXML
    private Text addedTextRef;

    private ObservableList<Cursor> cursors = FXCollections.observableArrayList(Cursor.DEFAULT, Cursor.CROSSHAIR,
            Cursor.WAIT, Cursor.TEXT, Cursor.HAND, Cursor.MOVE, Cursor.N_RESIZE, Cursor.NE_RESIZE, Cursor.E_RESIZE,
            Cursor.SE_RESIZE, Cursor.S_RESIZE, Cursor.SW_RESIZE, Cursor.W_RESIZE, Cursor.NW_RESIZE, Cursor.NONE);

    private DoubleProperty fillVals = new SimpleDoubleProperty(255.0);

    public void configScene(Scene sceneRef) {
        // Setup various property binding
        textSceneX.textProperty().bind(new SimpleStringProperty("Scene x: ").concat(sceneRef.xProperty().asString()));
        textSceneY.textProperty().bind(new SimpleStringProperty("Scene y: ").concat(sceneRef.yProperty().asString()));
        textSceneW.textProperty()
                .bind(new SimpleStringProperty("Scene width: ").concat(sceneRef.widthProperty().asString()));
        sceneHeightText.textProperty()
                .bind(new SimpleStringProperty("Scene height: ").concat(sceneRef.heightProperty().asString()));
        stageX.textProperty()
                .bind(new SimpleStringProperty("Stage x: ").concat(sceneRef.getWindow().xProperty().asString()));
        stageY.textProperty()
                .bind(new SimpleStringProperty("Stage y: ").concat(sceneRef.getWindow().yProperty().asString()));
        labelStageW.textProperty().bind(
                new SimpleStringProperty("Stage width: ").concat(sceneRef.getWindow().widthProperty().asString()));
        labelStageH.textProperty().bind(
                new SimpleStringProperty("Stage height: ").concat(sceneRef.getWindow().heightProperty().asString()));
        sceneRef.cursorProperty().bind(choiceBoxRef.getSelectionModel().selectedItemProperty());
        // When fillVals changes, use that value as the RGB to fill the scene
        fillVals.addListener((ov, oldValue, newValue) -> {
            Double fillValue = fillVals.getValue() / 256.0;
            sceneRef.setFill(new Color(fillValue, fillValue, fillValue, 1.0));
        });
        // When the selected radio button changes, set the appropriate
        // stylesheet
        toggleGroup5.selectedToggleProperty().addListener((ov, oldValue, newValue) -> {
            String radioButtonText = ((Labeled) newValue).getText();
            sceneRef.getStylesheets().clear();
            CommonsFX.addCSS(sceneRef, radioButtonText);
        });

        addedTextRef.textProperty().bind(new SimpleStringProperty("Scene fill: ").concat(sceneRef.fillProperty()));
    }

    public void initialize() {
        choiceBoxRef.setItems(cursors);
        choiceBoxRef.getSelectionModel().selectFirst();
        fillVals.bind(sliderRef.valueProperty());
    }

    public void onActionHyperlink4() {
        LOGGER.info(sceneHeightText.getText());
    }

    @Override
    public void start(Stage stage) {
        CommonsFX.loadFXML("On The Scene", "CSSStylingExample.fxml", this, stage);
        configScene(stage.getScene());
        CommonsFX.addCSS(stage.getScene(), "onTheScene.css");
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
