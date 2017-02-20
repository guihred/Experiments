package gaming.ex08;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
	private Group group;
	private int x = 1, y = 1;

	public ArkanoidModel(Group group, Scene scene) {
		this.group = group;
		final Rectangle rectangle = new Rectangle(200, 500, 50, 10);
		group.getChildren().add(rectangle);
		group.getChildren()
				.addAll(range(0, 105).mapToObj(i -> new Rectangle(i % 15 * 25 + 10, i / 15 * 15 + 50, 20, 10))
						.peek(r -> r.setFill(Color.AQUA)).collect(toList()));
		group.getChildren().add(circle);
		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), event -> gameLoop()));
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
			if (event.getX() > 0 && event.getX() < 350) {
				rectangle.setX(event.getX());
			}
		});

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	private void gameLoop() {
		circle.setCenterX(circle.getCenterX() + x);
		Node orElse = group.getChildren().stream().filter(c -> c != circle && circle.intersects(c.getBoundsInLocal()))
				.findAny().orElse(null);
		if (orElse != null || circle.getCenterX() <= 5 || circle.getCenterX() > 390) {
			x = -x;
		}
		if (orElse instanceof Rectangle && ((Rectangle) orElse).getFill().equals(Color.AQUA)) {
			group.getChildren().remove(orElse);
		}
		circle.setCenterY(circle.getCenterY() + y);
		Node orElse2 = group.getChildren().stream().filter(c -> c != circle && circle.intersects(c.getBoundsInLocal()))
				.findAny().orElse(null);
		if (orElse2 != null || circle.getCenterY() <= 5 || circle.getCenterY() > 570) {
			y = -y;
		}
		if (orElse2 instanceof Rectangle && ((Rectangle) orElse2).getFill().equals(Color.AQUA)) {
			group.getChildren().remove(orElse2);
		}
		circle.setCenterX(circle.getCenterX() + x);
		circle.setCenterY(circle.getCenterY() + y);
	}

	public static ArkanoidModel create(Group group, Scene scene) {
		return new ArkanoidModel(group, scene);
	}

}