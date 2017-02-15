package gaming.ex06;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.EventTarget;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape3D;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import simplebuilder.SimpleCircleBuilder;

public class QuartoLauncher extends Application {

    private final Group root = new Group();
    private final Xform world = new Xform();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private QuartoModel model = new QuartoModel();
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private final double cameraDistance = 550;
    private final double CONTROL_MULTIPLIER = 0.1;
    private final double SHIFT_MULTIPLIER = 0.1;
    private final double ALT_MULTIPLIER = 0.5;


    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(0.2);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.ry.setAngle(315.0);
        cameraXform.rx.setAngle(45);
    }

	private void handleMouseClick(MouseEvent event) {
		final EventTarget target = event.getTarget();
		if (target instanceof Shape3D) {
		    final Parent parent = ((Shape3D) target).getParent();
			if (parent instanceof QuartoPiece
					&& Stream.of(model.getMapQuarto()).flatMap(Stream::of).noneMatch(parent::equals)) {
				((QuartoPiece) parent).setSelected(true);
				model.getPieces().stream().filter(p -> !Objects.equals(p, parent) && p.isSelected())
						.forEach((QuartoPiece p) -> p.setSelected(false));
			}
		}
		if (target instanceof Circle && Stream.of(model.getMap()).flatMap(Stream::of).anyMatch(target::equals)) {
			List<QuartoPiece>selectedPiece=model.getPieces().stream().filter(p -> p.isSelected()).collect(Collectors.toList());
			for(QuartoPiece p:selectedPiece) {
		        p.setTranslateX(((Circle) target).getTranslateX());
		        p.setTranslateZ(((Circle) target).getTranslateZ());
				p.setSelected(false);
		        for (int i = 0; i < 4; i++) {
		            for (int j = 0; j < 4; j++) {
		                if (target == model.getMap()[i][j]) {
		                    model.getMapQuarto()[i][j] = p;
		                }
		            }
		        }
		        if (model.checkEnd()) {
		            System.out.println("ACABOU");
		            final Text text = new Text("You Got " + 0 + " points");
		            final Button button = new Button("Reset");
		            final Stage stage1 = new Stage();
		            button.setOnAction(a -> {
		                model.reset();
		                stage1.close();
		            });

		            final Group group = new Group(text, button);
		            group.setLayoutX(50);
		            group.setLayoutY(50);
		            stage1.setScene(new Scene(group));
		            stage1.show();

		        }
			}

		}
	}


	private void handleKeyPressed(KeyEvent event) {
		switch (event.getCode()) {
		    case Z:
		        if (event.isShiftDown()) {
		            cameraXform.ry.setAngle(0.0);
		            cameraXform.rx.setAngle(0.0);
		            camera.setTranslateZ(-cameraDistance);
		        }
		        cameraXform2.t.setX(0.0);
		        cameraXform2.t.setY(0.0);
		        break;
		    case UP:
			moveUpAndDown(event, 1);
		        break;
		    case DOWN:
			moveUpAndDown(event, -1);
		        break;
		    case RIGHT:
			moveSideways(event, 1);
		        break;
		    case LEFT:
			moveSideways(event, -1);
		        break;
		default:
			break;
		}
	}




	private void moveUpAndDown(KeyEvent event, int multiplier) {
		if (event.isControlDown() && event.isShiftDown()) {
			cameraXform2.t.setY(cameraXform2.t.getY() - multiplier * 10.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown() && event.isShiftDown()) {
			cameraXform.rx.setAngle(cameraXform.rx.getAngle() - multiplier * 10.0 * ALT_MULTIPLIER);
		} else if (event.isControlDown()) {
			cameraXform2.t.setY(cameraXform2.t.getY() - multiplier * 1.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown()) {
			cameraXform.rx.setAngle(cameraXform.rx.getAngle() - multiplier * 2.0 * ALT_MULTIPLIER);
		} else if (event.isShiftDown()) {
		    double z = camera.getTranslateZ();
			double newZ = z + multiplier * 5.0 * SHIFT_MULTIPLIER;
		    camera.setTranslateZ(newZ);
		}
	}




	private void moveSideways(KeyEvent event, int multiplier) {
		if (event.isControlDown() && event.isShiftDown()) {
			cameraXform2.t.setX(cameraXform2.t.getX() + multiplier * 10.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown() && event.isShiftDown()) {
			cameraXform.ry.setAngle(cameraXform.ry.getAngle() - multiplier * 10.0 * ALT_MULTIPLIER); // -
		} else if (event.isControlDown()) {
			cameraXform2.t.setX(cameraXform2.t.getX() + multiplier * 1.0 * CONTROL_MULTIPLIER);
		} else if (event.isAltDown()) {
			cameraXform.ry.setAngle(cameraXform.ry.getAngle() - multiplier * 2.0 * ALT_MULTIPLIER); // -
		}
	}
    private void buildAxes() {
        final PhongMaterial blackMaterial = new PhongMaterial();
        blackMaterial.setDiffuseColor(Color.BLACK);
        blackMaterial.setSpecularColor(Color.GRAY);
        final PhongMaterial grayMaterial = new PhongMaterial();
        grayMaterial.setDiffuseColor(Color.DARKVIOLET);
        grayMaterial.setSpecularColor(Color.BLACK);
        final PhongMaterial whiteMaterial = new PhongMaterial();
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.WHITE);
        final Box board = new Box(240.0, 1, 240.0);
        board.setMaterial(blackMaterial);
        final Circle cs = new SimpleCircleBuilder()
        		.radius(110)
				.fill(Color.BLACK)
        		.translateY(2)
        		.rotationAxis(Rotate.X_AXIS)
        		.rotate(90)
        		.build();
        final Circle cs2 = new Circle(120, Color.WHITE);
        cs2.setTranslateY(1);
        cs2.setRotationAxis(Rotate.X_AXIS);
        cs2.setRotate(90);
        final Group group = new Group(board, cs2, cs);
        for (int i = 0; i < 16; i++) {
            final Circle circle = new SimpleCircleBuilder()
            		.radius(10)
    				.fill(Color.WHITE)
    				.translateX(i % 4 * 40 - 60)
    				.translateZ(i / 4 * 40 - 60)
    				.translateY(3)
    				.rotationAxis(Rotate.X_AXIS)
    				.rotate(90)
    				.build();
            circle.fillProperty().bind(Bindings.when(circle.hoverProperty()).then(Color.BLUE).otherwise(Color.WHITE));
            model.getMap()[i % 4][i / 4] = circle;
            group.getChildren().add(circle);

            final QuartoPiece piece = new QuartoPiece(i);
                int j = i % 4;
                int k = i / 4;
            piece.setTranslateX(j == 0 ? -110 : j == 1 ? -90 : j == 2 ? 90 : 110);
            piece.setTranslateZ(k == 0 ? -110 : k == 1 ? -90 : k == 2 ? 90 : 110);
            world.getChildren().add(piece);
            model.getPieces().add(piece);

        }

        world.getChildren().addAll(group);
    }
    @Override
    public void start(Stage primaryStage) {
        root.getChildren().add(world);
        buildCamera();
        buildAxes();
        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.GREY);
		scene.setOnKeyPressed(event -> handleKeyPressed(event));
		scene.setOnMouseClicked(event -> handleMouseClick(event));
		scene.setOnMouseDragReleased(null);

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

