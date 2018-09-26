package simplebuilder;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class SimpleTimelineBuilder extends SimpleAnimationBuilder<Timeline, SimpleTimelineBuilder> {

	protected Timeline timeline;

	public SimpleTimelineBuilder() {
		super(new Timeline());
		timeline = animation;
	}

	public SimpleTimelineBuilder addKeyFrame(Duration time, EventHandler<ActionEvent> eventHandler) {
        timeline.getKeyFrames().add(new KeyFrame(time, eventHandler));
        return this;
    }

    public SimpleTimelineBuilder addKeyFrame(Duration time, KeyValue... values) {
        timeline.getKeyFrames().add(new KeyFrame(time, values));
        return this;
    }

    public <T> SimpleTimelineBuilder addKeyFrame(Duration time, WritableValue<T> target, T endValue) {
        timeline.getKeyFrames().add(new KeyFrame(time, new KeyValue(target, endValue)));
        return this;
    }

    public SimpleTimelineBuilder addKeyFrame(KeyFrame keyFrame) {
        timeline.getKeyFrames().add(keyFrame);
        return this;
    }

    public SimpleTimelineBuilder keyFrames(KeyFrame... elements) {
		timeline.getKeyFrames().setAll(elements);
		return this;
	}


}