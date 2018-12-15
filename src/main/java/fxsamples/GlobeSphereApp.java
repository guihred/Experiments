package fxsamples;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class GlobeSphereApp extends Application implements HasLogging {

    private static final int ROTATION_PERIOD_SECONDS = 24;
    private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
    private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
    private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularColorNull = new SimpleBooleanProperty(true);
    private final ObjectProperty<Color> specularColor = new SimpleObjectProperty<>(Color.WHITE);
    private final DoubleProperty specularColorOpacity = new SimpleDoubleProperty(1);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);

    public Parent createContent() {

        Image dImage = new Image(ResourceFXUtils.toExternalForm("earth-d.jpg"));

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHITE);
        material.diffuseMapProperty().bind(Bindings.when(diffuseMap).then(dImage).otherwise((Image) null));

        material.specularColorProperty().bind(Bindings.createObjectBinding(() -> {
            if (specularColorNull.get()) {
                return null;
            }
            return specularColor.get().deriveColor(0, 1, 1, specularColorOpacity.get());
        }, specularColor, specularColorNull, specularColorOpacity));

        Image sImage = new Image(ResourceFXUtils.toExternalForm("earth-s.jpg"));
        material.specularMapProperty().bind(Bindings.when(specularMap).then(sImage).otherwise((Image) null));
        Image nImage = new Image(ResourceFXUtils.toExternalForm("earth-n.jpg"));
        material.bumpMapProperty().bind(Bindings.when(bumpMap).then(nImage).otherwise((Image) null));
        Image siImage = new Image(ResourceFXUtils.toExternalForm("earth-l.jpg"));
        material.selfIlluminationMapProperty()
                .bind(Bindings.when(selfIlluminationMap).then(siImage).otherwise((Image) null));

        Sphere earth = new Sphere(5);
        earth.setMaterial(material);
        earth.setRotationAxis(Rotate.Y_AXIS);

        material.specularColorProperty().addListener((ov, t, t1) -> getLogger().info("specularColor = {}", t1));
        material.specularPowerProperty().addListener((ov, t, t1) -> getLogger().info("specularPower = {}", t1));

        // Create and position camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(new Rotate(-20, Rotate.Y_AXIS), new Rotate(-20, Rotate.X_AXIS),
                new Translate(0, 0, -20));

        PointLight sunObj = new PointLight(Color.WHITE);
        final double sunMultiplier = -0.41;
        sunObj.translateXProperty().bind(sunDistance.multiply(sunMultiplier * 2));
        sunObj.translateYProperty().bind(sunDistance.multiply(sunMultiplier));
        sunObj.translateZProperty().bind(sunDistance.multiply(sunMultiplier));
        sunObj.lightOnProperty().bind(sunLight);

        AmbientLight ambient = new AmbientLight(Color.BLACK);

        // Build the Scene Graph
        Group root = new Group();
        root.getChildren().add(camera);
        root.getChildren().add(earth);
        root.getChildren().add(sunObj);
        root.getChildren().add(ambient);

        RotateTransition rt = new RotateTransition(Duration.seconds(ROTATION_PERIOD_SECONDS), earth);
        rt.setByAngle(360);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.play();

        // Use a SubScene
        final int sceneSize = 400;
        SubScene subScene = new SubScene(root, sceneSize, sceneSize, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);

        return new Group(subScene);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(createContent());
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
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
