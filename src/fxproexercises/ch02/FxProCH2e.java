/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch02;

import static others.CommonsFX.newButton;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import others.SimpleCircleBuilder;
import others.SimpleRectangleBuilder;

public class FxProCH2e extends Application {

    /**
     * The center points of the moving ball
     */
    DoubleProperty centerX = new SimpleDoubleProperty();
    DoubleProperty centerY = new SimpleDoubleProperty();
    /**
     * The Y coordinate of the left paddle
     */
    DoubleProperty leftPaddleY = new SimpleDoubleProperty();
    /**
     * The Y coordinate of the right paddle
     */
    DoubleProperty rightPaddleY = new SimpleDoubleProperty();
    /**
     * The drag anchor for left and right paddles
     */
    double leftPaddleDragAnchorY;
    double rightPaddleDragAnchorY;
    /**
     * The initial translateY property for the left and right paddles
     */
    double initLeftPaddleTranslateY;
    double initRightPaddleTranslateY;
    /**
     * The moving ball
     */
    Circle ball;
    /**
     * The Group containing all of the walls, paddles, and ball. This also
     * allows us to requestFocus for KeyEvents on the Group
     */
    Group pongComponents;
    /**
     * The left and right paddles
     */
    Rectangle leftPaddle;
    Rectangle rightPaddle;
    /**
     * The walls
     */
    Rectangle topWall;
    Rectangle rightWall;
    Rectangle leftWall;
    Rectangle bottomWall;
    Button startButton;
    /**
     * Controls whether the startButton is visible
     */
    BooleanProperty startVisible = new SimpleBooleanProperty(true);
    /**
     * Controls whether the ball is moving right
     */
    boolean movingRight = true;
    /**
     * Controls whether the ball is moving down
     */
    boolean movingDown = true;
    /**
     * The animation of the ball
     */
	Timeline pongAnimation = new Timeline(
                    new KeyFrame(
                            new Duration(10.0), (javafx.event.ActionEvent t) -> {
                                checkForCollision();
                                int horzPixels = movingRight ? 1 : -1;
                                int vertPixels = movingDown ? 1 : -1;
                                centerX.setValue(centerX.getValue() + horzPixels);
                                centerY.setValue(centerY.getValue() + vertPixels);
                            })
	);
	{
		pongAnimation.setCycleCount(Timeline.INDEFINITE);
	}

    /**
     * Sets the initial starting positions of the ball and paddles
     */
    void initialize() {
        centerX.setValue(250);
        centerY.setValue(250);
        leftPaddleY.setValue(235);
        rightPaddleY.setValue(235);
        startVisible.set(true);
        pongComponents.requestFocus();
    }

