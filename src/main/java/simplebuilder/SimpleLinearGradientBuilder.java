package simplebuilder;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class SimpleLinearGradientBuilder implements SimpleBuilder<LinearGradient> {


	private CycleMethod cycleMethodp = CycleMethod.NO_CYCLE;
	private boolean proportionalp;
	private final List<Stop> stopsp = new ArrayList<>();
	private double startXp;
	private double startYp;
	private double endXp;
	private double endYp;

	@Override
	public LinearGradient build() {
		return new LinearGradient(startXp, startYp, endXp, endYp, proportionalp, cycleMethodp, stopsp);
	}

	public SimpleLinearGradientBuilder cycleMethod(CycleMethod v) {
		cycleMethodp = v;
		return this;
	}

	public SimpleLinearGradientBuilder endX(double v) {
		endXp = v;
		return this;
	}

	public SimpleLinearGradientBuilder endY(double v) {
		endYp = v;
		return this;
	}

	public SimpleLinearGradientBuilder proportional(boolean v) {
		proportionalp = v;
		return this;
	}

	public SimpleLinearGradientBuilder startX(double v) {
		startXp = v;
		return this;
	}

	public SimpleLinearGradientBuilder startY(double v) {
		startYp = v;
		return this;
	}

	public SimpleLinearGradientBuilder stops(List<Stop> v) {
		stopsp.clear();
		stopsp.addAll(v);
		return this;
	}
	public SimpleLinearGradientBuilder stops(Stop... v) {
		stopsp.clear();
		for (Stop element : v) {
			stopsp.add(element);
		}
		return this;
	}


}