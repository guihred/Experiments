package simplebuilder;

import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public class SimplePathTransitionBuilder extends SimpleAnimationBuilder<PathTransition, SimplePathTransitionBuilder> {

    protected PathTransition fadeTransition;

	public SimplePathTransitionBuilder() {
        super(new PathTransition());
		fadeTransition = animation;
	}

    public SimplePathTransitionBuilder duration(Duration duration) {
        fadeTransition.setDuration(duration);
        return this;
    }


	public SimplePathTransitionBuilder interpolator(Interpolator value) {
        fadeTransition.setInterpolator(value);
        return this;
    }


    public SimplePathTransitionBuilder node(Node highlight) {
		fadeTransition.setNode(highlight);
		return this;
	}

    public SimplePathTransitionBuilder orientation(OrientationType value) {
        fadeTransition.setOrientation(value);
        return this;
    }

    public SimplePathTransitionBuilder path(Shape path) {
        fadeTransition.setPath(path);
        return this;
    }

}