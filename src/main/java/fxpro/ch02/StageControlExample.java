package fxpro.ch02;

import static utils.CommonsFX.onCloseWindow;

import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleTextBuilder;
import simplebuilder.SimpleVBoxBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ex.HasLogging;

public class StageControlExample extends Application {

    private static final Logger LOG = HasLogging.log();
    private StringProperty title = new SimpleStringProperty();
    private Text textStageX = new SimpleTextBuilder().id("textStageX").textOrigin(VPos.TOP).build();
    private Text textStageY = new SimpleTextBuilder().id("textStageY").textOrigin(VPos.TOP).build();
    private Text textStageW = new SimpleTextBuilder().id("textStageW").textOrigin(VPos.TOP).build();
    private Text textStageH = new SimpleTextBuilder().id("textStageH").textOrigin(VPos.TOP).build();
    private Text textStageF = new SimpleTextBuilder().id("textStageF").textOrigin(VPos.TOP).build();
    private CheckBox checkBoxFullScreen = new CheckBox("fullScreen");
    private double dragAnchorX;
    private double dragAnchorY;

    @Override
    public void start(Stage stage) {
        checkBoxFullScreen.setId("checkBoxFullScreen");
        StageStyle stageStyle = StageStyle.TRANSPARENT;
        Parameters parameters = getParameters();
        if (parameters != null) {
            List<String> unnamedParams = parameters.getUnnamed();
            if (!unnamedParams.isEmpty()) {
                String stageStyleParam = unnamedParams.get(0);
                stageStyle = Stream.of(StageStyle.values()).filter(e -> e.name().equalsIgnoreCase(stageStyleParam))
                    .findFirst().orElse(stageStyle);
            }
        }
        TextField textField = new TextField("Stage Coach");
        textField.setPrefColumnCount(10);
        TextField titleField = textField;
        final Button toFrontButton = SimpleButtonBuilder.newButton("toFront()", e -> stage.toFront());
        final Button closeButton = SimpleButtonBuilder.newButton("close()", e -> stage.close());
        final Button toBackButton = SimpleButtonBuilder.newButton("toBack()", e -> stage.toBack());
        final VBox hbox = new VBox(new Label("title:"), titleField);
        CheckBox checkBox = new CheckBox("resizable");
        checkBox.setDisable(stageStyle == StageStyle.TRANSPARENT || stageStyle == StageStyle.UNDECORATED);
        StackPane rootGroup = new StackPane(
            new SimpleVBoxBuilder().spacing(10).children(textStageX, textStageY, textStageW, textStageH, textStageF,
                checkBox, checkBoxFullScreen, hbox, toBackButton, toFrontButton, closeButton).build());
        Scene scene = new Scene(rootGroup, Color.TRANSPARENT);
        CommonsFX.addCSS(scene, "stageControl.css");
        scene.setFill(Color.TRANSPARENT);
        // When mouse button is pressed, save the initial position of screen
        rootGroup.setOnMousePressed((MouseEvent me) -> {
            dragAnchorX = me.getScreenX() - stage.getX();
            dragAnchorY = me.getScreenY() - stage.getY();
        });
        // When screen is dragged, translate it accordingly

        rootGroup.setOnMouseDragged((MouseEvent me) -> {
            stage.setX(me.getScreenX() - dragAnchorX);
            stage.setY(me.getScreenY() - dragAnchorY);
        });
        textStageX.textProperty().bind(new SimpleStringProperty("x: ").concat(stage.xProperty().asString()));
        textStageY.textProperty().bind(new SimpleStringProperty("y: ").concat(stage.yProperty().asString()));
        textStageW.textProperty().bind(new SimpleStringProperty("width: ").concat(stage.widthProperty().asString()));
        textStageH.textProperty()
            .bind(new SimpleStringProperty("height: ").concat(stage.heightProperty().asString()));
        textStageF.textProperty()
            .bind(new SimpleStringProperty("focused: ").concat(stage.focusedProperty().asString()));
        stage.setResizable(true);
        checkBox.selectedProperty().bindBidirectional(stage.resizableProperty());
        checkBoxFullScreen.selectedProperty().addListener(
            (ov, oldValue, newValue) -> stage.setFullScreen(checkBoxFullScreen.selectedProperty().getValue()));
        title.bind(titleField.textProperty());
        stage.setScene(scene);
        title.addListener((ob, old, val) -> stage.setTitle(val));

        Boolean fieldValue = (Boolean) ClassReflectionUtils.getFieldValue(stage, "hasBeenVisible");
        if (!fieldValue) {
            stage.initStyle(stageStyle);
        }
        onCloseWindow(stage, () -> LOG.info("Stage is closing"));
        stage.show();
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4);

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
