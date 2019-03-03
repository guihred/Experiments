package simplebuilder;

import javafx.animation.FillTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class SimpleFillTransitionBuilder extends SimpleAnimationBuilder<FillTransition, SimpleFillTransitionBuilder> {

    protected FillTransition fillTransition;

	public SimpleFillTransitionBuilder() {
        super(new FillTransition());
        fillTransition = animation;
	}

	public SimpleFillTransitionBuilder duration(Duration value) {
        fillTransition.setDuration(value);
		return this;
	}

    public SimpleFillTransitionBuilder fromValue(Color value) {
        fillTransition.setFromValue(value);
		return this;
	}

    public SimpleFillTransitionBuilder shape(Shape value) {
        fillTransition.setShape(value);
		return this;
	}

    public SimpleFillTransitionBuilder toValue(Color value) {
        fillTransition.setToValue(value);
		return this;
	}


}