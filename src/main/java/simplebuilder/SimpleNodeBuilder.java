package simplebuilder;

import java.lang.reflect.Method;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import utils.ClassReflectionUtils;
import utils.TermFrequency;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

@SuppressWarnings("unchecked")
public class SimpleNodeBuilder<T extends Node, Z extends SimpleBuilder<T>> implements SimpleBuilder<T> {
    protected T node;
    private ContextMenu contextMenu;

    public SimpleNodeBuilder(final T shape) {
        this.node = shape;
        if (shape.getId() == null) {
            id(TermFrequency.getField());
        }

    }

    public Z addContextMenu(String text, EventHandler<ActionEvent> value) {
        contextMenu = SupplierEx.nonNull(contextMenu, new ContextMenu());
        MenuItem item = new MenuItem(text);
        item.setOnAction(actionEvent -> {
            value.handle(actionEvent);
            contextMenu.hide();
        });
        contextMenu.getItems().add(item);
        if (ClassReflectionUtils.hasField(node.getClass(), "contextMenu")) {
            Method setter = ClassReflectionUtils.getSetter(node.getClass(), "contextMenu");
            ClassReflectionUtils.invoke(node, setter, contextMenu);
            return (Z) this;
        }

        EventHandler<? super MouseEvent> onMousePressed = node.getOnMousePressed();
        node.setOnMousePressed(e -> {
            RunnableEx.runIf(onMousePressed, h -> h.handle(e));
            if (e.isSecondaryButtonDown()) {
                contextMenu.show(node, e.getScreenX(), e.getScreenY());
            }
        });
        return (Z) this;
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

    public final Z id(final String id) {
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

    public Z onKeyReleased(final EventHandler<? super KeyEvent> value) {
        onKeyReleased(node, value);
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

    public Z visible(ObservableValue<? extends Boolean> value) {
        node.visibleProperty().bind(value);
        return (Z) this;
    }

    public static void onKeyReleased(Node node, final EventHandler<? super KeyEvent> value) {
        EventHandler<? super KeyEvent> onKeyReleased = node.getOnKeyReleased();
        node.setOnKeyReleased(e -> {
            RunnableEx.runIf(onKeyReleased, onKey -> onKey.handle(e));
            value.handle(e);
        });
    }

    public static void onKeyReleased(Node node, KeyCode e, RunnableEx value) {
        onKeyReleased(node, e0 -> {
            if (e0.getCode() == e) {
                RunnableEx.run(value);
            }
        });
    }

}