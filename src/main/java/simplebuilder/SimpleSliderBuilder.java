package simplebuilder;

import javafx.beans.property.Property;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;

public class SimpleSliderBuilder extends SimpleRegionBuilder<Slider, SimpleSliderBuilder> {

	protected Slider slider;

	public SimpleSliderBuilder() {
		super(new Slider());
		slider = node;
	}

	public SimpleSliderBuilder bindBidirectional(Property<Number> other) {
        slider.valueProperty().bindBidirectional(other);
        return this;
    }
	public SimpleSliderBuilder max(double i) {
		slider.setMax(i);
		return this;
	}

	public SimpleSliderBuilder min(double i) {
		slider.setMin(i);
		return this;
	}

	public SimpleSliderBuilder orientation(Orientation value) {
		node.setOrientation(value);
		return this;
	}

    public SimpleSliderBuilder value(double i) {
		slider.setValue(i);
		return this;
	}


}