package simplebuilder;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public class SimpleTimelineBuilder extends SimpleAnimationBuilder<Timeline, SimpleTimelineBuilder> {

    public SimpleTimelineBuilder() {
        super(new Timeline());
    }

    private SimpleTimelineBuilder(Timeline timeline) {
        super(timeline);
    }

    public SimpleTimelineBuilder addKeyFrame(Duration time, EventHandler<ActionEvent> eventHandler) {
        animation.getKeyFrames().add(new KeyFrame(time, eventHandler));
        return this;
    }

    public <T> SimpleTimelineBuilder addKeyFrame(Duration time, WritableValue<T> target, T endValue) {
        animation.getKeyFrames().add(new KeyFrame(time, new KeyValue(target, endValue)));
        return this;
    }

    public <T> SimpleTimelineBuilder addKeyFrame(Duration time, WritableValue<T> target, T endValue,
            Interpolator interpolator) {
        animation.getKeyFrames().add(new KeyFrame(time, new KeyValue(target, endValue, interpolator)));
        return this;
    }

    public SimpleTimelineBuilder addKeyFrame(KeyFrame keyFrame) {
        animation.getKeyFrames().add(keyFrame);
        return this;
    }

    public SimpleTimelineBuilder keyFrames(KeyFrame... elements) {
        animation.getKeyFrames().setAll(elements);
        return this;
    }

    public static SimpleTimelineBuilder of(Timeline time) {

        return new SimpleTimelineBuilder(time);
    }

}