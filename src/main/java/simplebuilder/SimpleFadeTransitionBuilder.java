package simplebuilder;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class SimpleFadeTransitionBuilder extends SimpleAnimationBuilder<FadeTransition, SimpleFadeTransitionBuilder> {


	public SimpleFadeTransitionBuilder() {
		super(new FadeTransition());
	}

	public SimpleFadeTransitionBuilder duration(Duration value) {
        animation.setDuration(value);
		return this;
	}

	public SimpleFadeTransitionBuilder fromValue(double value) {
        animation.setFromValue(value);
		return this;
	}

	public SimpleFadeTransitionBuilder node(Node highlight) {
        animation.setNode(highlight);
		return this;
	}

	public SimpleFadeTransitionBuilder toValue(double value) {
        animation.setToValue(value);
		return this;
	}


}