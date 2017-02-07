package simplebuilder;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class SimpleVBoxBuilder extends SimplePaneBuilder<VBox, SimpleVBoxBuilder> {

	VBox vbox;

	public SimpleVBoxBuilder() {
		super(new VBox());
		vbox = node;
	}

	public SimpleVBoxBuilder alignment(Pos left) {
		vbox.setAlignment(left);
		return this;
	}

	public SimpleVBoxBuilder spacing(double left) {
		vbox.setSpacing(left);
		return this;
	}

}