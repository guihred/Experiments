package gaming.ex15;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NumberDisplayLauncher extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		SimpleIntegerProperty number = new SimpleIntegerProperty(0);
		Numbers3D number3d = new Numbers3D(0);
		number3d.numProperty().bind(number);
		number3d.setTranslateX(100);
		number3d.setTranslateY(60);
		stage.setScene(new Scene(new Group(number3d), 400, 400));
		Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(number, 0)),
				new KeyFrame(Duration.seconds(5), new KeyValue(number, 20)));
		timeline.setCycleCount(-1);
		timeline.play();
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
