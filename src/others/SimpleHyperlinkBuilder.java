package others;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;

public class SimpleHyperlinkBuilder extends SimpleRegionBuilder<Hyperlink, SimpleHyperlinkBuilder> implements SimpleBuilder<Hyperlink>{

	Hyperlink hyperlink;

	public SimpleHyperlinkBuilder() {
		super(new Hyperlink());
		hyperlink = region;
	}

	public SimpleHyperlinkBuilder text(String string) {
		hyperlink.setText(string);
		return this;
	}

	public SimpleHyperlinkBuilder onAction(EventHandler<ActionEvent> string) {
		hyperlink.setOnAction(string);
		return this;
	}



}