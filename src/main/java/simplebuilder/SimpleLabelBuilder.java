package simplebuilder;

import javafx.scene.control.Label;

public class SimpleLabelBuilder extends SimpleNodeBuilder<Label, SimpleLabelBuilder> {

	protected Label label;

	public SimpleLabelBuilder() {
		super(new Label());
		label = node;
	}


}