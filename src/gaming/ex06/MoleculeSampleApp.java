
package gaming.ex06;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

/**
 * MoleculeSampleApp
 */
public class MoleculeSampleApp extends Application {

	private double altMultiplier = 0.5;
	private final Group axisGroup = new Group();
	private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private final double cameraDistance = 450;
	private final Xform cameraXform = new Xform();
	private final Xform cameraXform2 = new Xform();
	private final Xform cameraXform3 = new Xform();
	private double CONTROL_MULTIPLIER = 0.1;
	private final Xform moleculeGroup = new Xform();
	private double mouseDeltaX;
	private double mouseDeltaY;
	private double mouseOldX;
	private double mouseOldY;
	private double mousePosX;
	private double mousePosY;
	private final Group root = new Group();
	private double SHIFT_MULTIPLIER = 0.1;
	private final Xform world = new Xform();
	private Timeline timeline = new SimpleTimelineBuilder()
			.cycleCount(Animation.INDEFINITE)
			.keyFrames(new KeyFrame(Duration.minutes(1), new KeyValue(world.ry.angleProperty(), 360.0)))
			.build();
	private boolean timelinePlaying = false;


    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(240.0, 1, 1);
        final Box yAxis = new Box(1, 240.0, 1);
        final Box zAxis = new Box(1, 1, 240.0);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        world.getChildren().addAll(axisGroup);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.ry.setAngle(320.0);
        cameraXform.rx.setAngle(40);
    }

    private void buildMolecule() {

        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

        final PhongMaterial greyMaterial = new PhongMaterial();
        greyMaterial.setDiffuseColor(Color.DARKGREY);
        greyMaterial.setSpecularColor(Color.GREY);

        // Molecule Hierarchy
        // [*] moleculeXform
        //     [*] oxygenXform
        //         [*] oxygenSphere
        //     [*] hydrogen1SideXform
        //         [*] hydrogen1Xform
        //             [*] hydrogen1Sphere
        //         [*] bond1Cylinder
        //     [*] hydrogen2SideXform
        //         [*] hydrogen2Xform
        //             [*] hydrogen2Sphere
        //         [*] bond2Cylinder

        Xform moleculeXform = new Xform();
        Xform oxygenXform = new Xform();
        Xform hydrogen1SideXform = new Xform();
        Xform hydrogen1Xform = new Xform();
        Xform hydrogen2SideXform = new Xform();
        Xform hydrogen2Xform = new Xform();

        Sphere oxygenSphere = new Sphere(40.0);
        oxygenSphere.setMaterial(redMaterial);

        Sphere hydrogen1Sphere = new Sphere(30.0);
        hydrogen1Sphere.setMaterial(whiteMaterial);
        hydrogen1Sphere.setTranslateX(0.0);

        Sphere hydrogen2Sphere = new Sphere(30.0);
        hydrogen2Sphere.setMaterial(whiteMaterial);
        hydrogen2Sphere.setTranslateZ(0.0);

        Cylinder bond1Cylinder = new Cylinder(5, 100);
        bond1Cylinder.setMaterial(greyMaterial);
        bond1Cylinder.setTranslateX(50.0);
        bond1Cylinder.setRotationAxis(Rotate.Z_AXIS);
        bond1Cylinder.setRotate(90.0);

        Cylinder bond2Cylinder = new Cylinder(5, 100);
        bond2Cylinder.setMaterial(greyMaterial);
        bond2Cylinder.setTranslateX(50.0);
        bond2Cylinder.setRotationAxis(Rotate.Z_AXIS);
        bond2Cylinder.setRotate(90.0);

        moleculeXform.getChildren().add(oxygenXform);
        moleculeXform.getChildren().add(hydrogen1SideXform);
        moleculeXform.getChildren().add(hydrogen2SideXform);
        oxygenXform.getChildren().add(oxygenSphere);
        hydrogen1SideXform.getChildren().add(hydrogen1Xform);
        hydrogen2SideXform.getChildren().add(hydrogen2Xform);
        hydrogen1Xform.getChildren().add(hydrogen1Sphere);
        hydrogen2Xform.getChildren().add(hydrogen2Sphere);
        hydrogen1SideXform.getChildren().add(bond1Cylinder);
        hydrogen2SideXform.getChildren().add(bond2Cylinder);

        hydrogen1Xform.setTx(100.0);
        hydrogen2Xform.setTx(100.0);
        hydrogen2SideXform.setRotateY(104.5);

        moleculeGroup.getChildren().add(moleculeXform);

        world.getChildren().addAll(moleculeGroup);
    }

	private void handleKeyEvent(KeyEvent event) {
		switch (event.getCode()) {
		    case Z:
		        if (event.isShiftDown()) {
		            cameraXform.ry.setAngle(0.0);
		            cameraXform.rx.setAngle(0.0);
		            camera.setTranslateZ(-300.0);
		        }
		        cameraXform2.t.setX(0.0);
		        cameraXform2.t.setY(0.0);
		        break;
		    case X:
		        if (event.isControlDown()) {
		            if (axisGroup.isVisible()) {
		                axisGroup.setVisible(false);
		            } else {
		                axisGroup.setVisible(true);
		            }
		        }
		        break;
		    case S:
		        if (event.isControlDown()) {
		            if (moleculeGroup.isVisible()) {
		                moleculeGroup.setVisible(false);
		            } else {
		                moleculeGroup.setVisible(true);
		            }
		        }
		        break;
		    case SPACE:
		        if (timelinePlaying) {
		            timeline.pause();
		            timelinePlaying = false;
		        } else {
		            timeline.play();
		            timelinePlaying = true;
		        }
		        break;
		    case UP:
			upAndDownMovement(event, -1);
		        break;
		    case DOWN:
			upAndDownMovement(event, 1);
		        break;
		    case RIGHT:
			leftAndRightMovement(event, 1);
		        break;
		    case LEFT:
			leftAndRightMovement(event, -1);
		        break;
		default:
			break;
		}
	}

	private void leftAndRightMovement(KeyEvent event, int i) {
		if (event.isControlDown() && event.isShiftDown()) {
			cameraXform2.t.setX(cameraXform2.t.getX() + i * 10.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown() && event.isShiftDown()) {
			cameraXform.ry.setAngle(cameraXform.ry.getAngle() - i * 10.0 * altMultiplier);
		} else if (event.isControlDown()) {
			cameraXform2.t.setX(cameraXform2.t.getX() + i * 1.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown()) {
			cameraXform.ry.setAngle(cameraXform.ry.getAngle() - i * 2.0 * altMultiplier);
		}
	}

	private void upAndDownMovement(KeyEvent event, int i) {
		if (event.isControlDown() && event.isShiftDown()) {
			cameraXform2.t.setY(cameraXform2.t.getY() + i * 10.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown() && event.isShiftDown()) {
			cameraXform.rx.setAngle(cameraXform.rx.getAngle() + i * 10.0 * altMultiplier);
		} else if (event.isControlDown()) {
			cameraXform2.t.setY(cameraXform2.t.getY() + i * 1.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown()) {
			cameraXform.rx.setAngle(cameraXform.rx.getAngle() + i * 2.0 * altMultiplier);
		} else if (event.isShiftDown()) {
			camera.setTranslateZ(camera.getTranslateZ() - i * 5.0 * SHIFT_MULTIPLIER);
		}
	}

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
		scene.setOnMouseDragged(me -> handleMouseDragged(me));
    }

	private void handleMouseDragged(MouseEvent me) {
		mouseOldX = mousePosX;
		mouseOldY = mousePosY;
		mousePosX = me.getSceneX();
		mousePosY = me.getSceneY();
		mouseDeltaX = mousePosX - mouseOldX;
		mouseDeltaY = mousePosY - mouseOldY;
		double modifier = me.isShiftDown() ? 10.0 : me.isControlDown() ? 0.1 : 1.0;
		double modifierFactor = 0.1;
		if (me.isPrimaryButtonDown()) {
		    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
		    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
		} else if (me.isSecondaryButtonDown()) {
		    double z = camera.getTranslateZ();
		    double newZ = z + mouseDeltaX * modifierFactor * modifier;
		    camera.setTranslateZ(newZ);
		} else if (me.isMiddleButtonDown()) {
		    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
		    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
		}
	}
    
        @Override
    public void start(Stage primaryStage) {
        root.getChildren().add(world);
        buildCamera();
        buildAxes();
        buildMolecule();

        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.GREY);
		scene.setOnKeyPressed(this::handleKeyEvent);
        handleMouse(scene);

        primaryStage.setTitle("Molecule Sample Application");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setCamera(camera);

    }

    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }
}