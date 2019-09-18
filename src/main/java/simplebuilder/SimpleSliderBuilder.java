package simplebuilder;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

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
        onChange(slider, listener);
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

    public static VBox newSlider(final String string, final double min, final double max, int block,
        final Property<Number> radius) {
        return SimpleVBoxBuilder.newVBox(string,
            new SimpleSliderBuilder().min(min).max(max).blocks(block).bindBidirectional(radius).build());
    }

    public static VBox newSlider(final String string, final double min, final double max,
        final Property<Number> radius) {
        return SimpleVBoxBuilder.newVBox(string,
            new SimpleSliderBuilder().min(min).bindBidirectional(radius).max(max).build());
    }

    public static void onChange(Slider slider1, ChangeListener<? super Number> listener) {
        slider1.valueProperty().addListener(listener);
        slider1.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                listener.changed(slider1.valueProperty(), slider1.getValue(), slider1.getValue());
            }
        });
    }

}