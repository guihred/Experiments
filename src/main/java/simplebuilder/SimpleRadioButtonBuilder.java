package simplebuilder;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class SimpleRadioButtonBuilder extends SimpleRegionBuilder<RadioButton, SimpleRadioButtonBuilder> {


	public SimpleRadioButtonBuilder() {
		super(new RadioButton());
	}

	public SimpleRadioButtonBuilder selected(boolean toggleGrp) {
        node.setSelected(toggleGrp);
		return this;
	}

	public SimpleRadioButtonBuilder text(String string) {
        node.setText(string);
		return this;
	}

	public SimpleRadioButtonBuilder toggleGroup(ToggleGroup toggleGrp) {
        node.setToggleGroup(toggleGrp);
		return this;
	}


}