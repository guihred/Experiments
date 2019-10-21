package simplebuilder;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class SimpleSequentialTransitionBuilder
        extends SimpleAnimationBuilder<SequentialTransition, SimpleSequentialTransitionBuilder> {

    protected SequentialTransition sequentialTransition;

	public SimpleSequentialTransitionBuilder() {
        super(new SequentialTransition());
        sequentialTransition = animation;
	}

    public SimpleSequentialTransitionBuilder addFadeTransition(double millis, Node node, double from, double to) {
        FadeTransition e = new FadeTransition(Duration.millis(millis), node);
        e.setFromValue(from);
        e.setToValue(to);
        sequentialTransition.getChildren().add(e);
        return this;
    }

    public SimpleSequentialTransitionBuilder addFadeTransition(double millis, Node node, double from, double to,
            EventHandler<ActionEvent> value) {
        FadeTransition e = new FadeTransition(Duration.millis(millis), node);
        e.setFromValue(from);
        e.setToValue(to);
        e.setOnFinished(value);
        sequentialTransition.getChildren().add(e);
        return this;
    }


    public SimpleSequentialTransitionBuilder addTranslateTransition(Node node, double durationMillis,
            ObservableValue<? extends Number> fromX, ObservableValue<? extends Number> toX) {
        TranslateTransition tickerScroller = new TranslateTransition();
        tickerScroller.setNode(node);
        tickerScroller.setDuration(Duration.millis(durationMillis));
        tickerScroller.fromXProperty().bind(fromX);
        tickerScroller.toXProperty().bind(toX);
        // when ticker has finished, reset and replay ticker animation
        sequentialTransition.getChildren().add(tickerScroller);
        return this;
    }

}