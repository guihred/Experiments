package simplebuilder;

import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class SimpleRadioButtonBuilder extends SimpleRegionBuilder<RadioButton, SimpleRadioButtonBuilder> {

	protected RadioButton radioButton;

	public SimpleRadioButtonBuilder() {
		super(new RadioButton());
		radioButton = region;
	}

	public SimpleRadioButtonBuilder selected(boolean toggleGrp) {
		radioButton.setSelected(toggleGrp);
		return this;
	}

	public SimpleRadioButtonBuilder text(String string) {
		radioButton.setText(string);
		return this;
	}

	public SimpleRadioButtonBuilder toggleGroup(ToggleGroup toggleGrp) {
		radioButton.setToggleGroup(toggleGrp);
		return this;
	}


}