package fxsamples;
import javafx.animation.Animation;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.ResourceFXUtils;

public class SandboxFX extends Application {

	private static final Image IMAGE = new Image(ResourceFXUtils.toExternalForm("The_Horse_in_Motion.jpg"), true);

	private static final int COLUMNS = 4;
	private static final int COUNT = 10;
	private static final int OFFSET_X = 18;
	private static final int OFFSET_Y = 25;
	private static final int WIDTH = 374;
	private static final int HEIGHT = 243;

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("The Horse in Motion");

		final ImageView imageView = new ImageView(IMAGE);
		imageView
				.setViewport(new Rectangle2D(OFFSET_X, OFFSET_Y, WIDTH, HEIGHT));

		final Animation animation = new SpriteAnimation(imageView, Duration.millis(1000)).setCount(COUNT)
				.setColumns(COLUMNS).setOffsetX(OFFSET_X).setOffsetY(OFFSET_Y)
						.setWidth(WIDTH).setHeight(HEIGHT);

		animation.setCycleCount(Animation.INDEFINITE);
		animation.play();

		primaryStage.setScene(new Scene(new Group(imageView)));
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}