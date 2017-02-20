package simplebuilder;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;

public class SimpleFlowPaneBuilder extends SimplePaneBuilder<FlowPane, SimpleFlowPaneBuilder> {

	protected FlowPane flowPane;

	public SimpleFlowPaneBuilder() {
		super(new FlowPane());
		flowPane = node;
	}

	@Override
	public SimpleFlowPaneBuilder padding(Insets insets) {
		flowPane.setPadding(insets);
		return this;
	}

	public SimpleFlowPaneBuilder orientation(Orientation vertical) {
		flowPane.setOrientation(vertical);
		return this;
	}

	public SimpleFlowPaneBuilder vgap(double i) {
		flowPane.setVgap(i);
		return this;
	}

	public SimpleFlowPaneBuilder hgap(double i) {
		flowPane.setHgap(i);
		return this;
	}

	public SimpleFlowPaneBuilder columnHalignment(HPos left) {
		flowPane.setColumnHalignment(left);
		return this;
	}

	public SimpleFlowPaneBuilder alignment(Pos left) {
		flowPane.setAlignment(left);
		return this;
	}



}