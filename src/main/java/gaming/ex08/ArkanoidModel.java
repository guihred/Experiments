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
import simplebuilder.SimpleTimelineBuilder;

public class ArkanoidModel {

    private Circle circle = new Circle(5, Color.RED);
	private Group group;
    private int x = 1;
    private int y = 2;
    private final Rectangle rectangle = new Rectangle(50, 10);
    private Scene scene;

	public ArkanoidModel(Group group, Scene scene) {
		this.group = group;
        this.scene = scene;


        rectangle.setX(scene.getWidth() / 2);
        rectangle.setY(scene.getHeight() * 5 / 6);
        circle.setCenterX(rectangle.getX() + rectangle.getWidth() / 2);
        circle.setCenterY(rectangle.getY() - circle.getRadius());

        int cols = 15;
        int spacing = 25;
		group.getChildren().add(rectangle);
        int rows = 6;
        group.getChildren()
                .addAll(range(0, cols * rows)
                        .mapToObj(i -> new Rectangle(i % cols * spacing + 10, i / cols * cols + 50, 20, 10))
						.peek(r -> r.setFill(Color.AQUA)).collect(toList()));
		group.getChildren().add(circle);
		scene.setOnKeyPressed((KeyEvent event) -> {
			final KeyCode code = event.getCode();
            switch (code) {
                case RIGHT:
                    if (rectangle.getX() < scene.getWidth() - rectangle.getWidth()) {
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
            if (event.getX() > 0 && event.getX() < scene.getWidth() - rectangle.getWidth()) {
				rectangle.setX(event.getX());
			}
		});
        Timeline timeline = new SimpleTimelineBuilder()
                .addKeyFrame(new KeyFrame(Duration.millis(10), event -> gameLoop()))
                .cycleCount(Animation.INDEFINITE)
                .build();
		timeline.play();
	}

	private void gameLoop() {
		circle.setCenterX(circle.getCenterX() + x);
        Node touchingNode = group.getChildren().stream()
                .filter(c -> c != circle && circle.intersects(c.getBoundsInLocal()))
				.findAny().orElse(null);
        if (touchingNode != null || circle.getCenterX() <= 5
                || circle.getCenterX() > scene.getWidth() - circle.getRadius() * 2) {

            x = x % 2 == 0 ? -x / 2 : 2 * -x;
            if (circle.getCenterX() <= 5) {
                x = Math.abs(x);
            }
            if (circle.getCenterX() > scene.getWidth() - circle.getRadius() * 2) {
                x = -Math.abs(x);
            }
		}
        removeIfBlock(touchingNode);
		circle.setCenterY(circle.getCenterY() + y);
        Node touchingNode2 = group.getChildren().stream()
                .filter(c -> c != circle && circle.intersects(c.getBoundsInLocal()))
				.findAny().orElse(null);
        if (touchingNode2 != null || circle.getCenterY() <= 5 || circle.getCenterY() > scene.getHeight() - 30) {
            y = -y;
		}
        removeIfBlock(touchingNode2);
		circle.setCenterX(circle.getCenterX() + x);
		circle.setCenterY(circle.getCenterY() + y);
	}

    private void removeIfBlock(Node orElse) {
        if (orElse instanceof Rectangle && ((Rectangle) orElse).getFill() == Color.AQUA) {
			group.getChildren().remove(orElse);
		}
    }

	public static ArkanoidModel create(Group group, Scene scene) {
		return new ArkanoidModel(group, scene);
	}

}
