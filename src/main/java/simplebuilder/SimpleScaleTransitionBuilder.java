package simplebuilder;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class SimpleScaleTransitionBuilder
        extends SimpleAnimationBuilder<ScaleTransition, SimpleScaleTransitionBuilder> {

    public SimpleScaleTransitionBuilder() {
        super(new ScaleTransition());
    }

    public SimpleScaleTransitionBuilder byX(double value) {
        animation.setByX(value);
        return this;
    }

    public SimpleScaleTransitionBuilder byY(double value) {
        animation.setByY(value);
        return this;
    }

    public SimpleScaleTransitionBuilder duration(Duration duration) {
        animation.setDuration(duration);
        return this;
    }

    public SimpleScaleTransitionBuilder interpolator(Interpolator duration) {
        animation.setInterpolator(duration);
        return this;
    }

    public SimpleScaleTransitionBuilder node(Node highlight) {
        animation.setNode(highlight);
        return this;
    }

}