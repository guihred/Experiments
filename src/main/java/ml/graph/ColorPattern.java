package ml.graph;

import javafx.scene.paint.Color;

public enum ColorPattern {
	BRIGHTNESS, SATURATION, HUE;
    private static final double BLUE_HUE = Color.BLUE.getHue();
    private static final double RED_HUE = Color.RED.getHue();


	public Color getColorForValue(final double value, final double min,
			final double max) {
		return getColorForValue(this, value, min, max);
	}

	public static Color getColorForValue(final ColorPattern colorPattern, final double value, final double min,
			final double max) {
		if (value < min || value > max) {
			return Color.BLACK;
		}
		switch (colorPattern) {
			case BRIGHTNESS:
				double brightness = 1 - (value - min) / (max - min);
				return Color.hsb(RED_HUE, 1.0, brightness);
			case HUE:
				double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - min) / (max - min);
				return Color.hsb(hue, 1.0, 1.0);
			case SATURATION:
				double saturation = (value - min) / (max - min);
				return Color.hsb(RED_HUE, saturation, 1.0);
			default:
				return Color.BLACK;
		}

	}

}