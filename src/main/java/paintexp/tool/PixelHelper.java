package paintexp.tool;

import javafx.scene.paint.Color;

class PixelHelper {

	int a;

	int r;
	int i;
	int g;
	int b;

	public void add(final int argb) {
        a += argb >> 24 & 0xFF;
        r += argb >> 16 & 0xFF;
        g += argb >> 8 & 0xFF;
        b += argb & 0xFF;
		i++;
	}

	public void add(final int argb, final int mul) {
		a += (argb >> 24 & 0xFF) * mul;
		r += (argb >> 16 & 0xFF) * mul;
		g += (argb >> 8 & 0xFF) * mul;
		b += (argb & 0xFF) * mul;
		i += mul;
	}

	public void reset() {
		a = b = r = g = 0;
	}

	public Color toColor() {
		int red = PaintTool.setWithinRange(i == 0 ? r : r / i, 0, 255);
		int green = PaintTool.setWithinRange(i == 0 ? g : g / i, 0, 255);
		int blue = PaintTool.setWithinRange(i == 0 ? b : b / i, 0, 255);
		double transp = PaintTool.setWithinRange(i == 0 ? 255 : a / (double) i, 0.0, 255) / 255;
		return Color.rgb(red, green, blue, transp);
	}

	public Color toColor(final int round) {
		int red = PaintTool.setWithinRange(i == 0 ? r : r / i, 0, 255) / round * round;
		int green = PaintTool.setWithinRange(i == 0 ? g : g / i, 0, 255) / round * round;
		int blue = PaintTool.setWithinRange(i == 0 ? b : b / i, 0, 255) / round * round;
		double transp = PaintTool.setWithinRange(i == 0 ? 255 : a / (double) i, 0.0, 255) / 255;
		return Color.rgb(red, green, blue, transp);
	}

}