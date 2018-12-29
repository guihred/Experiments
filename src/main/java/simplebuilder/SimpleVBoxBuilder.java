package simplebuilder;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class SimpleVBoxBuilder extends SimplePaneBuilder<VBox, SimpleVBoxBuilder> {

	protected VBox vbox;

	public SimpleVBoxBuilder() {
		super(new VBox());
		vbox = node;
	}

    public SimpleVBoxBuilder(double gap, Node... spacing) {
        super(new VBox(gap, spacing));
        vbox = node;
    }

    public SimpleVBoxBuilder(Node... spacing) {
	    super(new VBox(spacing));
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