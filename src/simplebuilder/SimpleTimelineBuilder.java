package simplebuilder;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

public class SimpleTimelineBuilder extends SimpleAnimationBuilder<Timeline, SimpleTimelineBuilder> {

	Timeline timeline;

	public SimpleTimelineBuilder() {
		super(new Timeline());
		timeline = animation;
	}

	public SimpleTimelineBuilder keyFrames(KeyFrame... elements) {
		timeline.getKeyFrames().setAll(elements);
		return this;
	}



}