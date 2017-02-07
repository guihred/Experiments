package simplebuilder;

import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;

@SuppressWarnings("unchecked")
public class SimpleNodeBuilder<T extends Node, Z extends SimpleBuilder<T>> implements SimpleBuilder<T> {
	protected T node;

	protected SimpleNodeBuilder(T shape) {
		this.node = shape;
	}

	@Override
	public T build() {
		return node;
	}

	public Z layoutX(double value) {
		node.setLayoutX(value);
		return (Z) this;
	}

	public Z opacity(double i) {
		node.setOpacity(i);
		return (Z) this;
	}

	public Z style(String i) {
		node.setStyle(i);
		return (Z) this;
	}
	public Z layoutY(double value) {
		node.setLayoutY(value);
		return (Z) this;
	}

	public Z cursor(Cursor hand) {
		node.setCursor(hand);
		return (Z) this;
	}

	public Z styleClass(String string) {
		node.setStyle(string);
		return (Z) this;
	}

	public Z id(String id) {
		node.setId(id);
		return (Z) this;
	}

	public Z rotationAxis(Point3D value) {
		node.setRotationAxis(value);
		return (Z) this;
	}

	public Z rotate(double value) {
		node.setRotate(value);
		return (Z) this;
	}

	public Z translateX(double value) {
		node.setTranslateX(value);
		return (Z) this;
	}

	public Z translateY(double value) {
		node.setTranslateY(value);
		return (Z) this;
	}

	public Z translateZ(double value) {
		node.setTranslateZ(value);
		return (Z) this;
	}

	public Z effect(Effect value) {
		node.setEffect(value);
		return (Z) this;
	}



	public Z onMouseDragged(EventHandler<? super MouseEvent> value) {
		node.setOnMouseDragged(value);
		return (Z) this;
	}

	public Z onMousePressed(EventHandler<? super MouseEvent> object) {
		node.setOnMousePressed(object);
		return (Z) this;
	}
}