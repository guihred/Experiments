package gaming.ex24;

import java.util.function.Supplier;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import simplebuilder.SimpleCircleBuilder;

public enum CheckersPlayer {
    NONE(),
    BLACK(1, () -> newCircle(Color.BLUE)),
    WHITE(-1, () -> newCircle(Color.WHITE));
    private Supplier<Shape> supply;
    private int dir;

    CheckersPlayer() {
    }

    CheckersPlayer(int dir, Supplier<Shape> supply) {
        this.dir = dir;
        this.supply = supply;
    }

    public int getDir() {
        return dir;
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