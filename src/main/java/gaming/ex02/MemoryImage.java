/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex02;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.stream.DoubleStream.iterate;
import static java.util.stream.DoubleStream.of;

import java.util.function.Supplier;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;


public enum MemoryImage {
    CIRCULO(() -> new Circle(10)),
    QUADRADO(() -> new Rectangle(20, 20)),
    RETANGULO(() -> new Rectangle(20, 10)),
    ESTRELA(() -> {
        int pontas = 5;
        double[] points = iterate(0, i -> i + 1).limit(pontas).flatMap(i
                -> of(10 * cos(i * 2 % 5 * 2 * PI / pontas),
                        10 * sin(i * 2 % 5 * 2 * PI / pontas))).toArray();
        return new Polygon(points);
    }),
    TRIANGULO(() -> new Polygon(0, 0, 20, 0, 10, -Math.sqrt(300)));
	private transient Supplier<Shape> shape;

    private MemoryImage(Supplier<Shape> shape) {
        this.shape = shape;
    }

    public Shape getShape() {
        return shape.get();
    }


}
