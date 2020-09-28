package simplebuilder;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SimpleVBoxBuilder extends SimplePaneBuilder<VBox, SimpleVBoxBuilder> {


	public SimpleVBoxBuilder() {
		super(new VBox());
	}

    public SimpleVBoxBuilder(double gap, Node... spacing) {
        super(new VBox(gap, spacing));
    }

    public SimpleVBoxBuilder(Node... spacing) {
	    super(new VBox(spacing));
	}

	public SimpleVBoxBuilder alignment(Pos left) {
        node.setAlignment(left);
		return this;
	}

	public SimpleVBoxBuilder spacing(double left) {
        node.setSpacing(left);
		return this;
	}

    public static VBox newVBox(String text, Node... e) {
        VBox vBox = new VBox(new Text(text));
        vBox.getChildren().addAll(e);
        return vBox;
    }

}