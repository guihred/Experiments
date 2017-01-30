package fxproexercises.starterApp;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;

class MazeSquare extends BorderPane {
	public static final int SQUARE_SIZE = 20;

	BooleanProperty visited = new SimpleBooleanProperty(false);
	BooleanProperty west = new SimpleBooleanProperty(false);
	BooleanProperty east = new SimpleBooleanProperty(false);
	BooleanProperty north = new SimpleBooleanProperty(false);
	BooleanProperty south = new SimpleBooleanProperty(false);

	public MazeSquare() {
		setStyle("-fx-background-color:green;");
		setPrefSize(SQUARE_SIZE, SQUARE_SIZE);
		final Line line = new Line(0, 0, 0, SQUARE_SIZE);
		line.visibleProperty().bind(east.not());
		setLeft(line);
		final Line line2 = new Line(0, 0, SQUARE_SIZE, 0);
		line2.visibleProperty().bind(north.not());
		setTop(line2);
		final Line line3 = new Line(0, 0, 0, SQUARE_SIZE);
		line3.visibleProperty().bind(west.not());
		setRight(line3);
		final Line line4 = new Line(0, 0, SQUARE_SIZE, 0);
		line4.visibleProperty().bind(south.not());
		setBottom(line4);
    }

}