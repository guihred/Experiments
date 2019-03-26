package simplebuilder;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class SimpleScaleTransitionBuilder
    extends SimpleAnimationBuilder<ScaleTransition, SimpleScaleTransitionBuilder> {

    protected ScaleTransition fadeTransition;

	public SimpleScaleTransitionBuilder() {
        super(new ScaleTransition());
		fadeTransition = animation;
	}

    public SimpleScaleTransitionBuilder byX(double value) {
        fadeTransition.setByX(value);
        return this;
    }


	public SimpleScaleTransitionBuilder byY(double value) {
        fadeTransition.setByY(value);
        return this;
    }


    public SimpleScaleTransitionBuilder duration(Duration duration) {
        fadeTransition.setDuration(duration);
        return this;
    }

    public SimpleScaleTransitionBuilder interpolator(Interpolator duration) {
        fadeTransition.setInterpolator(duration);
        return this;
    }

    public SimpleScaleTransitionBuilder node(Node highlight) {
		fadeTransition.setNode(highlight);
		return this;
	}


}