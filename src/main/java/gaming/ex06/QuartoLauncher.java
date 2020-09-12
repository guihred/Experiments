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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleDialogBuilder;
import utils.Xform;
import utils.ex.HasLogging;

public class QuartoLauncher extends Application {

    private static final int HEIGHT = 700;
    private static final int WIDTH = 1000;
    private static final double ALT_MULTIPLIER = 0.5;
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 0.1;
    private static final double CAMERA_DISTANCE = 550;
    private static final Logger LOG = HasLogging.log();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private final QuartoModel model = new QuartoModel();
    private final Group root = new Group();

    private final Xform world = new Xform();

    @Override
    public void start(Stage primaryStage) {
        cameraXform.toString();
        root.getChildren().add(world);
        buildCamera();
        buildAxes();
        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.GREY);
        scene.setOnKeyPressed(this::handleKeyPressed);
        scene.setOnMouseClicked(this::handleMouseClick);
        scene.setOnMouseDragReleased(null);

        primaryStage.setTitle("Quarto Application");
        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setCamera(camera);

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
        final Box board = new Box(240, 1, 240);
        board.setMaterial(blackMaterial);
        final Circle cs = new SimpleCircleBuilder().radius(110).fill(Color.BLACK).translateY(2)
            .rotationAxis(Rotate.X_AXIS).rotate(90).build();
        final Circle cs2 = new Circle(120, Color.WHITE);
        cs2.setTranslateY(1);
        cs2.setRotationAxis(Rotate.X_AXIS);
        cs2.setRotate(90);
        final Group group = new Group(board, cs2, cs);
        for (int i = 0; i < 16; i++) {
            final Circle circle = new SimpleCircleBuilder().radius(10).fill(Color.WHITE).translateX(getPosition(i % 4))
                .translateZ(getPosition(i / 4)).translateY(3).rotationAxis(Rotate.X_AXIS).rotate(90).build();
            circle.fillProperty().bind(Bindings.when(circle.hoverProperty()).then(Color.BLUE).otherwise(Color.WHITE));
            model.getMap()[i % 4][i / 4] = circle;
            group.getChildren().add(circle);

            final QuartoPiece piece = new QuartoPiece(i);
            int j = i % 4;
            int k = i / 4;
            piece.setTranslateX(QuartoModel.getTranslate(j));
            piece.setTranslateZ(QuartoModel.getTranslate(k));
            world.getChildren().add(piece);
            model.getPieces().add(piece);

        }

        world.getChildren().addAll(group);
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRz(180);

        final double nearClip = 0.2;
        camera.setNearClip(nearClip);
        final int farClip = 10000;
        camera.setFarClip(farClip);
        camera.setTranslateZ(-CAMERA_DISTANCE);
        final int yRotation = 315;
        cameraXform.setRy(yRotation);
        final int xRotation = 45;
        cameraXform.setRx(xRotation);
    }

    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case Z:
                reset(event);
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
            List<QuartoPiece> selectedPiece = model.getPieces().stream().filter(QuartoPiece::isSelected)
                .collect(Collectors.toList());
            for (QuartoPiece p : selectedPiece) {
                p.setTranslateX(((Circle) target).getTranslateX());
                p.setTranslateZ(((Circle) target).getTranslateZ());
                p.setSelected(false);
                setQuartoPiece(target, p);
                if (model.checkEnd()) {
                    LOG.info("{}", "ACABOU");
                    new SimpleDialogBuilder().text("You Got " + 0 + " points").button("Reset", model::reset)
                        .bindWindow(p)
                        .displayDialog();

                }
            }

        }
    }

    private void moveSideways(KeyEvent event, int multiplier) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + multiplier * 10 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRy(cameraXform.getRotateY() - multiplier * 10 * ALT_MULTIPLIER); // -
        } else if (event.isControlDown()) {
            cameraXform2.setTx(cameraXform2.getTx() + multiplier * 1 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRy(cameraXform.getRotateY() - multiplier * 2 * ALT_MULTIPLIER); // -
        }
    }

    private void moveUpAndDown(KeyEvent event, int multiplier) {
        if (event.isControlDown() && event.isShiftDown()) {
            cameraXform2.setTy(cameraXform2.getTy() - multiplier * 10 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown() && event.isShiftDown()) {
            cameraXform.setRx(cameraXform.getRotateX() - multiplier * 10 * ALT_MULTIPLIER);
        } else if (event.isControlDown()) {
            cameraXform2.setTy(cameraXform2.getTy() - multiplier * 1 * CONTROL_MULTIPLIER);
        } else if (event.isAltDown()) {
            cameraXform.setRx(cameraXform.getRotateX() - multiplier * 2 * ALT_MULTIPLIER);
        } else if (event.isShiftDown()) {
            double z = camera.getTranslateZ();
            double newZ = z + multiplier * 5 * SHIFT_MULTIPLIER;
            camera.setTranslateZ(newZ);
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

    private void setQuartoPiece(final EventTarget target, QuartoPiece p) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (target == model.getMap()[i][j]) {
                    model.getMapQuarto()[i][j] = p;
                }
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }

    private static int getPosition(int i) {
        final int spacing = 40;
        return i * spacing - 60;
    }
}
