package others;

import javafx.scene.control.Label;

public class SimpleLabelBuilder extends SimpleNodeBuilder<Label, SimpleLabelBuilder> implements SimpleBuilder<Label> {

	Label label;

	public SimpleLabelBuilder() {
		super(new Label());
		label = node;
	}


}