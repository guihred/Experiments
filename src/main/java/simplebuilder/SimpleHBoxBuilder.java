package simplebuilder;

import javafx.scene.layout.HBox;

public class SimpleHBoxBuilder extends SimplePaneBuilder<HBox, SimpleHBoxBuilder> {


	public SimpleHBoxBuilder() {
		super(new HBox());
	}

	public SimpleHBoxBuilder spacing(double left) {
        node.setSpacing(left);
		return this;
	}

}