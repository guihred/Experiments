package gaming.ex24;

import java.util.Arrays;
import javafx.beans.NamedArg;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import simplebuilder.SimpleSvgPathBuilder;

public class CheckersSquare extends StackPane {

    private ObjectProperty<CheckersPlayer> state = new SimpleObjectProperty<>(CheckersPlayer.NONE);
    private boolean black;
    private BooleanProperty highlight = new SimpleBooleanProperty(false);
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private BooleanProperty marked = new SimpleBooleanProperty(false);
    private BooleanProperty queen = new SimpleBooleanProperty(false);

    public CheckersSquare(@NamedArg("black") boolean black) {
        this.black = black;

        setBackground(
            new Background(new BackgroundFill(black ? Color.BLACK : Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        setPrefSize(50, 50);
        for (CheckersPlayer o : Arrays.asList(CheckersPlayer.WHITE, CheckersPlayer.BLACK)) {
            Shape shape = o.getShape();
            shape.visibleProperty().bind(state.isEqualTo(o));
            getChildren().add(shape);
            InnerShadow glow = new InnerShadow(20, Color.YELLOW);
            glow.colorProperty().bind(Bindings.when(marked).then(Color.RED).otherwise(Color.YELLOW));
            shape.effectProperty().bind(Bindings.when(selected.or(marked)).then(glow).otherwise((InnerShadow) null));
        }
        Effect glow = new InnerShadow(20, Color.BLUE);
        effectProperty().bind(Bindings.when(highlight).then(glow).otherwise((Effect) null));
        getChildren().add(new SimpleSvgPathBuilder().content("m12.50 2.50c0 -0.78 -0.65 -1.42 -1.42 -1.42"
            + "c-0.82 0 -1.45 0.65 -1.45 1.42c0 0.81 0.64 1.46 1.45 1.46c0.78 -0 1.42 -0.65 1.42 -1.46"
            + "m9.78 6.35c0 -0.82 -0.64 -1.45 -1.45 -1.45c-0.78 0 -1.42 0.64 -1.42 1.45"
            + "c0 0.78 0.64 1.42 1.42 1.42c0.82 0 1.45 -0.64 1.45 -1.42m-19.52 0"
            + "c0 -0.82 -0.65 -1.45 -1.46 -1.45c-0.78 0 -1.42 0.64 -1.42 1.45c0 0.78 0.64 1.42 1.42 1.42"
            + "c0.81 0 1.46 -0.64 1.46 -1.42m27.08 0.54c0 -0.81 -0.64 -1.45 -1.42 -1.45"
            + "c-0.81 0 -1.45 0.65 -1.45 1.45c0 0.78 0.64 1.43 1.45 1.43c0.78 0 1.42 -0.65 1.42 -1.43"
            + "m-34.67 0c0 -0.81 -0.64 -1.45 -1.42 -1.45c-0.81 0 -1.46 0.65 -1.46 1.45"
            + "c0 0.78 0.65 1.43 1.46 1.43c0.78 0 1.42 -0.65 1.42 -1.43m31.90 3.31"
            + "c0.34 -0.75 -0.35 -1.01 -0.65 -0.54c-1.01 1.72 -2.36 4.32 -3.99 5.94"
            + "c-0.81 0.81 -2.16 0.48 -2.33 -0.78c-0.31 -2.03 0.04 -3.65 0.11 -5.16"
            + "c0.04 -0.65 -0.52 -0.75 -0.75 -0.17c-0.61 1.39 -1.05 3.17 -2.33 4.59"
            + "c-1.12 1.19 -2.60 0.82 -3.37 -0.24c-1.63 -2.23 -2.03 -7.83 -2.30 -10.49"
            + "c-0.07 -0.51 -0.75 -0.51 -0.81 0c-0.27 2.67 -0.68 8.26 -2.33 10.49"
            + "c-0.75 1.05 -2.27 1.42 -3.34 0.24c-1.29 -1.42 -1.73 -3.20 -2.33 -4.59"
            + "c-0.25 -0.58 -0.82 -0.48 -0.78 0.17c0.10 1.52 0.44 3.17 0.14 5.16"
            + "c-0.17 1.26 -1.52 1.59 -2.37 0.78c-1.59 -1.62 -2.97 -4.22 -3.95 -5.94"
            + "c-0.31 -0.51 -0.98 -0.21 -0.64 0.54c2.20 4.76 4.73 11.94 4.96 12.24"
            + "c0.21 0.27 0.34 0.34 0.47 0.27c2.36 -1.35 6.61 -1.89 10.56 -1.89c3.95 0 8.16 0.54 10.56 1.89"
            + "c0.14 0.07 0.24 0 0.47 -0.27c0.24 -0.30 2.77 -7.49 4.96 -12.24m-15.99 12.48"
            + "c-4.18 0 -10.32 0.75 -10.32 2.77c0 2.06 6.14 2.80 10.32 2.80c4.19 0 10.32 -0.75 10.32 -2.80"
            + "c0 -2.03 -6.14 -2.77 -10.32 -2.77m0 4.59c-3.92 0 -8.94 -0.68 -8.94 -1.82"
            + "c0 -1.12 5.03 -1.79 8.94 -1.79c3.88 0 8.94 0.68 8.94 1.79c-0 1.15 -5.06 1.82 -8.94 1.82")
            .fill(Color.GOLD).visible(queen).build());

    }

    public boolean getHighlight() {
        return highlight.get();
    }

    public boolean getMarked() {
        return marked.get();
    }

    public boolean getQueen() {
        return queen.get();
    }

    public boolean getSelected() {
        return selected.get();
    }

    public CheckersPlayer getState() {
        return state.get();
    }

    public boolean isBlack() {
        return black;
    }

    public void setBlack(boolean black) {
        this.black = black;
    }

    public void setHighlight(boolean highlight) {
        this.highlight.set(highlight);
    }

    public void setMarked(boolean selected) {
        marked.set(selected);
    }

    public void setQueen(boolean queen) {
        this.queen.set(queen);
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public void setState(CheckersPlayer state) {
        this.state.set(state);
    }
}