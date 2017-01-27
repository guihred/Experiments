package others;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class SimpleFadeTransitionBuilder extends SimpleAnimationBuilder<FadeTransition, SimpleFadeTransitionBuilder> {

	FadeTransition circle;

	public SimpleFadeTransitionBuilder() {
		super(new FadeTransition());
		circle = animation;
	}

	public SimpleFadeTransitionBuilder node(Node highlight) {
		circle.setNode(highlight);
		return this;
	}

	public SimpleFadeTransitionBuilder duration(Duration value) {
		circle.setDuration(value);
		return this;
	}

	public SimpleFadeTransitionBuilder fromValue(double value) {
		circle.setFromValue(value);
		return this;
	}

	public SimpleFadeTransitionBuilder toValue(double value) {
		circle.setToValue(value);
		return this;
	}


}