package simplebuilder;

import javafx.scene.control.Label;

public class SimpleLabelBuilder extends SimpleNodeBuilder<Label, SimpleLabelBuilder> {

	public SimpleLabelBuilder() {
		super(new Label());
	}

}