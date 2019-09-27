/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch02;


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
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.*;
import utils.Delta;

public class PongLauncher extends Application {

    private static final double WIDTH = 500;
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
     * The Group containing all of the walls, paddles, and ball. This also allows us
     * to requestFocus for KeyEvents on the Group
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
    private Line topWall = new SimpleLineBuilder().startX(0).startY(0).endX(WIDTH).endY(0).build();
    private Line rightWall = new SimpleLineBuilder().startX(WIDTH).startY(0).endX(WIDTH).endY(WIDTH).build();
    private Line leftWall = new SimpleLineBuilder().startX(0).startY(0).endX(0).endY(WIDTH).build();
    private Line bottomWall = new SimpleLineBuilder().startX(0).startY(WIDTH).endX(WIDTH).endY(WIDTH).build();
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
    private Timeline pongAnimation = new SimpleTimelineBuilder().cycleCount(Animation.INDEFINITE)
        .addKeyFrame(Duration.millis(10), t -> {
            checkForCollision();
            int horzPixels = movingRight ? 1 : -1;
            int vertPixels = movingDown ? 1 : -1;
            centerX.setValue(centerX.getValue() + horzPixels);
            centerY.setValue(centerY.getValue() + vertPixels);
        }).build();
    private Button startButton = SimpleButtonBuilder.newButton(WIDTH / 2, WIDTH * 2 / 3, "Start!", e -> {
        startVisible.set(false);
        pongAnimation.playFromStart();
        pongComponents.requestFocus();
    });

    public double getLeftPaddleDragAnchorY() {
        return leftPaddleDelta.getY();
    }

    public double getRightPaddleDragAnchorY() {
        return leftPaddleDelta.getX();
    }

    public void setLeftPaddleDragAnchorY(double leftPaddleDragAnchorY) {
        leftPaddleDelta.setY(leftPaddleDragAnchorY);
    }

    public void setRightPaddleDragAnchorY(double rightPaddleDragAnchorY) {
        leftPaddleDelta.setX(rightPaddleDragAnchorY);
    }

    @Override
    public void start(Stage stage) {
        rightPaddle = new SimpleRectangleBuilder().x(WIDTH - 30).width(10).height(30).fill(Color.LIGHTBLUE)
            .cursor(Cursor.HAND).onMousePressed(me -> {
                initRightPaddleTranslateY = rightPaddle.getTranslateY();
                setRightPaddleDragAnchorY(me.getSceneY());
            }).onMouseDragged(me -> {
                double dragY = me.getSceneY() - getRightPaddleDragAnchorY();
                rightPaddleY.setValue(initRightPaddleTranslateY + dragY);
            }).build();
        leftPaddle = new SimpleRectangleBuilder().x(20).width(10).height(30).fill(Color.LIGHTBLUE).cursor(Cursor.HAND)
            .onMousePressed(me -> {
                initLeftPaddleTranslateY = leftPaddle.getTranslateY();
                setLeftPaddleDragAnchorY(me.getSceneY());
            }).onMouseDragged(me -> {
                double dragY = me.getSceneY() - getLeftPaddleDragAnchorY();
                leftPaddleY.setValue(initLeftPaddleTranslateY + dragY);
            }).build();

        pongComponents = new Group(topWall, leftWall, rightWall, bottomWall, leftPaddle, rightPaddle, startButton,
            ball);
        pongComponents.setFocusTraversable(true);
        pongComponents.setOnKeyPressed(k -> {
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

        Scene scene = new Scene(pongComponents, WIDTH, WIDTH);
        scene.setFill(new SimpleLinearGradientBuilder().startX(0.0).startY(0.0).endX(0.0).endY(1.0)
            .stops(new Stop(0.0, Color.BLACK), new Stop(0.0, Color.GRAY)).build());
        ball.centerXProperty().bind(centerX);
        ball.centerYProperty().bind(centerY);
        leftPaddle.translateYProperty().bind(leftPaddleY);
        rightPaddle.translateYProperty().bind(rightPaddleY);
        startButton.visibleProperty().bind(startVisible);
        stage.setScene(scene);
        initialize();
        stage.setWidth(WIDTH + 8);
        stage.setHeight(WIDTH + 30);
        stage.setTitle("ZenPong Example");
		stage.setOnCloseRequest(e -> pongAnimation.stop());
        stage.show();
    }

    /**
     * Checks whether or not the ball has collided with either the paddles, topWall,
     * or bottomWall. If the ball hits the wall behind the paddles, the game is
     * over.
     */
    private void checkForCollision() {
        if (ball.intersects(rightWall.getBoundsInLocal()) || ball.intersects(leftWall.getBoundsInLocal())) {
            pongAnimation.stop();
            initialize();
        } else if (ball.intersects(bottomWall.getBoundsInLocal()) || ball.intersects(topWall.getBoundsInLocal())) {
            movingDown = !movingDown;
        } else if (ball.intersects(leftPaddle.getBoundsInParent()) && !movingRight
            || ball.intersects(rightPaddle.getBoundsInParent()) && movingRight) {
            movingRight = !movingRight;
        }
    }

    /**
     * Sets the initial starting positions of the ball and paddles
     */
    private void initialize() {
        centerX.setValue(WIDTH / 2);
        centerY.setValue(WIDTH / 2);
        leftPaddleY.setValue(WIDTH / 2);
        rightPaddleY.setValue(WIDTH / 2);
        startVisible.set(true);
        pongComponents.requestFocus();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
