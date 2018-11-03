package gaming.ex15;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class Number3D extends Group {

	private IntegerProperty num = new SimpleIntegerProperty(0);
	private ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.BLACK);
    private DoubleProperty sizeProperty = new SimpleDoubleProperty(15);
	private PhongMaterial phongMaterial = new PhongMaterial();
//	 _ 	   _  _     _  _ _  _  _
//	| | |  _| _||_||_ |_  ||_||_| 
//	|_| | |_  _|  | _||_| ||_| _|
	public Number3D(int number) {
		phongMaterial.diffuseColorProperty().bind(color);
		num.set(number);
		Box rec1 = horizontal();
		rec1.setTranslateX(0);
		rec1.translateYProperty().bind(sizeProperty.multiply(-1));
		rec1.visibleProperty().bind(visibleIfNot(1, 4));
		Box rec2 = horizontal();
		rec2.setTranslateY(0);
		rec2.setTranslateX(0);
		rec2.visibleProperty().bind(visibleIfNot(0, 1, 7));
		Box rec3 = horizontal();
		rec3.translateYProperty().bind(sizeProperty);
		rec3.visibleProperty().bind(visibleIfNot(1, 4, 7));
		rec3.setTranslateX(0);
		Box rec4 = vertical();
        rec4.translateXProperty().bind(sizeProperty.divide(2));
        rec4.translateYProperty().bind(sizeProperty.divide(-2));
		rec4.visibleProperty().bind(visibleIfNot(1, 2, 3, 7));
		Box rec5 = vertical();
        rec5.translateXProperty().bind(sizeProperty.divide(-2));
        rec5.translateYProperty().bind(sizeProperty.divide(2));
		rec5.visibleProperty().bind(visibleIfNot(1, 3, 4, 5, 7, 9));
		Box rec6 = vertical();
        rec6.translateXProperty().bind(sizeProperty.divide(2));
        rec6.translateYProperty().bind(sizeProperty.divide(-2));
		rec6.visibleProperty().bind(visibleIfNot(5, 6));
		Box rec7 = vertical();
        rec7.translateXProperty().bind(sizeProperty.divide(2));
        rec7.translateYProperty().bind(sizeProperty.divide(2));
		rec7.visibleProperty().bind(visibleIfNot(2));
		getChildren().addAll(rec1, rec2, rec3, rec4, rec5, rec6, rec7);
	}

	public IntegerProperty numProperty() {
		return num;
	}


	public DoubleProperty sizeProperty() {
		return sizeProperty;
	}

	private Box horizontal() {

		Box rec1 = new Box(sizeProperty.doubleValue(), 1, 1);
		rec1.setMaterial(phongMaterial);
		rec1.widthProperty().bind(sizeProperty);
		rec1.heightProperty().bind(sizeProperty.divide(5));
		rec1.depthProperty().bind(sizeProperty.divide(5));
		return rec1;
	}

	private Box vertical() {
		Box rec7 = new Box(1, sizeProperty.doubleValue(), 1);
		rec7.setMaterial(phongMaterial);
		rec7.heightProperty().bind(sizeProperty);
		rec7.widthProperty().bind(sizeProperty.divide(5));
		rec7.depthProperty().bind(sizeProperty.divide(5));
		return rec7;
	}

	private BooleanBinding visibleIfNot(int o, int... a) {
		IntegerBinding mod = Bindings.createIntegerBinding(() -> num.get() % 10, num);

		BooleanBinding notEqualTo = mod.isNotEqualTo(o);
		for (int i = 0; i < a.length; i++) {
			notEqualTo = notEqualTo.and(mod.isNotEqualTo(a[i]));
		}
		return notEqualTo;
	}

}
