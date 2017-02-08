/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
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
package sample.cubesystem;

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
        Xform sceneRoot = new Xform();

        Xform cube1X = new Xform();
        Xsphere cube1 = new Xsphere(40, new Color(1.0, 0.9, 0.0, 1.0));

        Xform cube1_1X = new Xform();
        Xform cube1_2X = new Xform();
        Xform cube1_3X = new Xform();
        Xform cube1_4X = new Xform();
        Xform cube1_5X = new Xform();
        Xform cube1_6X = new Xform();
        Xform cube1_7X = new Xform();
        Xform cube1_8X = new Xform();
        Xform cube1_9X = new Xform();

        Xsphere cube1_1 = new Xsphere(4, Color.RED);
        Xsphere cube1_2 = new Xsphere(5, Color.ORANGE);
        Xsphere cube1_3 = new Xsphere(6, Color.CORNFLOWERBLUE);
        Xsphere cube1_4 = new Xsphere(7, Color.DARKGREEN);
        Xsphere cube1_5 = new Xsphere(8, Color.BLUE);
        Xsphere cube1_6 = new Xsphere(9, Color.PURPLE);
        Xsphere cube1_7 = new Xsphere(10, Color.BLUEVIOLET);
        Xsphere cube1_8 = new Xsphere(11, Color.DARKGOLDENROD);
        Xsphere cube1_9 = new Xsphere(12, Color.KHAKI);

        sceneRoot.getChildren().add(cube1X);

        cube1X.getChildren().add(cube1);
        cube1X.getChildren().add(cube1_1X);
        cube1X.getChildren().add(cube1_2X);
        cube1X.getChildren().add(cube1_3X);
        cube1X.getChildren().add(cube1_4X);
        cube1X.getChildren().add(cube1_5X);
        cube1X.getChildren().add(cube1_6X);
        cube1X.getChildren().add(cube1_7X);
        cube1X.getChildren().add(cube1_8X);
        cube1X.getChildren().add(cube1_9X);

        cube1_1X.getChildren().add(cube1_1);
        cube1_2X.getChildren().add(cube1_2);
        cube1_3X.getChildren().add(cube1_3);
        cube1_4X.getChildren().add(cube1_4);
        cube1_5X.getChildren().add(cube1_5);
        cube1_6X.getChildren().add(cube1_6);
        cube1_7X.getChildren().add(cube1_7);
        cube1_8X.getChildren().add(cube1_8);
        cube1_9X.getChildren().add(cube1_9);

        cube1_1.setTranslateX(40.0);
        cube1_2.setTranslateX(60.0);
        cube1_3.setTranslateX(80.0);
        cube1_4.setTranslateX(100.0);
        cube1_5.setTranslateX(120.0);
        cube1_6.setTranslateX(140.0);
        cube1_7.setTranslateX(160.0);
        cube1_8.setTranslateX(180.0);
        cube1_9.setTranslateX(200.0);

        cube1_1X.getRx().setAngle(30.0);
        cube1_2X.getRz().setAngle(10.0);
        cube1_3X.getRz().setAngle(50.0);
        cube1_4X.getRz().setAngle(170.0);
        cube1_5X.getRz().setAngle(60.0);
        cube1_6X.getRz().setAngle(30.0);
        cube1_7X.getRz().setAngle(120.0);
        cube1_8X.getRz().setAngle(40.0);
        cube1_9X.getRz().setAngle(-60.0);

        double endTime = 4000.0;

        // Animate
        animation = new Timeline();
        animation.getKeyFrames().addAll(new KeyFrame(Duration.ZERO,
                new KeyValue(cube1X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1X.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_1X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_2X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_3X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_4X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_5X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_6X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_7X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_8X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_9X.getRy().angleProperty(), 0.0),
                new KeyValue(cube1_1.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_2.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_3.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_4.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_5.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_6.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_7.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_8.getRx().angleProperty(), 0.0),
                new KeyValue(cube1_9.getRx().angleProperty(), 0.0)),
                new KeyFrame(new Duration(endTime),
                new KeyValue(cube1X.getRy().angleProperty(), 360.0),
                new KeyValue(cube1X.getRx().angleProperty(), 360.0),
                new KeyValue(cube1_1X.getRy().angleProperty(), -2880.0),
                new KeyValue(cube1_2X.getRy().angleProperty(), -1440.0),
                new KeyValue(cube1_3X.getRy().angleProperty(), -1080.0),
                new KeyValue(cube1_4X.getRy().angleProperty(), -720.0),
                new KeyValue(cube1_5X.getRy().angleProperty(), 1440.0),
                new KeyValue(cube1_6X.getRy().angleProperty(), 1080.0),
                new KeyValue(cube1_7X.getRy().angleProperty(), -360.0),
                new KeyValue(cube1_8X.getRy().angleProperty(), -720.0),
                new KeyValue(cube1_9X.getRy().angleProperty(), -1080.0),
                new KeyValue(cube1_1.getRx().angleProperty(), 7200.0),
                new KeyValue(cube1_2.getRx().angleProperty(), -7200.0),
                new KeyValue(cube1_3.getRx().angleProperty(), 7200.0),
                new KeyValue(cube1_4.getRx().angleProperty(), -7200.0),
                new KeyValue(cube1_5.getRx().angleProperty(), 7200.0),
                new KeyValue(cube1_6.getRx().angleProperty(), -7200.0),
                new KeyValue(cube1_7.getRx().angleProperty(), 7200.0),
                new KeyValue(cube1_8.getRx().angleProperty(), -7200.0),
                new KeyValue(cube1_9.getRx().angleProperty(), 7200.0)));
		animation.setCycleCount(Animation.INDEFINITE);
        
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
    public void stop() {
        animation.pause();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(createContent(), 640, 480, true, SceneAntialiasing.BALANCED));
        primaryStage.show();
        play();
    }

    /**
     * Java main for when running without JavaFX launcher
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
