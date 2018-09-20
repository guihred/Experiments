package utils;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Axis wall
 */
public class Axis extends Pane {

    private Rectangle wall;

    public Axis(double size) {

        // wall
        // first the wall, then the lines => overlapping of lines over walls
        // works
        wall = new Rectangle(size, size);
        getChildren().add(wall);

        // grid
        double zTranslate = 0;
        double lineWidth = 1.0;
        Color gridColor = Color.WHITE;

        for (int y = 0; y <= size; y += size / 10) {

            Line line = new Line(0, 0, size, 0);
            line.setStroke(gridColor);
            line.setFill(gridColor);
            line.setTranslateY(y);
            line.setTranslateZ(zTranslate);
            line.setStrokeWidth(lineWidth);

            getChildren().addAll(line);

        }

        for (int x = 0; x <= size; x += size / 10) {

            Line line = new Line(0, 0, 0, size);
            line.setStroke(gridColor);
            line.setFill(gridColor);
            line.setTranslateX(x);
            line.setTranslateZ(zTranslate);
            line.setStrokeWidth(lineWidth);

            getChildren().addAll(line);

        }

    }

    public void setFill(Paint paint) {
        wall.setFill(paint);
    }

}