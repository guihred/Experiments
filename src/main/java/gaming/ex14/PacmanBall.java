package gaming.ex14;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

public class PacmanBall extends Circle {

	private final BooleanProperty special = new SimpleBooleanProperty(false);
	private Timeline timeline = new SimpleTimelineBuilder()
			.keyFrames(new KeyFrame(Duration.ZERO, new KeyValue(radiusProperty(), 10)),
					new KeyFrame(Duration.seconds(0.1), new KeyValue(radiusProperty(), 15)))
			.autoReverse(true).cycleCount(Animation.INDEFINITE).build();

	public PacmanBall(Double[] d) {
		super(d[0], d[1], 5, Color.WHITE);
		special.addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				timeline.play();
			} else {
				timeline.stop();
			}
		});
	}

	public final BooleanProperty specialProperty() {
		return special;
	}

	public final boolean isSpecial() {
		return special.get();
	}

	public final void setSpecial(final boolean special) {
		this.special.set(special);
	}

}
