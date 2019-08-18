package simplebuilder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class SimpleButtonBuilder extends SimpleNodeBuilder<Button, SimpleButtonBuilder> {

    protected Button button;

	public SimpleButtonBuilder() {
        super(new Button());
        button = node;
	}

	public SimpleButtonBuilder onAction(EventHandler<ActionEvent> value) {
        button.setOnAction(value);
        return this;
    }

    public SimpleButtonBuilder text(String string) {
        button.setText(string);
		return this;
	}

    public static Button newButton(final double layoutX, final double layoutY, final String nome,
        final EventHandler<ActionEvent> onAction) {
        Button button = new Button(nome);
        button.setLayoutX(layoutX);
        button.setLayoutY(layoutY);
        button.setOnAction(onAction);
        return button;
    }

    public static Button newButton(final Node graphic, final String id, final EventHandler<ActionEvent> onAction) {
        Button button = new Button(null, graphic);
        button.setId(id);
        button.setOnAction(onAction);
        return button;
    }

    public static Button newButton(final String nome, final EventHandler<ActionEvent> onAction) {
        Button button = new Button(nome);
        button.setId(nome);
        button.setOnAction(onAction);
        return button;
    }
}