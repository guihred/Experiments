package gaming.ex12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class PlatformMain extends Application {
	private static final	double PERIOD = 36.0;
	private long lastNanoTime;

	private Player jungle = new Player(ImageResource.JUNGLE);

    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("The Cat in Motion");
        Leopard cat = new Leopard();
        Bird bird = new Bird();
        Dog dog = new Dog();

        dog.walkLeft();
        bird.flyRight();
        final int height = 512;
        final int width = 1024;
        Canvas canvas = new Canvas(width, height);
        Group group = new Group(canvas);
        List<Player> grounds = new ArrayList<>();
        grounds.add(new Ground(9, 2));
        Tree tree = new Tree();
        grounds.add(tree);
        grounds.add(new Land());
        List<Enemy> enemies = Arrays.asList(bird, dog);
        Scene scene = new Scene(group);
        EventHandler<? super KeyEvent> eventHandler = e -> {
            KeyCode code = e.getCode();
            if (KeyCode.RIGHT == code) {
                cat.walkRight();
            }
            if (KeyCode.LEFT == code) {
                cat.walkLeft();
            }
            if (KeyCode.UP == code) {
                cat.jump();
            }
            if (KeyCode.DOWN == code) {
                cat.stop();
            }
        };
        scene.setOnKeyPressed(eventHandler);
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                cat.stop();
            }
        });
        primaryStage.setWidth(ImageResource.JUNGLE.getScaledWidth());
        primaryStage.setHeight(ImageResource.JUNGLE.getScaledHeight());
        primaryStage.setScene(scene);
        primaryStage.show();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        AnimationTimer a = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                gameLoop(cat, bird, dog, grounds, enemies, gc);
            }
        };
        a.start();

    }
	protected void gameLoop(Leopard cat, Bird bird, Dog dog, List<Player> grounds, List<Enemy> enemies,
			GraphicsContext gc) {
		jungle.render(gc);
        double time = lastNanoTime++ % PERIOD + 1;
        double time2 = time / PERIOD;
		for (Enemy enemy : enemies) {
			if (enemy.isClose(cat)) {
				enemy.attack(cat);
			}
		}
		dog.update(time2);
		bird.update(time2);
		cat.update(time2);

		for (Player ground : grounds) {
			ground.render(gc);
			for (Enemy enemy : enemies) {
				if (ground.intersects((Player) enemy)) {
					((Player) enemy).verticalCollision(ground);
				}
			}

		}
		dog.render(gc);
		bird.render(gc);
		cat.render(gc);

		if (cat.intersects(bird)) {
			bird.fall();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}