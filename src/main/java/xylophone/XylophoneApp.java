package xylophone;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;
import utils.ResourceFXUtils;
import utils.Xform;

public class XylophoneApp extends Application {

	private static final double X_START = -110.0;
    private static final double X_OFFSET = 30.0;
    private static final double Y_POS = 25.0;
	private static final double BAR_WIDTH = 22.0;
    private static final double BAR_DEPTH = 7.0;
    private Xform sceneRoot = new Xform();
    private Timeline animation = new SimpleTimelineBuilder()
            .addKeyFrame(Duration.ZERO,
                    new KeyValue(sceneRoot.rotateYProperty(), 390D,
                            Interpolator.TANGENT(Duration.seconds(0.5), 390D, Duration.seconds(0.5), 390D)))
            .addKeyFrame(Duration.seconds(2),
                    new KeyValue(sceneRoot.rotateYProperty(), 30D,
                            Interpolator.TANGENT(Duration.seconds(0.5), 30D, Duration.seconds(0.5), 30D)))

            .build();
    private Timeline animation2 = new SimpleTimelineBuilder().cycleCount(Animation.INDEFINITE)
            .addKeyFrame(Duration.ZERO,
                    new KeyValue(sceneRoot.rotateXProperty(), 60D, Interpolator.TANGENT(Duration.seconds(1.0), 60D)))
            .addKeyFrame(Duration.seconds(4),
                    new KeyValue(sceneRoot.rotateXProperty(), 80D, Interpolator.TANGENT(Duration.seconds(1.0), 80D)))
            .addKeyFrame(Duration.seconds(8),
                    new KeyValue(sceneRoot.rotateXProperty(), 60D, Interpolator.TANGENT(Duration.seconds(1.0), 60D)))
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

	public Parent createContent() {
        sceneRoot.setRx(45);
        sceneRoot.setRy(30);
        sceneRoot.setScale(3);
		Group rectangleGroup = new Group();
		// Base1
        Box base1Cube = new Box(BAR_WIDTH * 11.5, BAR_DEPTH * 2, 10);
        base1Cube.setMaterial(new PhongMaterial(Color.DARKSALMON));
        base1Cube.setTranslateX(X_START + 128);
        base1Cube.setTranslateZ(Y_POS + 20.0);
		base1Cube.setTranslateY(11.0);

		// Base2
        Box base2Cube = new Box(BAR_WIDTH * 11.5, BAR_DEPTH * 2, 10);
        base2Cube.setMaterial(new PhongMaterial(Color.DARKSALMON));
        base2Cube.setTranslateX(X_START + 128);
        base2Cube.setTranslateZ(Y_POS - 20.0);
		base2Cube.setTranslateY(11.0);

        rectangleGroup.getChildren().addAll(base1Cube, base2Cube);

        Color[] colors = { Color.PURPLE, Color.BLUEVIOLET, Color.BLUE, Color.GREEN, Color.GREENYELLOW,
                Color.YELLOW, Color.ORANGE, Color.RED };
        AudioClip[] audios = { bar1Note, bar2Note, bar3Note, bar4Note, bar5Note, bar6Note, bar7Note,
                bar8Note };
        addBars(colors, audios, rectangleGroup);
		sceneRoot.getChildren().add(rectangleGroup);
		SubScene subScene = new SubScene(sceneRoot, 1150, 570, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(new PerspectiveCamera());
		sceneRoot.translateXProperty().bind(subScene.widthProperty().divide(2.2));
		sceneRoot.translateYProperty().bind(subScene.heightProperty().divide(1.6));
		return new Group(subScene);
	}

    public void play() {
		animation.play();
		animation2.play();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new Scene(createContent()));
		primaryStage.show();
		play();
	}

	@Override
	public void stop() {
		animation.pause();
		animation2.pause();
	}

	private void addBars(Color[] colors, AudioClip[] audios, Group rectangleGroup) {
        for (int i = 0; i < colors.length; i++) {
            AudioClip audioClip = audios[i];
            Box barCube = new Box(BAR_WIDTH, BAR_DEPTH, 100 - i * 5);
            barCube.setMaterial(new PhongMaterial(colors[i]));
            barCube.setTranslateX(X_START + (i + 1) * X_OFFSET);
            barCube.setTranslateZ(Y_POS);
            barCube.setOnMousePressed(me -> audioClip.play());
            rectangleGroup.getChildren().add(barCube);
        }
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
