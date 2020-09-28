package simplebuilder;

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;

public class SimpleFlowPaneBuilder extends SimplePaneBuilder<FlowPane, SimpleFlowPaneBuilder> {


	public SimpleFlowPaneBuilder() {
		super(new FlowPane());
	}

	public SimpleFlowPaneBuilder columnHalignment(HPos left) {
        node.setColumnHalignment(left);
		return this;
	}

	public SimpleFlowPaneBuilder hgap(double i) {
        node.setHgap(i);
		return this;
	}

	public SimpleFlowPaneBuilder orientation(Orientation vertical) {
        node.setOrientation(vertical);
		return this;
	}


	public SimpleFlowPaneBuilder vgap(double i) {
        node.setVgap(i);
		return this;
	}



}