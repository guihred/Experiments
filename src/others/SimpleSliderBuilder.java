package others;

import javafx.geometry.Orientation;
import javafx.scene.control.Slider;

public class SimpleSliderBuilder extends SimpleRegionBuilder<Slider, SimpleSliderBuilder>
		implements SimpleBuilder<Slider> {

	Slider slider;

	public SimpleSliderBuilder() {
		super(new Slider());
		slider = node;
	}

	public SimpleSliderBuilder orientation(Orientation value) {
		node.setOrientation(value);
		return this;
	}
	public SimpleSliderBuilder min(double i) {
		slider.setMin(i);
		return this;
	}

	public SimpleSliderBuilder max(double i) {
		slider.setMax(i);
		return this;
	}

	public SimpleSliderBuilder value(double i) {
		slider.setValue(i);
		return this;
	}


}