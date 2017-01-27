package gaming.ex06;

import java.util.Objects;
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
import others.SimpleCircleBuilder;

public class QuartoLauncher extends Application {

    private final Group root = new Group();
    private final Xform world = new Xform();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    QuartoModel model = new QuartoModel();
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

    


    private void handleMouse(Scene scene) {
        scene.setOnMouseClicked((MouseEvent event) -> {
            final EventTarget target = event.getTarget();
            if (target instanceof Shape3D) {
                final Parent parent = ((Shape3D) target).getParent();
                if (parent instanceof QuartoPiece) {
                    if (Stream.of(model.mapQuarto).flatMap(Stream::of).noneMatch(parent::equals)) {
                        ((QuartoPiece) parent).selected.set(true);
                        model.pieces.stream().filter(p -> !Objects.equals(p, parent) && p.selected.get()).forEach(
                                (QuartoPiece p) -> p.selected.setValue(false)
                        );
                    }
                }
            }
            if (target instanceof Circle && Stream.of(model.map).flatMap(Stream::of).anyMatch(target::equals)) {
                model.pieces.stream().filter(p -> p.selected.get()).forEach((QuartoPiece p) -> {
                    p.setTranslateX(((Circle) target).getTranslateX());
                    p.setTranslateZ(((Circle) target).getTranslateZ());
                    p.selected.set(false);
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            if (target == model.map[i][j]) {
                                model.mapQuarto[i][j] = p;
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
                });

            }
        });


        scene.setOnMouseDragReleased(null);
    }


    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed((KeyEvent event) -> {
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
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                    } else if (event.isShiftDown()) {
                        double z = camera.getTranslateZ();
                        double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                        camera.setTranslateZ(newZ);
                    }
                    break;
                case DOWN:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                    } else if (event.isShiftDown()) {
                        double z = camera.getTranslateZ();
                        double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                        camera.setTranslateZ(newZ);
                    }
                    break;
                case RIGHT:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                    }
                    break;
                case LEFT:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                    }
                    break;
			default:
				break;
            }
        });
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
            model.map[i % 4][i / 4] = circle;
            group.getChildren().add(circle);

            final QuartoPiece piece = new QuartoPiece(i);
                int j = i % 4;
                int k = i / 4;
            piece.setTranslateX(j == 0 ? -110 : j == 1 ? -90 : j == 2 ? 90 : 110);
            piece.setTranslateZ(k == 0 ? -110 : k == 1 ? -90 : k == 2 ? 90 : 110);
            world.getChildren().add(piece);
            model.pieces.add(piece);

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
        handleKeyboard(scene);
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

