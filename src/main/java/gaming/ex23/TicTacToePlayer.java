package gaming.ex23;

import java.util.function.Supplier;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import simplebuilder.SimpleCircleBuilder;
import simplebuilder.SimpleSvgPathBuilder;

public enum TicTacToePlayer {
    NONE() {
        @Override
        public String toString() {
            return "N";
        }
    },
    X(() -> new SimpleSvgPathBuilder().stroke(Color.BLACK).content("M0,0L30,30M30,0L0,30").build()),
    O(() -> new SimpleCircleBuilder().radius(25).stroke(Color.BLACK).fill(Color.TRANSPARENT).build());
    private Supplier<Shape> supply;

    TicTacToePlayer() {
    }

    TicTacToePlayer(Supplier<Shape> supply) {
        this.supply = supply;
    }

    public Shape getShape() {
        return supply.get();
    }
}