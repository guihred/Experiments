package simplebuilder;

import javafx.animation.FillTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class SimpleFillTransitionBuilder extends SimpleAnimationBuilder<FillTransition, SimpleFillTransitionBuilder> {


	public SimpleFillTransitionBuilder() {
        super(new FillTransition());
	}

	public SimpleFillTransitionBuilder duration(Duration value) {
        animation.setDuration(value);
		return this;
	}

    public SimpleFillTransitionBuilder fromValue(Color value) {
        animation.setFromValue(value);
		return this;
	}

    public SimpleFillTransitionBuilder shape(Shape value) {
        animation.setShape(value);
		return this;
	}

    public SimpleFillTransitionBuilder toValue(Color value) {
        animation.setToValue(value);
		return this;
	}


}