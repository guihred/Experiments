package gaming.ex14;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.util.Duration;

public class Pacman extends Region {


	private Arc arc = new Arc();

	public Pacman() {
		setLayoutX(40);
		setLayoutY(40);

		arc.setFill(Color.YELLOW);
		arc.setRadiusX(20.0f);
		arc.setRadiusY(20.0f);
		arc.setStartAngle(45.0f);
		arc.setLength(270.0f);
		arc.setType(ArcType.ROUND);
		getChildren().add(arc);

		Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(arc.startAngleProperty(), 45.0f)),
				new KeyFrame(Duration.ZERO, new KeyValue(arc.lengthProperty(), 270.0f)),
				new KeyFrame(Duration.seconds(0.25), new KeyValue(arc.startAngleProperty(), 0.0f)),
				new KeyFrame(Duration.seconds(0.25), new KeyValue(arc.lengthProperty(), 360.0f)));
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.setAutoReverse(true);
		timeline.playFromStart();
	}

	public void turn(double angle) {
		arc.setRotate(angle);
	}

}
