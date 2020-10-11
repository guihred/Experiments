package fxsamples;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;
import utils.fx.Xform;

public class MoleculeSampleApp extends Application {

    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double CAMERA_DISTANCE = 450;
    private static final double ALT_MULTIPLIER = 0.5;
    @FXML
    private Group axisGroup;
    private PerspectiveCamera camera;
    @FXML
    private Xform cameraXform;
    @FXML
    private Xform cameraXform2;
    @FXML
    private Xform cameraXform3;
    @FXML
    private Xform moleculeGroup;
    private double mouseOldX;
    private double mouseOldY;
    private double mousePosX;
    private double mousePosY;
    @FXML
    private Group root;
    @FXML
    private Xform world;
    @FXML
    private Timeline timeline;
    private boolean timelinePlaying;

    public void initialize() {
        SimpleTimelineBuilder.of(timeline).addKeyFrame(Duration.minutes(1), world.rotateYProperty(), 360);
        camera = new PerspectiveCamera(true);
        final double farClip = 10000.0;
        camera.setFarClip(farClip);
        camera.setNearClip(CONTROL_MULTIPLIER);
        camera.setTranslateZ(-CAMERA_DISTANCE);
        cameraXform3.getChildren().add(camera);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int height = 700;
        CommonsFX.loadFXML("Molecule Sample Application", "MoleculeSampleApp.fxml", this, primaryStage, 1000, height);
        Scene scene = primaryStage.getScene();
        scene.setFill(Color.GREY);
        scene.setOnKeyPressed(this::handleKeyEvent);
        handleMouse(scene);
        scene.setCamera(camera);
    }

    private void handleKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case Z:
                reset(event);
                break;
            case X:
                toggleAxisVisible(event);
                break;
            case S:
                toggleMoleculeVisible(event);
                break;
            case SPACE:
                toggleAnimation();
                break;
            case UP:
                upAndDownMovement(event, -1);
                break;
            case DOWN:
                upAndDownMovement(event, 1);
                return;
            case RIGHT:
                leftAndRightMovement(event, 1);
                return;
            case LEFT:
                leftAndRightMovement(event, -1);
                return;
            default:
                break;
        }
    }

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged(this::handleMouseDragged);
    }

    private void handleMouseDragged(MouseEvent me) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        double mouseDeltaX = mousePosX - mouseOldX;
        double mouseDeltaY = mousePosY - mouseOldY;
        double modifier = getModifier(me);
        double modifierFactor = CONTROL_MULTIPLIER;
        if (me.isPrimaryButtonDown()) {
            cameraXform.setRy(cameraXform.getRotateY() - mouseDeltaX * modifierFactor * modifier * 2); // +
            cameraXform.setRx(cameraXform.getRotateX() + mouseDeltaY * modifierFactor * modifier * 2); // -
        } else if (me.isSecondaryButtonDown()) {
            double z = camera.getTranslateZ();
            double newZ = z + mouseDeltaX * modifierFactor * modifier;
            camera.setTranslateZ(newZ);
        } else if (me.isMiddleButtonDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + mouseDeltaX * modifierFactor * modifier * 3. / 10); // -
            cameraXform2.setTy(cameraXform2.getTy() + mouseDeltaY * modifierFactor * modifier * 3. / 10); // -
        }
    }

    private void leftAndRightMovement(KeyEvent event, int i) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + i * 10 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRy(cameraXform.getRotateY() - i * 10 * ALT_MULTIPLIER);
        } else if (event.isControlDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + i * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRy(cameraXform.getRotateY() - i * 2 * ALT_MULTIPLIER);
        }
    }

    private void reset(KeyEvent event) {
        if (event.isShiftDown()) {
            cameraXform.setRy(0);
            cameraXform.setRx(0);
            camera.setTranslateZ(-CAMERA_DISTANCE);
        }
        cameraXform2.setTx(0);
        cameraXform2.setTy(0);
    }

    private void toggleAnimation() {
        if (timelinePlaying) {
            timeline.pause();
        } else {
            timeline.play();
        }
        timelinePlaying = !timelinePlaying;
    }

    private void toggleAxisVisible(KeyEvent event) {
        if (event.isControlDown()) {
            axisGroup.setVisible(!axisGroup.isVisible());
        }
    }

    private void toggleMoleculeVisible(KeyEvent event) {
        if (event.isControlDown()) {
            moleculeGroup.setVisible(!moleculeGroup.isVisible());
        }
    }

    private void upAndDownMovement(KeyEvent event, int i) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTy(cameraXform2.getTy() + i * 10 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRx(cameraXform.getRotateX() + i * 10 * ALT_MULTIPLIER);
        } else if (event.isControlDown()) {
            cameraXform2.setTy(cameraXform2.getTy() + i * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRx(cameraXform.getRotateX() + i * 2 * ALT_MULTIPLIER);
        } else if (event.isShiftDown()) {
            camera.setTranslateZ(camera.getTranslateZ() - i * 5 * CONTROL_MULTIPLIER);
        }
    }

    public static void main(String[] args) {

        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }

    private static double getModifier(MouseEvent me) {
        if (me.isShiftDown()) {
            return 10;
        }
        if (me.isControlDown()) {
            return CONTROL_MULTIPLIER;
        }
        return 1;
    }
}
