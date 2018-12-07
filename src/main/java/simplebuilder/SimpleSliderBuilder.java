package simplebuilder;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;

public class SimpleSliderBuilder extends SimpleRegionBuilder<Slider, SimpleSliderBuilder> {

	protected Slider slider;
    private int blocks = 100;
	public SimpleSliderBuilder() {
		super(new Slider());
		slider = node;
	}

    public SimpleSliderBuilder(double min, double max, double value) {
        super(new Slider(min, max, value));
        slider = node;
    }

	public SimpleSliderBuilder bindBidirectional(Property<Number> other) {
        slider.valueProperty().bindBidirectional(other);
        return this;
    }
	public SimpleSliderBuilder blocks(int i) {
        blocks = i;
        node.setBlockIncrement((slider.getMax() - slider.getMin()) / blocks);
        return this;
    }

	@Override
    public Slider build() {
        node.setBlockIncrement((slider.getMax() - slider.getMin()) / blocks);
        return super.build();
    }

	public SimpleSliderBuilder max(double i) {
		slider.setMax(i);
		return this;
	}

    public SimpleSliderBuilder min(double i) {
		slider.setMin(i);
		return this;
	}

    public SimpleSliderBuilder onChange(ChangeListener<? super Number> listener) {
        slider.valueProperty()
                .addListener(listener);
        slider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                listener.changed(slider.valueProperty(), slider.getValue(), slider.getValue());
            }
        });
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