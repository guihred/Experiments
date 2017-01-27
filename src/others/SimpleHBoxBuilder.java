package others;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class SimpleHBoxBuilder extends SimplePaneBuilder<HBox, SimpleHBoxBuilder> {

	HBox hbox;

	public SimpleHBoxBuilder() {
		super(new HBox());
		hbox = node;
	}

	public SimpleHBoxBuilder alignment(Pos left) {
		hbox.setAlignment(left);
		return this;
	}

	public SimpleHBoxBuilder spacing(double left) {
		hbox.setSpacing(left);
		return this;
	}

}