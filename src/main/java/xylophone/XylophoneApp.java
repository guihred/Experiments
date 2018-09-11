package xylophone;

import fxsamples.Xform;
import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.ResourceFXUtils;
import simplebuilder.SimpleTimelineBuilder;

public class XylophoneApp extends Application {

	private Xform sceneRoot = new Xform();
	private Timeline animation = new SimpleTimelineBuilder().keyFrames(
			new KeyFrame(Duration.ZERO,
					new KeyValue(sceneRoot.rotateYProperty(), 390D,
							Interpolator.TANGENT(Duration.seconds(0.5), 390D, Duration.seconds(0.5), 390D))),
			new KeyFrame(Duration.seconds(2), new KeyValue(sceneRoot.rotateYProperty(), 30D,
					Interpolator.TANGENT(Duration.seconds(0.5), 30D, Duration.seconds(0.5), 30D))))
			.build();
	private Timeline animation2 = new SimpleTimelineBuilder().cycleCount(Animation.INDEFINITE).keyFrames(
			new KeyFrame(Duration.ZERO,
					new KeyValue(sceneRoot.rotateXProperty(), 60D, Interpolator.TANGENT(Duration.seconds(1.0), 60D))),
			new KeyFrame(Duration.seconds(4),
					new KeyValue(sceneRoot.rotateXProperty(), 80D, Interpolator.TANGENT(Duration.seconds(1.0), 80D))),
			new KeyFrame(Duration.seconds(8),
					new KeyValue(sceneRoot.rotateXProperty(), 60D, Interpolator.TANGENT(Duration.seconds(1.0), 60D))))
			.build();
	// I didn't have any xylophone sounds so I added piano sounds :P
    private final AudioClip bar1Note = new AudioClip(ResourceFXUtils.toURL("waves/C.wav").toString());
    private final AudioClip bar2Note = new AudioClip(ResourceFXUtils.toURL("waves/D.wav").toString());
    private final AudioClip bar3Note = new AudioClip(ResourceFXUtils.toURL("waves/E.wav").toString());
    private final AudioClip bar4Note = new AudioClip(ResourceFXUtils.toURL("waves/F.wav").toString());
    private final AudioClip bar5Note = new AudioClip(ResourceFXUtils.toURL("waves/G.wav").toString());
    private final AudioClip bar6Note = new AudioClip(ResourceFXUtils.toURL("waves/A.wav").toString());
    private final AudioClip bar7Note = new AudioClip(ResourceFXUtils.toURL("waves/B.wav").toString());
    private final AudioClip bar8Note = new AudioClip(ResourceFXUtils.toURL("waves/mC.wav").toString());
    private static final double X_START = -110.0;
    private static final double X_OFFSET = 30.0;
    private static final double Y_POS = 25.0;
    private static final double BAR_WIDTH = 22.0;
    private static final double BAR_DEPTH = 7.0;

