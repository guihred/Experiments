package gaming.ex15;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import utils.MouseInScreenHandler;

public class RubiksCubeLauncher extends Application {
	private RubiksModel model = new RubiksModel();



    @Override
	public void start(Stage stage) throws Exception {
		Group root = new Group();
		model.extracted(root);
        SubScene subScene = new SubScene(root, 500, 500, true, SceneAntialiasing.BALANCED);
		subScene.heightProperty().bind(stage.heightProperty());
		subScene.widthProperty().bind(stage.widthProperty());
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(1000.0);
		final int distance = 200;
        camera.setTranslateX(-distance);
		camera.setTranslateZ(-distance);
		camera.setRotationAxis(Rotate.Y_AXIS);
        camera.setNearClip(2. / 10);
        final int fieldOfView = 40;
        camera.setFieldOfView(fieldOfView);
		subScene.setCamera(camera);


		Scene sc = new Scene(new Group(subScene));
		stage.setScene(sc);
		stage.show();

		RubiksKeyboard value = new RubiksKeyboard(camera, model);
		sc.setOnKeyPressed(value);
		sc.setOnKeyReleased(value::keyReleased);
        sc.setOnMouseMoved(new MouseInScreenHandler(sc, camera));
		model.setPivot();
	}



	public static void main(String[] args) {
		launch(args);
	}


}
