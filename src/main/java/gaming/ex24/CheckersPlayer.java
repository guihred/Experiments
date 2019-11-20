package gaming.ex24;

import java.util.function.Supplier;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import simplebuilder.SimpleCircleBuilder;

public enum CheckersPlayer {
    NONE(),
    BLACK(() -> newCircle(Color.RED)),
    WHITE(() -> newCircle(Color.WHITE));
    private Supplier<Shape> supply;

    CheckersPlayer() {
    }

    CheckersPlayer(Supplier<Shape> supply) {
        this.supply = supply;
    }

    public Shape getShape() {
        return supply.get();
    }

    public CheckersPlayer opposite() {
        if (this == CheckersPlayer.BLACK) {
            return WHITE;
        }
        if (this == CheckersPlayer.WHITE) {
            return BLACK;
        }
        return CheckersPlayer.NONE;
    }

    private static Circle newCircle(Color fill) {
        InnerShadow reflection = new InnerShadow();
        reflection.setColor(fill);
        reflection.setRadius(10);
        return new SimpleCircleBuilder().radius(20).stroke(Color.BLACK).fill(fill).effect(reflection).build();
    }

}