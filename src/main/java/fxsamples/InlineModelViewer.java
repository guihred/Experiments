package fxsamples;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class InlineModelViewer extends Application {

	private static final int VIEWPORT_SIZE = 800;

	private static final double MODEL_SCALE_FACTOR = 40;
	private static final double MODEL_X_OFFSET = 0;
	private static final double MODEL_Y_OFFSET = 0;
	private static final double MODEL_Z_OFFSET = (double) VIEWPORT_SIZE / 2;

	private static final String TEXTURE_LOC = "file:wall.jpg";

	private Image texture;
	private PhongMaterial texturedMaterial = new PhongMaterial();

	private MeshView meshView = loadMeshView();

	private MeshView loadMeshView() {
		float[] points = { -5, 5, 0, -5, -5, 0, 5, 5, 0, 5, -5, 0 };
		float[] texCoords = { 1, 1, 1, 0, 0, 1, 0, 0 };
		int[] faces = { 2, 2, 1, 1, 0, 0, 2, 2, 3, 3, 1, 1 };

		TriangleMesh mesh = new TriangleMesh();
		mesh.getPoints().setAll(points);
		mesh.getTexCoords().setAll(texCoords);
		mesh.getFaces().setAll(faces);

		return new MeshView(mesh);
	}

	private Group buildScene() {
		meshView.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
		meshView.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET);
		meshView.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET);
		meshView.setScaleX(MODEL_SCALE_FACTOR);
		meshView.setScaleY(MODEL_SCALE_FACTOR);
		meshView.setScaleZ(MODEL_SCALE_FACTOR);

		return new Group(meshView);
	}

	@Override
	public void start(Stage stage) {
		texture = new Image(TEXTURE_LOC);
		texturedMaterial.setDiffuseMap(texture);

		Group group = buildScene();

		RotateTransition rotate = rotate3dGroup(group);

		VBox layout = new VBox(createControls(rotate), createScene3D(group));

		stage.setTitle("Model Viewer");

		Scene scene = new Scene(layout, Color.CORNSILK);
		stage.setScene(scene);
		stage.show();
	}

	private SubScene createScene3D(Group group) {
		SubScene scene3d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true,
				SceneAntialiasing.BALANCED);
		scene3d.setFill(Color.rgb(10, 10, 40));
		scene3d.setCamera(new PerspectiveCamera());
		return scene3d;
	}

	private VBox createControls(RotateTransition rotateTransition) {
		CheckBox cull = new CheckBox("Cull Back");
		meshView.cullFaceProperty()
				.bind(Bindings.when(cull.selectedProperty()).then(CullFace.BACK).otherwise(CullFace.NONE));
		CheckBox wireframe = new CheckBox("Wireframe");
		meshView.drawModeProperty()
				.bind(Bindings.when(wireframe.selectedProperty()).then(DrawMode.LINE).otherwise(DrawMode.FILL));

		CheckBox rotate = new CheckBox("Rotate");
		rotate.selectedProperty().addListener(observable -> {
			if (rotate.isSelected()) {
				rotateTransition.play();
			} else {
				rotateTransition.pause();
			}
		});

		CheckBox textureCheck = new CheckBox("Texture");
		meshView.materialProperty()
				.bind(Bindings.when(textureCheck.selectedProperty()).then(texturedMaterial)
						.otherwise((PhongMaterial) null));

		VBox controls = new VBox(10, rotate, textureCheck, cull, wireframe);
		controls.setPadding(new Insets(10));
		return controls;
	}

	private RotateTransition rotate3dGroup(Group group) {
		RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
		rotate.setAxis(Rotate.Y_AXIS);
		rotate.setFromAngle(0);
		rotate.setToAngle(360);
		rotate.setInterpolator(Interpolator.LINEAR);
		rotate.setCycleCount(Animation.INDEFINITE);

		return rotate;
	}

	public static void main(String[] args) {
		launch(args);
	}

}