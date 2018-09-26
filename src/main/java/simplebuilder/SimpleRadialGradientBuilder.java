package simplebuilder;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class SimpleRadialGradientBuilder implements SimpleBuilder<RadialGradient> {


	private double centerXRG;
	private double centerYRG;
	private CycleMethod cycleMethodRG;
	private double focusAngleRG;
	private double focusDistanceRG;
	private boolean proportionalRG;
	private double radiusRG;
	private List<Stop> stopsRG = new ArrayList<>();

	@Override
	public RadialGradient build() {
		return new RadialGradient(focusAngleRG, focusDistanceRG, centerXRG, centerYRG, radiusRG, proportionalRG,
				cycleMethodRG, stopsRG);
	}

	public SimpleRadialGradientBuilder centerX(double v) {
		centerXRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder centerY(double v) {
		centerYRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder cycleMethod(CycleMethod v) {
		cycleMethodRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder focusAngle(double v) {
		focusAngleRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder focusDistance(double v) {
		focusDistanceRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder proportional(boolean v) {
		proportionalRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder radius(double v) {
		radiusRG = v;
		return this;
	}

	public SimpleRadialGradientBuilder stops(List<Stop> v) {
		stopsRG = v;
		return this;
	}
	public SimpleRadialGradientBuilder stops(Stop... v) {
		stopsRG.clear();
		for (Stop element : v) {
			stopsRG.add(element);
		}
		return this;
	}


}