package gaming.ex05;

public enum TetrisDirection {
    DOWN, LEFT, RIGHT, UP;
    TetrisDirection next() {
        final TetrisDirection[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}