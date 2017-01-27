package fxproexercises.ch02;

import static others.CommonsFX.newButton;
import static others.CommonsFX.newCheckBox;
import static others.CommonsFX.newTextField;

import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import others.SimpleRectangleBuilder;
import others.SimpleTextBuilder;
import others.SimpleVBoxBuilder;

public class FxProCH2 extends Application {

	StringProperty title = new SimpleStringProperty();
	Text textStageX;
	Text textStageY;
	Text textStageW;
	Text textStageH;
	Text textStageF;
	CheckBox checkBoxResizable;
	CheckBox checkBoxFullScreen = new CheckBox("fullScreen");
	double dragAnchorX;
	double dragAnchorY;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		StageStyle stageStyle = StageStyle.UTILITY;
		List<String> unnamedParams = getParameters().getUnnamed();
		if (unnamedParams.size() > 0) {
			String stageStyleParam = unnamedParams.get(0);
			if (stageStyleParam.equalsIgnoreCase("transparent")) {
				stageStyle = StageStyle.TRANSPARENT;
			} else if (stageStyleParam.equalsIgnoreCase("undecorated")) {
				stageStyle = StageStyle.UNDECORATED;
			} else if (stageStyleParam.equalsIgnoreCase("utility")) {
				stageStyle = StageStyle.UTILITY;
			}
		}
		final Stage stageRef = stage;
		Group rootGroup;
		TextField titleTextField = newTextField("Stage Coach", 15);
		final Rectangle skyBlueRect = new SimpleRectangleBuilder().x(0).y(0).width(250).height(350).arcHeight(50).arcWidth(50).fill(Color.SKYBLUE).stroke(null).build();
		final Button toFrontButton = newButton("toFront()", (e) -> {
			stageRef.toFront();
		});
		final Button closeButton = newButton("close()", (e) -> {
			stageRef.close();
		});
		final Button toBackButton = newButton("toBack()", (e) -> {
			stageRef.toBack();
		});
		final HBox hbox = new HBox(10, new Label("title:"), titleTextField);
		boolean disabled = stageStyle == StageStyle.TRANSPARENT || stageStyle == StageStyle.UNDECORATED;
		String text = "resizable";
		Node[] children = { textStageX = new SimpleTextBuilder().textOrigin(VPos.TOP).build(), textStageY = new SimpleTextBuilder().textOrigin(VPos.TOP).build(), textStageW = new SimpleTextBuilder().textOrigin(VPos.TOP).build(), textStageH = new SimpleTextBuilder().textOrigin(VPos.TOP).build(),
				textStageF = new SimpleTextBuilder().textOrigin(VPos.TOP).build(), checkBoxResizable = newCheckBox(text, disabled), checkBoxFullScreen, hbox, toBackButton, toFrontButton, closeButton };
		Scene scene = new Scene(rootGroup = new Group(skyBlueRect,
				new SimpleVBoxBuilder().layoutX(30).layoutY(20).spacing(10).children(children).build()),
				270, 370);
		scene.setFill(Color.TRANSPARENT);
		// When mouse button is pressed, save the initial position of screen
		rootGroup.setOnMousePressed((MouseEvent me) -> {
			dragAnchorX = me.getScreenX() - stageRef.getX();
			dragAnchorY = me.getScreenY() - stageRef.getY();
		});
		// When screen is dragged, translate it accordingly

		rootGroup.setOnMouseDragged((MouseEvent me) -> {
			stageRef.setX(me.getScreenX() - dragAnchorX);
			stageRef.setY(me.getScreenY() - dragAnchorY);
		});
		textStageX.textProperty().bind(new SimpleStringProperty("x: ").concat(stageRef.xProperty().asString()));
		textStageY.textProperty().bind(new SimpleStringProperty("y: ").concat(stageRef.yProperty().asString()));
		textStageW.textProperty().bind(new SimpleStringProperty("width: ").concat(stageRef.widthProperty().asString()));
		textStageH.textProperty()
				.bind(new SimpleStringProperty("height: ").concat(stageRef.heightProperty().asString()));
		textStageF.textProperty()
				.bind(new SimpleStringProperty("focused: ").concat(stageRef.focusedProperty().asString()));
		stage.setResizable(true);
		checkBoxResizable.selectedProperty().bindBidirectional(stage.resizableProperty());
		checkBoxFullScreen.selectedProperty().addListener(
				(ov, oldValue, newValue) -> stageRef.setFullScreen(checkBoxFullScreen.selectedProperty().getValue()));
		title.bind(titleTextField.textProperty());
		stage.setScene(scene);
		stage.titleProperty().bind(title);
		stage.initStyle(stageStyle);
		stage.setOnCloseRequest(we -> System.out.println("Stage is closing"));
		stage.show();
		Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
		stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4);
	}

}
