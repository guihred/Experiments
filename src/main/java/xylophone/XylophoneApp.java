package xylophone;

import static utils.ResourceFXUtils.toExternalForm;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
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
import utils.Xform;

public class XylophoneApp extends Application {

    private static final double END_ROTATE = 390D;
    private static final Duration ONE_SECOND = Duration.seconds(1.0);
    private static final Duration HALF_SECOND = Duration.seconds(0.5);
    private static final double X_START = -110.0;
    private static final double X_OFFSET = 30.0;
    private static final double Y_POS = 25.0;
	private static final double BAR_WIDTH = 22.0;
    private static final double BAR_DEPTH = 7.0;
    private Xform sceneRoot = new Xform();
    private Timeline animation = new SimpleTimelineBuilder()
            .addKeyFrame(Duration.ZERO,
                    sceneRoot.rotateYProperty(), END_ROTATE,
                    Interpolator.TANGENT(HALF_SECOND, END_ROTATE, HALF_SECOND, END_ROTATE))
            .addKeyFrame(Duration.seconds(2),
                    sceneRoot.rotateYProperty(), X_OFFSET,
                    Interpolator.TANGENT(HALF_SECOND, X_OFFSET, HALF_SECOND, X_OFFSET))

            .build();
    private Timeline animation2 = new SimpleTimelineBuilder().cycleCount(Animation.INDEFINITE)
            .addKeyFrame(Duration.ZERO,
                    sceneRoot.rotateXProperty(), 60D, Interpolator.TANGENT(ONE_SECOND, 60D))
            .addKeyFrame(Duration.seconds(4),
                    sceneRoot.rotateXProperty(), 90D, Interpolator.TANGENT(ONE_SECOND, 90D))
            .addKeyFrame(Duration.seconds(8),
                    sceneRoot.rotateXProperty(), 60D, Interpolator.TANGENT(ONE_SECOND, 60D))
			.build();
    // I didn't have any xylophone sounds so I added piano sounds :P
    private final AudioClip bar1Note = new AudioClip(toExternalForm("waves/C.wav"));
    private final AudioClip bar2Note = new AudioClip(toExternalForm("waves/D.wav"));
    private final AudioClip bar3Note = new AudioClip(toExternalForm("waves/E.wav"));
    private final AudioClip bar4Note = new AudioClip(toExternalForm("waves/F.wav"));
    private final AudioClip bar5Note = new AudioClip(toExternalForm("waves/G.wav"));
    private final AudioClip bar6Note = new AudioClip(toExternalForm("waves/A.wav"));
    private final AudioClip bar7Note = new AudioClip(toExternalForm("waves/B.wav"));
    private final AudioClip bar8Note = new AudioClip(toExternalForm("waves/mC.wav"));

	public Parent createContent() {
        sceneRoot.setRx(9. * 5);
        sceneRoot.setRy(X_OFFSET);
        sceneRoot.setScale(3);
		Group rectangleGroup = new Group();
		// Base1
        final double proportion = 11.5;
        Box base1Cube = new Box(BAR_WIDTH * proportion, BAR_DEPTH * 2, 10);
        base1Cube.setMaterial(new PhongMaterial(Color.DARKSALMON));
        final int leftPadding = 128;
        base1Cube.setTranslateX(X_START + leftPadding);
        base1Cube.setTranslateZ(Y_POS + 20.0);
        final double topPadding = 11.0;
        base1Cube.setTranslateY(topPadding);

		// Base2
        Box base2Cube = new Box(BAR_WIDTH * proportion, BAR_DEPTH * 2, 10);
        base2Cube.setMaterial(new PhongMaterial(Color.DARKSALMON));
        base2Cube.setTranslateX(X_START + leftPadding);
        base2Cube.setTranslateZ(Y_POS - 20.0);
        base2Cube.setTranslateY(topPadding);

        rectangleGroup.getChildren().addAll(base1Cube, base2Cube);

        Color[] colors = { Color.PURPLE, Color.BLUEVIOLET, Color.BLUE, Color.GREEN, Color.GREENYELLOW,
                Color.YELLOW, Color.ORANGE, Color.RED };
        AudioClip[] audios = { bar1Note, bar2Note, bar3Note, bar4Note, bar5Note, bar6Note, bar7Note,
                bar8Note };
        addBars(colors, audios, rectangleGroup);
		sceneRoot.getChildren().add(rectangleGroup);
        SubScene subScene = new SubScene(sceneRoot, 1000, 500, true, SceneAntialiasing.BALANCED);
		subScene.setCamera(new PerspectiveCamera());
        final double other = 2.2;
        sceneRoot.translateXProperty().bind(subScene.widthProperty().divide(other));
        final double other2 = 1.6;
        sceneRoot.translateYProperty().bind(subScene.heightProperty().divide(other2));
		return new Group(subScene);
	}

    public void play() {
		animation.play();
		animation2.play();
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setScene(new Scene(createContent()));
		primaryStage.show();
		play();
	}

	@Override
	public void stop() {
		animation.pause();
		animation2.pause();
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

	private static void addBars(Color[] colors, AudioClip[] audios, Group rectangleGroup) {
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
}
