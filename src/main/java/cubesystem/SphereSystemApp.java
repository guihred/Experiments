package cubesystem;

import java.util.stream.Stream;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SphereSystemApp extends Application {

    private Timeline animation;

    public Parent createContent() {
        CubeXForm sceneRoot = new CubeXForm();
        CubeXForm cube1X = new CubeXForm();
        Xsphere cube1 = new Xsphere(40, new Color(1.0, 0.9, 0.0, 1.0));
        CubeXForm cube11X = new CubeXForm();
        CubeXForm cube12X = new CubeXForm();
        CubeXForm cube13X = new CubeXForm();
        CubeXForm cube14X = new CubeXForm();
        CubeXForm cube15X = new CubeXForm();
        CubeXForm cube16X = new CubeXForm();
        CubeXForm cube17X = new CubeXForm();
        CubeXForm cube18X = new CubeXForm();
        CubeXForm cube19X = new CubeXForm();
        Xsphere cube11 = new Xsphere(4, Color.RED);
        Xsphere cube12 = new Xsphere(5, Color.ORANGE);
        Xsphere cube13 = new Xsphere(6, Color.CORNFLOWERBLUE);
        Xsphere cube14 = new Xsphere(7, Color.DARKGREEN);
        Xsphere cube15 = new Xsphere(8, Color.BLUE);
        Xsphere cube16 = new Xsphere(9, Color.PURPLE);
        Xsphere cube17 = new Xsphere(10, Color.BLUEVIOLET);
        Xsphere cube18 = new Xsphere(11, Color.DARKGOLDENROD);
        Xsphere cube19 = new Xsphere(12, Color.KHAKI);
        sceneRoot.getChildren().add(cube1X);
        cube1X.getChildren().addAll(cube1, cube11X, cube12X, cube13X, cube14X, cube15X, cube16X, cube17X, cube18X,
                cube19X);
        cube11X.getChildren().add(cube11);
        cube12X.getChildren().add(cube12);
        cube13X.getChildren().add(cube13);
        cube14X.getChildren().add(cube14);
        cube15X.getChildren().add(cube15);
        cube16X.getChildren().add(cube16);
        cube17X.getChildren().add(cube17);
        cube18X.getChildren().add(cube18);
        cube19X.getChildren().add(cube19);
        cube11.setTranslateX(40.0);
        cube12.setTranslateX(60.0);
		cube13.setTranslateX(80.0);
		cube14.setTranslateX(100.0);
		cube15.setTranslateX(120.0);
		cube16.setTranslateX(140.0);
		cube17.setTranslateX(160.0);
		cube18.setTranslateX(180.0);
		cube19.setTranslateX(200.0);
		cube11X.getRx().setAngle(30.0);
		cube12X.getRz().setAngle(10.0);
		cube13X.getRz().setAngle(50.0);
		cube14X.getRz().setAngle(170.0);
		cube15X.getRz().setAngle(60.0);
		cube16X.getRz().setAngle(30.0);
		cube17X.getRz().setAngle(120.0);
		cube18X.getRz().setAngle(40.0);
		cube19X.getRz().setAngle(-60.0);
		// Animate
		animation = new Timeline();

        KeyValue[] valueAtZero = valuesAtZero(
                new CubeXForm[] { cube1X, cube11X, cube12X, cube13X, cube14X, cube15X, cube16X, cube17X, cube18X,
                        cube19X },
                new Xsphere[] { cube11, cube12, cube13, cube14, cube15, cube16, cube17, cube18, cube19 });

		KeyFrame keyFrame = new KeyFrame(Duration.ZERO, valueAtZero);
		KeyValue[] valuesAt4 = valuesAt42(cube1X, cube11X, cube12X, cube13X, cube14X, cube15X, cube16X, cube17X,
				cube18X, cube19X, cube11, cube12, cube13, cube14, cube15, cube16, cube17, cube18, cube19);
		KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(4), valuesAt4);
		animation.getKeyFrames().addAll(keyFrame, keyFrame2);
		animation.setCycleCount(Animation.INDEFINITE);
		animation.setAutoReverse(false);
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(1500);
		camera.getTransforms().add(new Translate(0, 0, -900));
		SubScene subScene = new SubScene(sceneRoot, 640, 480, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		return new Group(subScene);
	}

	public void play() {
        animation.play();
    }

	@Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent(), 640, 480, true, SceneAntialiasing.BALANCED));
        primaryStage.show();
        play();
    }

	@Override
    public void stop() {
        animation.pause();
    }

    public KeyValue[] valuesAtZero(CubeXForm[] cube19X, Xsphere[] cube11) {
        return Stream
                .concat(Stream.of(cube19X).map(e -> new KeyValue(e.getRy().angleProperty(), 0.0)),
                        Stream.of(cube11).map(e -> new KeyValue(e.getRx().angleProperty(), 0.0)))
                .toArray(KeyValue[]::new);
	}

    private KeyValue[] valuesAt42(CubeXForm cube1X, CubeXForm cube11X, CubeXForm cube12X, CubeXForm cube13X,
			CubeXForm cube14X, CubeXForm cube15X, CubeXForm cube16X, CubeXForm cube17X, CubeXForm cube18X,
			CubeXForm cube19X, Xsphere... spheres) {
		Stream<KeyValue> array = Stream.of(spheres).map(c -> new KeyValue(c.getRx().angleProperty(), 7200));
		KeyValue[] keyValues = new KeyValue[] { new KeyValue(cube1X.getRy().angleProperty(), 360.0),
				new KeyValue(cube1X.getRx().angleProperty(), 360.0),
				new KeyValue(cube11X.getRy().angleProperty(), -2880.0),
				new KeyValue(cube12X.getRy().angleProperty(), -1440.0),
				new KeyValue(cube13X.getRy().angleProperty(), -1080.0),
				new KeyValue(cube14X.getRy().angleProperty(), -720.0),
				new KeyValue(cube15X.getRy().angleProperty(), 1440.0),
				new KeyValue(cube16X.getRy().angleProperty(), 1080.0),
				new KeyValue(cube17X.getRy().angleProperty(), -360.0),
				new KeyValue(cube18X.getRy().angleProperty(), -720.0),
				new KeyValue(cube19X.getRy().angleProperty(), -1080.0) };
		return Stream.concat(Stream.of(keyValues), array).toArray(KeyValue[]::new);
	}

    /**
     * Java main for when running without JavaFX launcher
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
