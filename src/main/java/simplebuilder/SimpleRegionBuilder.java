package simplebuilder;

import javafx.scene.layout.Region;

@SuppressWarnings("unchecked")
public class SimpleRegionBuilder<T extends Region, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {

	protected T region;

	public SimpleRegionBuilder(T region) {
		super(region);
		this.region = region;
	}

	public Z prefHeight(double value) {
		region.setPrefHeight(value);
		return (Z) this;
	}

    public Z scaleShape(boolean value) {
        region.setScaleShape(value);
        return (Z) this;
    }
	public Z prefWidth(double value) {
		region.setPrefWidth(value);
		return (Z) this;
	}

	public static SimpleRegionBuilder<Region, SimpleRegionBuilder<Region, SimpleBuilder<Region>>> create() {
		return new SimpleRegionBuilder<>(new Region());
	}




}