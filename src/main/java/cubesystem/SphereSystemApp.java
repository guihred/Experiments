package cubesystem;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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

    private Random r = new SecureRandom();

    public Parent createContent() {
        CubeXForm sceneRoot = new CubeXForm();
        CubeXForm cube1X = new CubeXForm();
        final int size = 40;
        Xsphere cube1 = new Xsphere(size, Color.YELLOW);
        CubeXForm cube11X = new CubeXForm();
        CubeXForm cube12X = new CubeXForm();
        CubeXForm cube13X = new CubeXForm();
        CubeXForm cube14X = new CubeXForm();
        CubeXForm cube15X = new CubeXForm();
        CubeXForm cube16X = new CubeXForm();
        CubeXForm cube17X = new CubeXForm();
        CubeXForm cube18X = new CubeXForm();
        CubeXForm cube19X = new CubeXForm();
        int i = 3;
        Xsphere cube11 = new Xsphere(++i, Color.RED);
        Xsphere cube12 = new Xsphere(++i, Color.ORANGE);
        Xsphere cube13 = new Xsphere(++i, Color.CORNFLOWERBLUE);
        Xsphere cube14 = new Xsphere(++i, Color.DARKGREEN);
        Xsphere cube15 = new Xsphere(++i, Color.BLUE);
        Xsphere cube16 = new Xsphere(++i, Color.PURPLE);
        Xsphere cube17 = new Xsphere(++i, Color.BLUEVIOLET);
        Xsphere cube18 = new Xsphere(++i, Color.DARKGOLDENROD);
        Xsphere cube19 = new Xsphere(++i, Color.KHAKI);
        sceneRoot.getChildren().add(cube1X);
        cube1X.getChildren().addAll(cube1, cube11X, cube12X, cube13X, cube14X, cube15X, cube16X, cube17X, cube18X,
            cube19X);
        cube11X.getChildren().addAll(cube11);
        cube12X.getChildren().add(cube12);
        cube13X.getChildren().add(cube13);
        cube14X.getChildren().add(cube14);
        cube15X.getChildren().add(cube15);
        cube16X.getChildren().add(cube16);
        cube17X.getChildren().add(cube17);
        cube18X.getChildren().add(cube18);
        cube19X.getChildren().add(cube19);
        final double d = 20.0;
        i = 1;
        cube11.setTranslateX(++i * d);
        cube12.setTranslateX(++i * d);
        cube13.setTranslateX(++i * d);
        cube14.setTranslateX(++i * d);
        cube15.setTranslateX(++i * d);
        cube16.setTranslateX(++i * d);
        cube17.setTranslateX(++i * d);
        cube18.setTranslateX(++i * d);
        cube19.setTranslateX(++i * d);
        cube11X.getRx().setAngle(rndPartialAngle());
        cube12X.getRz().setAngle(rndPartialAngle());
        cube13X.getRz().setAngle(rndPartialAngle());
        cube14X.getRz().setAngle(rndPartialAngle());
        cube15X.getRz().setAngle(rndPartialAngle());
        cube16X.getRz().setAngle(rndPartialAngle());
        cube17X.getRz().setAngle(rndPartialAngle());
        cube18X.getRz().setAngle(rndPartialAngle());
        cube19X.getRz().setAngle(rndPartialAngle());
        // Animate
        animation = new Timeline();

        List<CubeXForm> cubesX = Arrays.asList(cube1X, cube11X, cube12X, cube13X, cube14X, cube15X, cube16X, cube17X,
            cube18X, cube19X);
        List<Xsphere> cubes = Arrays.asList(cube11, cube12, cube13, cube14, cube15, cube16, cube17, cube18, cube19);
        KeyFrame keyFrame = new KeyFrame(Duration.ZERO, valuesAtZero(cubesX, cubes));
        KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(4), valuesAt42(cubesX, cubes));
        animation.getKeyFrames().addAll(keyFrame, keyFrame2);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setAutoReverse(false);
        PerspectiveCamera camera = new PerspectiveCamera(true);
        final int farClip = 1500;
        camera.setFarClip(farClip);
        final int pos = 900;
        camera.getTransforms().add(new Translate(0, 0, -pos));
        final int width = 640;
        final int height = 480;
        SubScene subScene = new SubScene(sceneRoot, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        return new Group(subScene);
    }

    public void play() {
        animation.play();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int width = 640;
        final int height = 480;
        primaryStage.setScene(new Scene(createContent(), width, height, true, SceneAntialiasing.BALANCED));
        primaryStage.show();
        play();
    }

    @Override
    public void stop() {
        animation.pause();
    }

    private double rndAngle() {
        return r.nextInt(8) * 360.0 * (r.nextBoolean() ? 1 : -1);
    }

    private double rndPartialAngle() {
        final int bound = 36;
        return r.nextInt(bound) * 10.0;
    }

    private KeyValue[] valuesAt42(List<CubeXForm> cube1X, List<Xsphere> spheres) {
        Stream<KeyValue> array = spheres.stream().map(c -> new KeyValue(c.getRx().angleProperty(), 20 * 360));
        Stream<KeyValue> collect = cube1X.stream().map(e -> new KeyValue(e.getRy().angleProperty(), rndAngle()));
        return Stream.concat(array, collect).toArray(KeyValue[]::new);
    }

    /**
     * Java main for when running without JavaFX launcher
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static KeyValue[] valuesAtZero(List<CubeXForm> cube19X, List<Xsphere> cube11) {
        return Stream.concat(cube19X.stream().map(e -> new KeyValue(e.getRy().angleProperty(), 0.0)),
            cube11.stream().map(e -> new KeyValue(e.getRx().angleProperty(), 0.0))).toArray(KeyValue[]::new);
    }
}
