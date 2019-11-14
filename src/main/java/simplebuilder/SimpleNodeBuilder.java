package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

@SuppressWarnings("unchecked")
public class SimpleNodeBuilder<T extends Node, Z extends SimpleBuilder<T>> implements SimpleBuilder<T> {
    protected T node;

    public SimpleNodeBuilder(final T shape) {
        this.node = shape;
    }

    @Override
    public T build() {
        return node;
    }

    public Z cache(final boolean value) {
        node.setCache(value);
        return (Z) this;
    }

    public Z cursor(final Cursor hand) {
        node.setCursor(hand);
        return (Z) this;
    }

    public Z effect(final Effect value) {
        node.setEffect(value);
        return (Z) this;
    }

    public Z id(final String id) {
        node.setId(id);
        return (Z) this;
    }

    public Z layoutX(final double value) {
        node.setLayoutX(value);
        return (Z) this;
    }

    public Z layoutY(final double value) {
        node.setLayoutY(value);
        return (Z) this;
    }

    public Z managed(final boolean value) {
        node.setManaged(value);
        return (Z) this;
    }

    public Z onMouseDragged(final EventHandler<? super MouseEvent> value) {
        node.setOnMouseDragged(value);
        return (Z) this;
    }

    public Z onMousePressed(final EventHandler<? super MouseEvent> object) {
        node.setOnMousePressed(object);
        return (Z) this;
    }

    public Z onMouseReleased(final EventHandler<? super MouseEvent> value) {
        node.setOnMouseReleased(value);
        return (Z) this;
    }

    public Z opacity(final double i) {
        node.setOpacity(i);
        return (Z) this;
    }

    public Z prefHeight(final double value) {
        node.prefHeight(value);
        if (node instanceof Region) {
            ((Region) node).setPrefHeight(value);
        }
        return (Z) this;
    }

    public Z prefWidth(final double value) {
        node.prefWidth(value);
        if (node instanceof Region) {
            ((Region) node).setPrefWidth(value);
        }

        return (Z) this;
    }

    public Z rotate(final double value) {
        node.setRotate(value);
        return (Z) this;
    }

    public Z rotate(final ObservableValue<? extends Number> observable) {
        node.rotateProperty().bind(observable);
        return (Z) this;
    }

    public Z rotationAxis(final Point3D value) {
        node.setRotationAxis(value);
        return (Z) this;
    }

    public Z style(final String i) {
        node.setStyle(i);
        return (Z) this;
    }

    public Z styleClass(final String string) {
        node.getStyleClass().add(string);
        return (Z) this;
    }

    public Z translateX(final double value) {
        node.setTranslateX(value);
        return (Z) this;
    }

    public Z translateY(final double value) {
        node.setTranslateY(value);
        return (Z) this;
    }

    public Z translateZ(final double value) {
        node.setTranslateZ(value);
        return (Z) this;
    }

    public Z visible(final boolean value) {
        node.setVisible(value);
        return (Z) this;
    }

}