	public Parent createContent() {
		sceneRoot.setRx(45.0);
		sceneRoot.setRy(30.0);
		sceneRoot.setScale(2 * 1.5);


		Group rectangleGroup = new Group();

		// Base1
        Box base1Cube = new Box(BAR_WIDTH * 11.5, BAR_DEPTH * 2.0, 10.0);
        base1Cube.setMaterial(new PhongMaterial(Color.DARKSALMON));
        base1Cube.setTranslateX(X_START + 128);
        base1Cube.setTranslateZ(Y_POS + 20.0);
		base1Cube.setTranslateY(11.0);

		// Base2
        Box base2Cube = new Box(BAR_WIDTH * 11.5, BAR_DEPTH * 2.0, 10.0);
        base2Cube.setMaterial(new PhongMaterial(Color.DARKSALMON));
        base2Cube.setTranslateX(X_START + 128);
        base2Cube.setTranslateZ(Y_POS - 20.0);
		base2Cube.setTranslateY(11.0);

		// Bar1
        Box bar1Cube = new Box(BAR_WIDTH, BAR_DEPTH, 100.0);
		bar1Cube.setMaterial(new PhongMaterial(Color.PURPLE));
        bar1Cube.setTranslateX(X_START + 1 * X_OFFSET);
        bar1Cube.setTranslateZ(Y_POS);

		// Bar2
        Box bar2Cube = new Box(BAR_WIDTH, BAR_DEPTH, 95);
		bar2Cube.setMaterial(new PhongMaterial(Color.BLUEVIOLET));
        bar2Cube.setTranslateX(X_START + 2 * X_OFFSET);
        bar2Cube.setTranslateZ(Y_POS);

		// Bar3
        Box bar3Cube = new Box(BAR_WIDTH, BAR_DEPTH, 90);
		bar3Cube.setMaterial(new PhongMaterial(Color.BLUE));
        bar3Cube.setTranslateX(X_START + 3 * X_OFFSET);
        bar3Cube.setTranslateZ(Y_POS);

		// Bar4
        Box bar4Cube = new Box(BAR_WIDTH, BAR_DEPTH, 85);
		bar4Cube.setMaterial(new PhongMaterial(Color.GREEN));
        bar4Cube.setTranslateX(X_START + 4 * X_OFFSET);
        bar4Cube.setTranslateZ(Y_POS);

		// Bar5
        Box bar5Cube = new Box(BAR_WIDTH, BAR_DEPTH, 80);
		bar5Cube.setMaterial(new PhongMaterial(Color.GREENYELLOW));
        bar5Cube.setTranslateX(X_START + 5 * X_OFFSET);
        bar5Cube.setTranslateZ(Y_POS);
		// Bar6
        Box bar6Cube = new Box(BAR_WIDTH, BAR_DEPTH, 75);
		bar6Cube.setMaterial(new PhongMaterial(Color.YELLOW));
        bar6Cube.setTranslateX(X_START + 6 * X_OFFSET);
        bar6Cube.setTranslateZ(Y_POS);
		// Bar7
        Box bar7Cube = new Box(BAR_WIDTH, BAR_DEPTH, 70);
		bar7Cube.setMaterial(new PhongMaterial(Color.ORANGE));
        bar7Cube.setTranslateX(X_START + 7 * X_OFFSET);
        bar7Cube.setTranslateZ(Y_POS);

		// Bar8
        Box bar8Cube = new Box(BAR_WIDTH, BAR_DEPTH, 65);
		bar8Cube.setMaterial(new PhongMaterial(Color.RED));
        bar8Cube.setTranslateX(X_START + 8 * X_OFFSET);
        bar8Cube.setTranslateZ(Y_POS);

        bar1Cube.setOnMousePressed(me -> bar1Note.play());
        bar2Cube.setOnMousePressed(me -> bar2Note.play());
        bar3Cube.setOnMousePressed(me -> bar3Note.play());
        bar4Cube.setOnMousePressed(me -> bar4Note.play());
        bar5Cube.setOnMousePressed(me -> bar5Note.play());
        bar6Cube.setOnMousePressed(me -> bar6Note.play());
        bar7Cube.setOnMousePressed(me -> bar7Note.play());
        bar8Cube.setOnMousePressed(me -> bar8Note.play());
		rectangleGroup.getChildren().addAll(base1Cube, base2Cube, bar1Cube, bar2Cube, bar3Cube, bar4Cube, bar5Cube,
				bar6Cube, bar7Cube, bar8Cube);
		sceneRoot.getChildren().add(rectangleGroup);
		PerspectiveCamera camera = new PerspectiveCamera();
		SubScene subScene = new SubScene(sceneRoot, 780 * 1.5, 380 * 1.5, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(camera);
		sceneRoot.translateXProperty().bind(subScene.widthProperty().divide(2.2));
		sceneRoot.translateYProperty().bind(subScene.heightProperty().divide(1.6));
		return new Group(subScene);
	}

	public void play() {
		animation.play();
		animation2.play();
	}

	@Override
	public void stop() {
		animation.pause();
		animation2.pause();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(createContent()));
		primaryStage.show();
		play();
	}

	/**
	 * Java main for when running without JavaFX launcher
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
