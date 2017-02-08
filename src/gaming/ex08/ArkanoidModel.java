package gaming.ex08;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ArkanoidModel {

	private Circle circle = new Circle(190, 250, 5, Color.RED);

	public static ArkanoidModel create(Group group, Scene scene) {
		return new ArkanoidModel(group, scene);
	}
    public ArkanoidModel(Group group, Scene scene) {
        final Rectangle rectangle = new Rectangle(200, 500, 50, 10);
        group.getChildren().add(rectangle);
        for (int i = 0; i < 105; i++) {
            Rectangle rect = new Rectangle(i % 15 * 25 + 10, i / 15 * 15 + 50, 20, 10);
            rect.setFill(Color.AQUA);
            group.getChildren().add(rect);
        }
        group.getChildren().add(circle);
        EventHandler<ActionEvent> eventHandler = new EventHandler<ActionEvent>() {
			private int x = 1, y = 1;
            @Override
            public void handle(ActionEvent event) {

                circle.setCenterX(circle.getCenterX() + x);
                Node orElse = group.getChildren().stream().filter(c -> c != circle && circle.intersects(c.getBoundsInLocal())).findAny().orElse(null);
                if (orElse != null
                        || circle.getCenterX() <= 5 || circle.getCenterX() > 390) {

                    x = -x;
                }
				if (orElse instanceof Rectangle && ((Rectangle) orElse).getFill().equals(Color.AQUA)) {
					group.getChildren().remove(orElse);
				}

                circle.setCenterY(circle.getCenterY() + y);
                Node orElse2 = group.getChildren().stream().filter(c -> c != circle && circle.intersects(c.getBoundsInLocal())).findAny().orElse(null);
				if (orElse2 != null
                        || circle.getCenterY() <= 5 || circle.getCenterY() > 570) {
                    y = -y;
                }
				if (orElse2 instanceof Rectangle && ((Rectangle) orElse2).getFill().equals(Color.AQUA)) {
					group.getChildren().remove(orElse2);
				}
                circle.setCenterX(circle.getCenterX() + x);
                circle.setCenterY(circle.getCenterY() + y);
            }
        };
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), eventHandler));
        scene.setOnKeyPressed((KeyEvent event) -> {
            final KeyCode code = event.getCode();
            switch (code) {
                case RIGHT:
                    if (rectangle.getX() < 350) {
                        rectangle.setX(rectangle.getX() + 10);
                    }
                    break;
                case LEFT:
                    if (rectangle.getX() > 0) {
                        rectangle.setX(rectangle.getX() - 10);
                    }
				break;
                default:
				break;
            }
        });
        scene.setOnMouseMoved((MouseEvent event) -> {
            double x = event.getX();
            if (x > 0 && x < 350) {
                rectangle.setX(x);
            }
        });


		timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

}
