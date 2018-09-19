/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch02;

import static simplebuilder.CommonsFX.newButton;

import fxsamples.Delta;
import javafx.animation.Animation;
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
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleLinearGradientBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleTimelineBuilder;

public class PongLauncher extends Application {

    /**
     * The center points of the moving ball
     */
	private DoubleProperty centerX = new SimpleDoubleProperty();
	private DoubleProperty centerY = new SimpleDoubleProperty();
    /**
     * The Y coordinate of the left paddle
     */
	private DoubleProperty leftPaddleY = new SimpleDoubleProperty();
    /**
     * The Y coordinate of the right paddle
     */
	private DoubleProperty rightPaddleY = new SimpleDoubleProperty();
    /**
     * The drag anchor for left and right paddles
     */
    private Delta leftPaddleDelta = new Delta();
    /**
     * The initial translateY property for the left and right paddles
     */
	private double initLeftPaddleTranslateY;
	private double initRightPaddleTranslateY;
    /**
     * The moving ball
     */
	private Circle ball = new SimpleCircleBuilder().radius(5.0).fill(Color.RED).build();
    /**
     * The Group containing all of the walls, paddles, and ball. This also
     * allows us to requestFocus for KeyEvents on the Group
     */
	private Group pongComponents;
    /**
     * The left and right paddles
     */
	private Rectangle leftPaddle;
	private Rectangle rightPaddle;
    /**
     * The walls
     */
	/**
	 * Controls whether the startButton is visible
	 */
	private BooleanProperty startVisible = new SimpleBooleanProperty(true);
	private Rectangle topWall = new SimpleRectangleBuilder().x(0).y(0).width(500).height(1).build();
	private Rectangle rightWall = new SimpleRectangleBuilder().x(500).y(0).width(1).height(500).build();
	private Rectangle leftWall = new SimpleRectangleBuilder().x(0).y(0).width(1).height(500).build();
	private Rectangle bottomWall = new SimpleRectangleBuilder().x(0).y(500).width(500).height(1).build();
	/**
	 * Controls whether the ball is moving right
	 */
	private boolean movingRight = true;
	/**
	 * Controls whether the ball is moving down
	 */
	private boolean movingDown = true;
	/**
	 * The animation of the ball
	 */
	private Timeline pongAnimation = new SimpleTimelineBuilder()
			.cycleCount(Animation.INDEFINITE)
            .addKeyFrame(
					new Duration(10.0), t -> {
						checkForCollision();
						int horzPixels = movingRight ? 1 : -1;
						int vertPixels = movingDown ? 1 : -1;
						centerX.setValue(centerX.getValue() + horzPixels);
						centerY.setValue(centerY.getValue() + vertPixels);
                    })
			.build();
	private Button startButton = newButton(225, 470, "Start!", e -> {
		startVisible.set(false);
		pongAnimation.playFromStart();
		pongComponents.requestFocus();
	});

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
		} else if (ball.intersects(leftPaddle.getBoundsInParent()) && !movingRight
				|| ball.intersects(rightPaddle.getBoundsInParent()) && movingRight) {
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
                    setRightPaddleDragAnchorY(me.getSceneY());
				}).onMouseDragged((MouseEvent me) -> {
                    double dragY = me.getSceneY() - getRightPaddleDragAnchorY();
					rightPaddleY.setValue(initRightPaddleTranslateY + dragY);
				})
                .build();
		leftPaddle = new SimpleRectangleBuilder()
                .x(20)
                .width(10)
                .height(30)
                .fill(Color.LIGHTBLUE)
                .cursor(Cursor.HAND)
                .onMousePressed((MouseEvent me) -> {
                    initLeftPaddleTranslateY = leftPaddle.getTranslateY();
                    setLeftPaddleDragAnchorY(me.getSceneY());
                })
                .onMouseDragged((MouseEvent me) -> {
                    double dragY = me.getSceneY() - getLeftPaddleDragAnchorY();
                    leftPaddleY.setValue(initLeftPaddleTranslateY + dragY);
                })
                .build();

        pongComponents = new Group(topWall, leftWall, rightWall, bottomWall, leftPaddle, rightPaddle, startButton,
                ball);
		pongComponents.setFocusTraversable(true);
		pongComponents.setOnKeyPressed((KeyEvent k) -> {
			if (k.getCode() == KeyCode.L && !rightPaddle.getBoundsInParent().intersects(topWall.getBoundsInLocal())) {
				rightPaddleY.setValue(rightPaddleY.getValue() - 6);
			}
			if (k.getCode() == KeyCode.COMMA
					&& !rightPaddle.getBoundsInParent().intersects(bottomWall.getBoundsInLocal())) {
				rightPaddleY.setValue(rightPaddleY.getValue() + 6);
			}
			if (k.getCode() == KeyCode.A && !leftPaddle.getBoundsInParent().intersects(topWall.getBoundsInLocal())) {
				leftPaddleY.setValue(leftPaddleY.getValue() - 6);
			}
			if (k.getCode() == KeyCode.Z && !leftPaddle.getBoundsInParent().intersects(bottomWall.getBoundsInLocal())) {
				leftPaddleY.setValue(leftPaddleY.getValue() + 6);
			}
		});
		Scene scene = new Scene(pongComponents, 500, 500);
		scene.setFill(new SimpleLinearGradientBuilder().startX(0.0).startY(0.0).endX(0.0).endY(1.0)
				.stops(new Stop(0.0, Color.BLACK), new Stop(0.0, Color.GRAY)).build());
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

    public double getLeftPaddleDragAnchorY() {
        return leftPaddleDelta.getY();
    }

    public void setLeftPaddleDragAnchorY(double leftPaddleDragAnchorY) {
        leftPaddleDelta.setY(leftPaddleDragAnchorY);
    }

    public double getRightPaddleDragAnchorY() {
        return leftPaddleDelta.getX();
    }

    public void setRightPaddleDragAnchorY(double rightPaddleDragAnchorY) {
        leftPaddleDelta.setX(rightPaddleDragAnchorY);
    }
}
