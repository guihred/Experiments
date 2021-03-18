package simplebuilder;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import utils.StringSigaUtils;

public class SimpleSliderBuilder extends SimpleRegionBuilder<Slider, SimpleSliderBuilder> {

    private int blocks = 100;

    public SimpleSliderBuilder() {
        super(new Slider());
    }

    public SimpleSliderBuilder(double min, double max, double value) {
        super(new Slider(min, max, value));
    }

    public SimpleSliderBuilder bindBidirectional(Property<Number> other) {
        node.valueProperty().bindBidirectional(other);
        return this;
    }

    @Override
    public Slider build() {
        node.setBlockIncrement((node.getMax() - node.getMin()) / blocks);
        return super.build();
    }

    public SimpleSliderBuilder max(double i) {
        node.setMax(i);
        return this;
    }

    public SimpleSliderBuilder min(double i) {
        node.setMin(i);
        return this;
    }

    public SimpleSliderBuilder orientation(Orientation value) {
        node.setOrientation(value);
        return this;
    }

    public SimpleSliderBuilder value(double i) {
        node.setValue(i);
        return this;
    }

    private SimpleSliderBuilder blocks(int nBlocks) {
        blocks = nBlocks;
        node.setBlockIncrement((node.getMax() - node.getMin()) / blocks);
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
                new SimpleSliderBuilder().id(StringSigaUtils.changeCase(string.replaceAll(" ", ""))).min(min)
                        .bindBidirectional(radius).max(max).build());
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