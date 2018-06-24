/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex10;

import java.io.Serializable;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public enum MinesweeperImage {

    BLANK(j -> new Rectangle(5, 5, Color.WHITE)),
    BOMB(j -> {
        int pontas = 8;
        double[] points = DoubleStream.iterate(0, i -> i + 1).limit(pontas * 2)
                .flatMap(i -> DoubleStream.of(5 * (i % 2 + 1) * Math.cos(Math.toRadians(i * 360 / pontas / 2)),
                        5 * (i % 2 + 1) * Math.sin(Math.toRadians(i * 360 / pontas / 2))))
                .toArray();
        return new Polygon(points);
    }),
    NUMBER(i -> {
        Text t = new Text(Integer.toString(i));
        t.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, 11));
        return t;
    }),
    FLAG(j -> {
        long pontas = 3;
        double[] points = DoubleStream.iterate(0, i -> i + 1).limit(pontas * 2)
                .flatMap(i -> DoubleStream.of(2 * (i % 2 + 1) * Math.cos(Math.toRadians(i * 360 / pontas / 2)),
                        2 * (i % 2 + 1) * Math.sin(Math.toRadians(i * 360 / pontas / 2)) + 1))
                .toArray();
        Polygon polygon = new Polygon(points);
        polygon.setFill(Color.RED);
        return polygon;
    })

    ;
	private F<Integer, Shape> shape;

	private MinesweeperImage(F<Integer, Shape> shape) {
        this.shape = shape;

    }

    public Shape getShape(Integer i) {
        return shape.apply(i);
    }

	public static interface F<T, R> extends Function<T, R>, Serializable {
		// DOES NOTHING
	}

}

