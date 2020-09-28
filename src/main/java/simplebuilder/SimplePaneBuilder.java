package simplebuilder;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

@SuppressWarnings("unchecked")
public class SimplePaneBuilder<T extends Pane, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {


	protected SimplePaneBuilder(T shape) {
		super(shape);
	}

	public Z children(Node... label4) {
        node.getChildren().setAll(label4);
		return (Z) this;
	}


    public Z padding(Insets insets) {
        node.setPadding(insets);
        return (Z) this;
    }


    public Z padding(int top, int right, int bottom, int left) {
        node.setPadding(new Insets(top, right, bottom, left));
        return (Z) this;
    }

}