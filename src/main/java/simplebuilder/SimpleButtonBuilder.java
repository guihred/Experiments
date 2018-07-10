package simplebuilder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class SimpleButtonBuilder extends SimpleNodeBuilder<Button, SimpleButtonBuilder> {

    protected Button button;

	public SimpleButtonBuilder() {
        super(new Button());
        button = this.node;
	}

	public SimpleButtonBuilder text(String string) {
        button.setText(string);
		return this;
	}

    public SimpleButtonBuilder onAction(EventHandler<ActionEvent> value) {
        button.setOnAction(value);
        return this;
    }
}