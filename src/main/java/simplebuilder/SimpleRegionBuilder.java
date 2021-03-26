package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

@SuppressWarnings("unchecked")
public class SimpleRegionBuilder<T extends Region, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {


	public SimpleRegionBuilder(final T region) {
		super(region);
	}

    public Z maxWidth(final double minWidth) {
        node.setMaxWidth(minWidth);
    	return (Z) this;
    }

	public Z minWidth(final double minWidth) {
        node.setMinWidth(minWidth);
	    return (Z) this;
    }

    public Z prefHeight(final ObservableValue<? extends Number> value) {
        node.prefHeightProperty().bind(value);
        return (Z) this;
    }
    public Z prefWidth(final ObservableValue<? extends Number> value) {
        node.prefWidthProperty().bind(value);
        return (Z) this;
    }




    public static <T extends SimpleRegionBuilder<Region, SimpleRegionBuilder<Region, SimpleBuilder<Region>>>>
        SimpleRegionBuilder<Region, T> create() {
		return new SimpleRegionBuilder<>(new Region());
	}




}