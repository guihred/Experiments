package simplebuilder;

import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class SimpleDropShadowBuilder implements SimpleBuilder<DropShadow> {

	private DropShadow dropShadow;

	public SimpleDropShadowBuilder() {
		dropShadow = new DropShadow();
	}

	@Override
	public DropShadow build() {
		return dropShadow;
	}

	public SimpleDropShadowBuilder color(Color dodgerblue) {
		dropShadow.setColor(dodgerblue);
		return this;
	}

	public SimpleDropShadowBuilder spread(double dodgerblue) {
		dropShadow.setSpread(dodgerblue);
		return this;
	}

}