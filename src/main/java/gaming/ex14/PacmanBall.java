package gaming.ex14;

import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import simplebuilder.SimpleTimelineBuilder;

public class PacmanBall extends Circle {

    public static final int MAZE_SIZE = 5;
    public static final double SQUARE_SIZE = 60;
    private final BooleanProperty special = new SimpleBooleanProperty(false);
    private final Timeline timeline = new SimpleTimelineBuilder().addKeyFrame(Duration.ZERO, radiusProperty(), 10)
        .addKeyFrame(Duration.seconds(1. / 10), radiusProperty(), 15).autoReverse(true).cycleCount(Animation.INDEFINITE)
        .build();

    public PacmanBall(@NamedArg("centerX") double x, @NamedArg("centerY") double y) {
        super(x, y, 5, Color.WHITE);
        special.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timeline.play();
            } else {
                timeline.stop();
            }
        });
    }


    public final boolean isSpecial() {
        return special.get();
    }

    public final void setSpecial(final boolean special) {
        this.special.set(special);
    }


}
