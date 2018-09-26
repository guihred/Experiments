package simplebuilder;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class SimpleFadeTransitionBuilder extends SimpleAnimationBuilder<FadeTransition, SimpleFadeTransitionBuilder> {

	protected FadeTransition fadeTransition;

	public SimpleFadeTransitionBuilder() {
		super(new FadeTransition());
		fadeTransition = animation;
	}

	public SimpleFadeTransitionBuilder duration(Duration value) {
		fadeTransition.setDuration(value);
		return this;
	}

	public SimpleFadeTransitionBuilder fromValue(double value) {
		fadeTransition.setFromValue(value);
		return this;
	}

	public SimpleFadeTransitionBuilder node(Node highlight) {
		fadeTransition.setNode(highlight);
		return this;
	}

	public SimpleFadeTransitionBuilder toValue(double value) {
		fadeTransition.setToValue(value);
		return this;
	}


}