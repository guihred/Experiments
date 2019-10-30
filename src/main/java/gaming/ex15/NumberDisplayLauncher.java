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
	public void start(Stage stage) {
		SimpleIntegerProperty number = new SimpleIntegerProperty(0);
		Numbers3D number3d = new Numbers3D(0);
		number3d.numProperty().bind(number);
        final int pad = 60;
        number3d.setTranslateX(pad);
        number3d.setTranslateY(pad);
        stage.setScene(new Scene(new Group(number3d)));
		Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(number, 0)),
                new KeyFrame(Duration.seconds(20), new KeyValue(number, 20)));
		timeline.setCycleCount(-1);
		timeline.play();
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