    /**
     * Checks whether or not the ball has collided with either the paddles,
     * topWall, or bottomWall. If the ball hits the wall behind the paddles, the
     * game is over.
     */
    void checkForCollision() {
        if (ball.intersects(rightWall.getBoundsInLocal())
                || ball.intersects(leftWall.getBoundsInLocal())) {
            pongAnimation.stop();
            initialize();
        } else if (ball.intersects(bottomWall.getBoundsInLocal())
                || ball.intersects(topWall.getBoundsInLocal())) {
            movingDown = !movingDown;
        } else if (ball.intersects(leftPaddle.getBoundsInParent()) && !movingRight) {
            movingRight = !movingRight;
        } else if (ball.intersects(rightPaddle.getBoundsInParent()) && movingRight) {
            movingRight = !movingRight;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
		rightPaddle = new SimpleRectangleBuilder()
                .x(470)
                .width(10)
                .height(30)
                .fill(Color.LIGHTBLUE)
				.cursor(Cursor.HAND)
				.onMousePressed((MouseEvent me) -> {
					initRightPaddleTranslateY = rightPaddle.getTranslateY();
					rightPaddleDragAnchorY = me.getSceneY();
				}).onMouseDragged((MouseEvent me) -> {
					double dragY = me.getSceneY() - rightPaddleDragAnchorY;
					rightPaddleY.setValue(initRightPaddleTranslateY + dragY);
				})
                .build();
		startButton = newButton(225, 470, "Start!", (e) -> {
                    startVisible.set(false);
                    pongAnimation.playFromStart();
                    pongComponents.requestFocus();
                })
		;
		leftPaddle = new SimpleRectangleBuilder()
                .x(20)
                .width(10)
                .height(30)
                .fill(Color.LIGHTBLUE)
                .cursor(Cursor.HAND)
                .onMousePressed((MouseEvent me) -> {
                    initLeftPaddleTranslateY = leftPaddle.getTranslateY();
                    leftPaddleDragAnchorY = me.getSceneY();
                })
                .onMouseDragged((MouseEvent me) -> {
                    double dragY = me.getSceneY() - leftPaddleDragAnchorY;
                    leftPaddleY.setValue(initLeftPaddleTranslateY + dragY);
                })
                .build();
		bottomWall = new SimpleRectangleBuilder()
                .x(0)
                .y(500)
                .width(500)
                .height(1)
                .build();
		rightWall = new SimpleRectangleBuilder()
                .x(500)
                .y(0)
                .width(1)
                .height(500)
                .build();
		leftWall = new SimpleRectangleBuilder()
                .x(0)
                .y(0)
                .width(1)
                .height(500)
                .build();
		topWall = new SimpleRectangleBuilder()
                .x(0)
                .y(0)
                .width(500)
                .height(1)
                .build();
		ball = new SimpleCircleBuilder()
                .radius(5.0)
				.fill(Color.RED)
                .build();
		Group group = new Group(topWall, leftWall, rightWall, bottomWall, leftPaddle, rightPaddle, startButton, ball);
		group.setFocusTraversable(true);
		group.setOnKeyPressed((KeyEvent k) -> {
					if (k.getCode() == KeyCode.SPACE
							&& pongAnimation.statusProperty().get() == Animation.Status.STOPPED) {
						rightPaddleY.setValue(rightPaddleY.getValue() - 6);
					} else if (k.getCode() == KeyCode.L
							&& !rightPaddle.getBoundsInParent().intersects(topWall.getBoundsInLocal())) {
						rightPaddleY.setValue(rightPaddleY.getValue() - 6);
					} else if (k.getCode() == KeyCode.COMMA
							&& !rightPaddle.getBoundsInParent().intersects(bottomWall.getBoundsInLocal())) {
						rightPaddleY.setValue(rightPaddleY.getValue() + 6);
					} else if (k.getCode() == KeyCode.A
							&& !leftPaddle.getBoundsInParent().intersects(topWall.getBoundsInLocal())) {
						leftPaddleY.setValue(leftPaddleY.getValue() - 6);
					} else if (k.getCode() == KeyCode.Z
							&& !leftPaddle.getBoundsInParent().intersects(bottomWall.getBoundsInLocal())) {
						leftPaddleY.setValue(leftPaddleY.getValue() + 6);
					}
		});
		Scene scene = new Scene(pongComponents = group, 500, 500);
		// scene.setFill(LinearGradientBuilder.create()
		// .startX(0.0)
		// .startY(0.0)
		// .endX(0.0)
		// .endY(1.0)
		// .stops(
		// new Stop(0.0, Color.BLACK),
		// new Stop(0.0, Color.GRAY)
		// )
		// .build());
        ball.centerXProperty().bind(centerX);
        ball.centerYProperty().bind(centerY);
        leftPaddle.translateYProperty().bind(leftPaddleY);
        rightPaddle.translateYProperty().bind(rightPaddleY);
        startButton.visibleProperty().bind(startVisible);
        stage.setScene(scene);
        initialize();
        stage.setTitle("ZenPong Example");
        stage.show();
    }
}
