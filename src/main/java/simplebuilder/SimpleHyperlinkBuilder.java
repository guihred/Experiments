package simplebuilder;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;

public class SimpleHyperlinkBuilder extends SimpleRegionBuilder<Hyperlink, SimpleHyperlinkBuilder> {


	public SimpleHyperlinkBuilder() {
		super(new Hyperlink());
	}

	public SimpleHyperlinkBuilder onAction(EventHandler<ActionEvent> string) {
        node.setOnAction(string);
		return this;
	}

	public SimpleHyperlinkBuilder text(String string) {
        node.setText(string);
		return this;
	}



}