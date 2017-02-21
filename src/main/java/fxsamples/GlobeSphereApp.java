package fxsamples;
/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
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
import simplebuilder.ResourceFXUtils;

/**
 * A sample that demonstrates features of PhongMaterial applied to a 3D Sphere.
 * Provided is a playground to exercise the following properties of material:
 * diffuse map and color, specular map, color and power, bump map, self
 * illumination map.
 * 
 * @sampleName 3D Sphere
 * @preview preview.png
 * @see javafx.scene.paint.PhongMaterial
 * @see javafx.scene.shape.Sphere
 * @see javafx.animation.Animation
 * @see javafx.animation.Interpolator
 * @see javafx.animation.RotateTransition
 * @see javafx.beans.binding.Bindings
 * @see javafx.beans.property.BooleanProperty
 * @see javafx.beans.property.DoubleProperty
 * @see javafx.beans.property.SimpleBooleanProperty
 * @see javafx.beans.property.SimpleDoubleProperty
 * @see javafx.scene.AmbientLight
 * @see javafx.scene.Group
 * @see javafx.scene.PerspectiveCamera
 * @see javafx.scene.PointLight
 * @see javafx.scene.SceneAntialiasing
 * @see javafx.scene.SubScene
 * @see javafx.scene.image.Image
 * @see javafx.scene.paint.Color
 * @see javafx.scene.transform.Rotate
 * @see javafx.scene.transform.Translate
 * @see javafx.util.Duration
 * @playground - (name="Material")
 * @playground material.diffuseColor
 * @playground diffuseMap
 * @playground specularColorNull
 * @playground specularColor
 * @playground specularColorOpacity (max=1)
 * @playground specularMap
 * @playground material.specularPower (min=0, max=64)
 * @playground bumpMap
 * @playground selfIlluminationMap
 * @playground - (name="Light")
 * @playground sun.color
 * @playground sunLight
 * @playground sunDistance (min=5, max=150)
 * @playground - (name="Sphere")
 * @playground earth.drawMode
 * @playground earth.cullFace
 * @conditionalFeatures SCENE3D
 */
public class GlobeSphereApp extends Application {

	private Sphere earth;
	private PhongMaterial material;
	private PointLight sunObj;
	private final DoubleProperty sunDistance = new SimpleDoubleProperty(100);
	private final BooleanProperty sunLight = new SimpleBooleanProperty(true);
	private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
	private final BooleanProperty specularColorNull = new SimpleBooleanProperty(
			true);
	private final ObjectProperty<Color> specularColor = new SimpleObjectProperty<>(
			Color.WHITE);
	private final DoubleProperty specularColorOpacity = new SimpleDoubleProperty(
			1);
	private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
	private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
	private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(
			true);

	public Parent createContent() throws Exception {

		Image dImage = new Image(ResourceFXUtils.toExternalForm("earth-d.jpg"));

		material = new PhongMaterial();
		material.setDiffuseColor(Color.WHITE);
		material.diffuseMapProperty().bind(
				Bindings.when(diffuseMap).then(dImage).otherwise((Image) null));

		material.specularColorProperty().bind(Bindings.createObjectBinding(() -> {
				if (specularColorNull.get()) {
					return null;
				}
				return specularColor.get().deriveColor(0, 1, 1, specularColorOpacity.get());
		}, specularColor, specularColorNull, specularColorOpacity));

		Image sImage = new Image(ResourceFXUtils.toExternalForm("earth-s.jpg"));
		material.specularMapProperty()
				.bind(Bindings.when(specularMap).then(sImage)
						.otherwise((Image) null));
		Image nImage = new Image(ResourceFXUtils.toExternalForm("earth-n.jpg"));
		material.bumpMapProperty().bind(
				Bindings.when(bumpMap).then(nImage).otherwise((Image) null));
		Image siImage = new Image(ResourceFXUtils.toExternalForm("earth-l.jpg"));
		material.selfIlluminationMapProperty().bind(
				Bindings.when(selfIlluminationMap).then(siImage)
						.otherwise((Image) null));

		earth = new Sphere(5);
		earth.setMaterial(material);
		earth.setRotationAxis(Rotate.Y_AXIS);

		material.specularColorProperty().addListener(
				(ChangeListener<Color>) (ov, t, t1) -> System.out
						.println("specularColor = " + t1));
		material.specularPowerProperty().addListener(
				(ChangeListener<Number>) (ov, t, t1) -> System.out
						.println("specularPower = " + t1));

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.getTransforms().addAll(new Rotate(-20, Rotate.Y_AXIS),
				new Rotate(-20, Rotate.X_AXIS), new Translate(0, 0, -20));

		sunObj = new PointLight(Color.rgb(255, 243, 234));
		sunObj.translateXProperty().bind(sunDistance.multiply(-0.82));
		sunObj.translateYProperty().bind(sunDistance.multiply(-0.41));
		sunObj.translateZProperty().bind(sunDistance.multiply(-0.41));
		sunObj.lightOnProperty().bind(sunLight);

		AmbientLight ambient = new AmbientLight(Color.rgb(1, 1, 1));

		// Build the Scene Graph
		Group root = new Group();
		root.getChildren().add(camera);
		root.getChildren().add(earth);
		root.getChildren().add(sunObj);
		root.getChildren().add(ambient);

		RotateTransition rt = new RotateTransition(Duration.seconds(24), earth);
		rt.setByAngle(360);
		rt.setInterpolator(Interpolator.LINEAR);
		rt.setCycleCount(Animation.INDEFINITE);
		rt.play();

		// Use a SubScene
		SubScene subScene = new SubScene(root, 400, 300, true,
				SceneAntialiasing.BALANCED);
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
