/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch02;

import static utils.CommonsFX.onCloseWindow;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleLinearGradientBuilder;
import simplebuilder.SimpleTimelineBuilder;
import utils.CommonsFX;
import utils.Delta;

public class PongLauncher extends Application {

    private static final double WIDTH = 500;
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
    @FXML
    private Circle ball;
    /**
     * The Group containing all of the walls, paddles, and ball. This also allows us
     * to requestFocus for KeyEvents on the Group
     */
    @FXML
    private Group pongComponents;
    /**
     * The left and right paddles
     */
    @FXML
    private Rectangle leftPaddle;
    @FXML
    private Rectangle rightPaddle;
    /**
     * The walls
     */
    @FXML
    private Line topWall;
    @FXML
    private Line rightWall;
    @FXML
    private Line leftWall;
    @FXML
    private Line bottomWall;
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
    private Timeline pongAnimation =
            new SimpleTimelineBuilder().cycleCount(Animation.INDEFINITE).addKeyFrame(Duration.millis(10), t -> {
                checkForCollision();
                int horzPixels = movingRight ? 1 : -1;
                int vertPixels = movingDown ? 1 : -1;
                ball.setCenterX(ball.getCenterX() + horzPixels);
                ball.setCenterY(ball.getCenterY() + vertPixels);
            }).build();
    @FXML
    private Button startButton;

    public double getLeftPaddleDragAnchorY() {
        return leftPaddleDelta.getY();
    }

    public double getRightPaddleDragAnchorY() {
        return leftPaddleDelta.getX();
    }

    public void initialize() {
        reset();
    }

    public void onActionStartButton() {
        startButton.setVisible(false);
        pongAnimation.playFromStart();
        pongComponents.requestFocus();
    }

    public void onKeyPressedGroup0(KeyEvent k) {
        if (k.getCode() == KeyCode.L && !rightPaddle.getBoundsInParent().intersects(topWall.getBoundsInLocal())) {
            rightPaddle.setTranslateY(rightPaddle.getTranslateY() - 6);
        }
        if (k.getCode() == KeyCode.COMMA
                && !rightPaddle.getBoundsInParent().intersects(bottomWall.getBoundsInLocal())) {
            rightPaddle.setTranslateY(rightPaddle.getTranslateY() + 6);
        }
        if (k.getCode() == KeyCode.A && !leftPaddle.getBoundsInParent().intersects(topWall.getBoundsInLocal())) {
            leftPaddle.setTranslateY(leftPaddle.getTranslateY() - 6);
        }
        if (k.getCode() == KeyCode.Z && !leftPaddle.getBoundsInParent().intersects(bottomWall.getBoundsInLocal())) {
            leftPaddle.setTranslateY(leftPaddle.getTranslateY() + 6);
        }

    }

    public void onMouseDraggedLeftPaddle(MouseEvent me) {
        double dragY = me.getSceneY() - getLeftPaddleDragAnchorY();
        leftPaddle.setTranslateY(initLeftPaddleTranslateY + dragY);
    }

    public void onMouseDraggedRightPaddle(MouseEvent me) {
        double dragY = me.getSceneY() - getRightPaddleDragAnchorY();
        rightPaddle.setTranslateY(initRightPaddleTranslateY + dragY);
    }

    public void onMousePressedLeftPaddle(MouseEvent me) {
        initLeftPaddleTranslateY = leftPaddle.getTranslateY();
        setLeftPaddleDragAnchorY(me.getSceneY());
    }

    public void onMousePressedRightPaddle(MouseEvent me) {
        initRightPaddleTranslateY = rightPaddle.getTranslateY();
        setRightPaddleDragAnchorY(me.getSceneY());
    }

    public void setLeftPaddleDragAnchorY(double leftPaddleDragAnchorY) {
        leftPaddleDelta.setY(leftPaddleDragAnchorY);
    }

    public void setRightPaddleDragAnchorY(double rightPaddleDragAnchorY) {
        leftPaddleDelta.setX(rightPaddleDragAnchorY);
    }

    @Override
    public void start(Stage stage) {
        CommonsFX.loadFXML("ZenPong Example", "PongLauncher.fxml", this, stage, WIDTH, WIDTH);
        stage.getScene().setFill(new SimpleLinearGradientBuilder().startX(0.0).startY(0.0).endX(0.0).endY(1.0)
                .stops(new Stop(0.0, Color.BLACK), new Stop(0.0, Color.GRAY)).build());
        onCloseWindow(stage, () -> pongAnimation.stop());
    }

    /**
     * Checks whether or not the ball has collided with either the paddles, topWall,
     * or bottomWall. If the ball hits the wall behind the paddles, the game is
     * over.
     */
    private void checkForCollision() {
        if (ball.intersects(rightWall.getBoundsInLocal()) || ball.intersects(leftWall.getBoundsInLocal())) {
            pongAnimation.stop();
            reset();
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
    private void reset() {
        ball.setCenterX(WIDTH / 2);
        ball.setCenterY(WIDTH / 2);
        leftPaddle.setTranslateY(WIDTH / 2);
        rightPaddle.setTranslateY(WIDTH / 2);
        startButton.setVisible(true);
        pongComponents.requestFocus();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
