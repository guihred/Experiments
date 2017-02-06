package gaming.ex09;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Maze3DSquare extends BorderPane {
    public static final int SQUARE_SIZE = 40;

    BooleanProperty visited = new SimpleBooleanProperty(false);
    BooleanProperty west = new SimpleBooleanProperty(false);
    BooleanProperty east = new SimpleBooleanProperty(false);
    BooleanProperty north = new SimpleBooleanProperty(false);
    BooleanProperty south = new SimpleBooleanProperty(false);

    public Maze3DSquare() {
        styleProperty().bind(Bindings.when(visited).then("-fx-background-color:green;").otherwise("-fx-background-color:gray;"));
        setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
        final PhongMaterial phongMaterial = new PhongMaterial(Color.ROYALBLUE);

        Box line = new Box(1, SQUARE_SIZE, SQUARE_SIZE);
        line.setMaterial(phongMaterial);
        line.visibleProperty().bind(east.not());
        setRight(line);
        Box line2 = new Box(SQUARE_SIZE, 1, SQUARE_SIZE);
        line2.setMaterial(phongMaterial);
        line2.visibleProperty().bind(north.not());
        setTop(line2);
        Box line3 = new Box(1, SQUARE_SIZE, SQUARE_SIZE);
        line3.visibleProperty().bind(west.not());
        line3.setMaterial(phongMaterial);
        setLeft(line3);
        Box line4 = new Box(SQUARE_SIZE, 1, SQUARE_SIZE);
        line4.visibleProperty().bind(south.not());
        setBottom(line4);
        line4.setMaterial(phongMaterial);
    }

}
