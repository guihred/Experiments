package simplebuilder;

import javafx.scene.layout.HBox;

public class SimpleHBoxBuilder extends SimplePaneBuilder<HBox, SimpleHBoxBuilder> {

	protected HBox hbox;

	public SimpleHBoxBuilder() {
		super(new HBox());
		hbox = node;
	}

	public SimpleHBoxBuilder spacing(double left) {
		hbox.setSpacing(left);
		return this;
	}

}