package gaming.ex21;

import java.security.SecureRandom;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Dice extends Group {
    private Rectangle rectangle1 = createSquare(0);
    private IntegerProperty number = new SimpleIntegerProperty();
    private SecureRandom random = new SecureRandom();

    public Dice() {
        circle(0, 0, (a, b, c) -> a ^ b || a && b && !c);// A
        circle(1, 0, (a, b, c) -> a && b && !c);// B
        circle(2, 0, (a, b, c) -> a && !b || a && b && !c);// C
        circle(1, 1, (a, b, c) -> c && !(a && b));// D
        circle(0, 2, (a, b, c) -> a && !b || a && b && !c);// E
        circle(1, 2, (a, b, c) -> a && b && !c);// F
        circle(2, 2, (a, b, c) -> a ^ b || a && b && !c); // G
    }

    public int throwDice() {
        int value = random.nextInt(6) + 1;
        number.set(value);
        return number.get();
    }

    private Circle circle(final int x, final int y, final TriBooleanFunction func) {
        Circle circle = new Circle(5);
        circle.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            int n = number.get();
            boolean a = (n & 4) == 4;
            boolean b = (n & 2) == 2;
            boolean c = (n & 1) == 1;
            return func.apply(a, b, c);
        }, number));
        double value = rectangle1.getWidth() / 4;
        circle.setCenterX(value + value * x);
        circle.setCenterY(value + value * y);
        getChildren().add(circle);
        return circle;
    }

    private Rectangle createSquare(final int y) {
        Rectangle rectangle = new Rectangle(0, y, 50, 50);
        rectangle.setArcHeight(20);
        rectangle.setArcWidth(20);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);

        getChildren().add(rectangle);
        return rectangle;
    }

    @FunctionalInterface
    interface TriBooleanFunction {
        boolean apply(boolean a, boolean b, boolean c);
    }
}
