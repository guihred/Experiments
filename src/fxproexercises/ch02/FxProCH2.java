package fxproexercises.ch02;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxBuilder;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FxProCH2 extends Application {

    StringProperty title = new SimpleStringProperty();
    Text textStageX;
    Text textStageY;
    Text textStageW;
    Text textStageH;
    Text textStageF;
    CheckBox checkBoxResizable;
    CheckBox checkBoxFullScreen;
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
        TextField titleTextField;
        final Rectangle skyBlueRect = RectangleBuilder.create()
                .width(250)
                .height(350)
                .arcWidth(50)
                .arcHeight(50)
                .fill(Color.SKYBLUE)
                .build();
        final Button toFrontButton = ButtonBuilder.create()
                .text("toFront()")
                .onAction((e) -> {
                    stageRef.toFront();
                })
                .build();
        final Button closeButton = ButtonBuilder.create()
                .text("close()")
                .onAction((e) -> {
                    stageRef.close();
                })
                .build();
        final Button toBackButton = ButtonBuilder.create()
                .text("toBack()")
                .onAction((e) -> {
                    stageRef.toBack();
                })
                .build();

        final HBox hbox = new HBox(10, new Label("title:"),
                titleTextField = TextFieldBuilder.create()
                .text("Stage Coach")
                .prefColumnCount(15)
                .build()
        )                ;
        Scene scene = SceneBuilder.create()
                .width(270)
                .height(370)
                .fill(Color.TRANSPARENT)
                .root(rootGroup = new Group(skyBlueRect,
                                VBoxBuilder.create()
                                .layoutX(30)
                                .layoutY(20)
                                .spacing(10)
                                .children(textStageX = TextBuilder.create()
                                        .textOrigin(VPos.TOP)
                                        .build(),
                                        textStageY = TextBuilder.create()
                                        .textOrigin(VPos.TOP)
                                        .build(),
                                        textStageW = TextBuilder.create()
                                        .textOrigin(VPos.TOP)
                                        .build(),
                                        textStageH = TextBuilder.create()
                                        .textOrigin(VPos.TOP)
                                        .build(),
                                        textStageF = TextBuilder.create()
                                        .textOrigin(VPos.TOP)
                                        .build(),
                                        checkBoxResizable = CheckBoxBuilder.create()
                                        .text("resizable")
                                        .disable(stageStyle == StageStyle.TRANSPARENT
                                                || stageStyle == StageStyle.UNDECORATED)
                                        .build(),
                                        checkBoxFullScreen = new CheckBox("fullScreen"), hbox, toBackButton, toFrontButton, closeButton)
                                .build()
                        )
                )
                .build();
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
        textStageH.textProperty().bind(new SimpleStringProperty("height: ").concat(stageRef.heightProperty().asString()));
        textStageF.textProperty().bind(new SimpleStringProperty("focused: ").concat(stageRef.focusedProperty().asString()));
        stage.setResizable(true);
        checkBoxResizable.selectedProperty()                .bindBidirectional(stage.resizableProperty());
        checkBoxFullScreen.selectedProperty().addListener((ov, oldValue, newValue) -> stageRef.setFullScreen(checkBoxFullScreen.selectedProperty().getValue()));
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
