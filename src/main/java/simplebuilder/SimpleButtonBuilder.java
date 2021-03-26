package simplebuilder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import utils.ex.RunnableEx;

public class SimpleButtonBuilder extends SimpleNodeBuilder<Button, SimpleButtonBuilder> {

    public SimpleButtonBuilder() {
        super(new Button());
    }

    public SimpleButtonBuilder onAction(EventHandler<ActionEvent> value) {
        node.setOnAction(value);
        return this;
    }

    private SimpleButtonBuilder text(String string) {
        node.setText(string);
        return this;
    }

    public static Button newButton(final Node graphic, final String id, final EventHandler<ActionEvent> onAction) {
        Button node = new Button(null, graphic);
        node.setId(id);
        node.setOnAction(onAction);
        return node;
    }

    public static Button newButton(final String nome, final EventHandler<ActionEvent> onAction) {
        Button node = new Button(nome);
        node.setId(nome);
        node.setOnAction(onAction);
        return node;
    }

    public static Button newButton(final String nome, final RunnableEx onAction) {
        Button node = new Button(nome);
        node.setId(nome);
        node.setOnAction(e -> RunnableEx.run(onAction));
        return node;
    }
}