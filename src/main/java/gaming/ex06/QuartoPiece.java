/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaming.ex06;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;

public class QuartoPiece extends Group {

	private final int number;
	private final BooleanProperty selected = new SimpleBooleanProperty(false);
	private final PhongMaterial material;

    public QuartoPiece(int number) {
        this.number = number;

        material = new PhongMaterial();
        material.setSpecularColor(Color.WHITE);
        int height = 20;
        Shape3D shape = (1 & number) == 0 ? new Box(20, 20, 20) : new Cylinder(10, 20);

        shape.setMaterial(material);
        final Color color = (2 & number) == 0 ? Color.WHITE : Color.DARKVIOLET;
        material.diffuseColorProperty().bind(Bindings.when(selected).then(Color.BLUE).otherwise(Bindings.when(shape.hoverProperty()).then(Color.RED).otherwise(color)));

        shape.setTranslateY(10);
        if ((4 & number) == 4) {
            shape.setScaleY(2);
            shape.setTranslateY(shape.getTranslateY() + 10);
            height = 40;
        }

        if ((8 & number) == 8) {
            final Cylinder circle = new Cylinder(5, height);
            final PhongMaterial blue = new PhongMaterial();
            blue.setDiffuseColor(Color.BLUE);
            blue.setSpecularColor(Color.BLUE);
            circle.setMaterial(blue);
            getChildren().add(circle);
            circle.translateXProperty().bind(shape.translateXProperty());
            circle.translateYProperty().bind(shape.translateYProperty().add(1));
            circle.translateZProperty().bind(shape.translateZProperty());
        }
        getChildren().add(shape);
    }

	public int getNumber() {
		return number;
	}

	public Boolean isSelected() {
		return selected.get();
	}
	public void setSelected(boolean value) {
		selected.set(value);
	}

}
