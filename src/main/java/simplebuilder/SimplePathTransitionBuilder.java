package simplebuilder;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class SimplePathTransitionBuilder extends SimpleAnimationBuilder<PathTransition, SimplePathTransitionBuilder> {


	public SimplePathTransitionBuilder() {
        super(new PathTransition());
	}

    public SimplePathTransitionBuilder duration(Duration duration) {
        animation.setDuration(duration);
        return this;
    }


	public SimplePathTransitionBuilder interpolator(Interpolator value) {
        animation.setInterpolator(value);
        return this;
    }


    public SimplePathTransitionBuilder node(Node highlight) {
        animation.setNode(highlight);
		return this;
	}

    public SimplePathTransitionBuilder orientation(OrientationType value) {
        animation.setOrientation(value);
        return this;
    }

    public SimplePathTransitionBuilder path(Shape path) {
        animation.setPath(path);
        return this;
    }

}