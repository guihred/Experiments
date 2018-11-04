package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

@SuppressWarnings("unchecked")
public class SimpleRegionBuilder<T extends Region, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {

	protected T region;

	public SimpleRegionBuilder(T region) {
		super(region);
		this.region = region;
	}

    public Z minWidth(double minWidth) {
        region.setMinWidth(minWidth);
	    return (Z) this;
    }

    public Z prefHeight(double value) {
		region.setPrefHeight(value);
		return (Z) this;
	}
	public Z prefHeight(ObservableValue<? extends Number> value) {
        region.prefHeightProperty().bind(value);
        return (Z) this;
    }

    public Z prefWidth(double value) {
		region.setPrefWidth(value);
		return (Z) this;
	}

    public Z prefWidth(ObservableValue<? extends Number> value) {
        region.prefWidthProperty().bind(value);
        return (Z) this;
    }

    public Z scaleShape(boolean value) {
        region.setScaleShape(value);
        return (Z) this;
    }

	public static SimpleRegionBuilder<Region, SimpleRegionBuilder<Region, SimpleBuilder<Region>>> create() {
		return new SimpleRegionBuilder<>(new Region());
	}




}