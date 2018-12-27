package simplebuilder;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

@SuppressWarnings("unchecked")
public class SimplePaneBuilder<T extends Pane, Z extends SimpleBuilder<T>> extends SimpleNodeBuilder<T, Z> {

	protected T pane;

	protected SimplePaneBuilder(T shape) {
		super(shape);
		this.pane = shape;
	}

	public Z children(Node... label4) {
		pane.getChildren().setAll(label4);
		return (Z) this;
	}


	public Z padding(Insets insets) {
		pane.setPadding(insets);
		return (Z) this;
	}

    public Z padding(int insets) {
        pane.setPadding(new Insets(insets));
        return (Z) this;
    }

    public Z padding(int top, int right, int bottom, int left) {
        pane.setPadding(new Insets(top, right, bottom, left));
        return (Z) this;
    }

}