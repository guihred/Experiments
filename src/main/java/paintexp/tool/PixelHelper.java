package paintexp.tool;
import static paintexp.tool.DrawOnPoint.getWithinRange;

import javafx.scene.paint.Color;

public class PixelHelper {

    private int a;
    private int r;
    private int i;
    private int g;
    private int b;

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

	public int modulus() {
	    return Math.abs(r) + Math.abs(g) + Math.abs(b) ;
    }

	public void reset() {
        a = b = r = g = i = 0;
	}

    public void reset(int argb) {
        a = argb >> 24 & 0xFF;
        r = argb >> 16 & 0xFF;
        g = argb >> 8 & 0xFF;
        b = argb & 0xFF;
        i = 1;
    }

	public int toArgb(final int round) {
        int red = getWithinRange(i == 0 ? r : r / i, 0, 255) / round * round;
        int green = getWithinRange(i == 0 ? g : g / i, 0, 255) / round * round;
        int blue = getWithinRange(i == 0 ? b : b / i, 0, 255) / round * round;
        int transp = getWithinRange(i == 0 ? 255 : a / i, 0, 255);
		return transp << 24 | red << 16 | green << 8 | blue;
	}

	public Color toColor() {
        int red = getWithinRange(i == 0 ? r : r / i, 0, 255);
        int green = getWithinRange(i == 0 ? g : g / i, 0, 255);
        int blue = getWithinRange(i == 0 ? b : b / i, 0, 255);
        double transp = getWithinRange(i == 0 ? 255 : a / (double) i, 0.0, 255) / 255;
		return Color.rgb(red, green, blue, transp);
	}

	public Color toColor(final int round) {
        int red = getWithinRange(i == 0 ? r : r / i, 0, 255) / round * round;
        int green = getWithinRange(i == 0 ? g : g / i, 0, 255) / round * round;
        int blue = getWithinRange(i == 0 ? b : b / i, 0, 255) / round * round;
        double transp = getWithinRange(i == 0 ? 255 : a / (double) i, 0.0, 255) / 255;
		return Color.rgb(red, green, blue, transp);
	}

    public static Color asColor(final int argb) {
        int a = argb >> 24 & 0xFF;
        int r = argb >> 16 & 0xFF;
        int g = argb >> 8 & 0xFF;
        int b = argb & 0xFF;
        return Color.rgb(r, g, b, a / (double) 255);
    }

    public static int toArgb(Color c) {
        int b = (int) (c.getBlue() * 255);
        int r = (int) (c.getRed() * 255);
        int g = (int) (c.getGreen() * 255);
        int a = (int) (c.getOpacity() * 255);
        return a << 24 | r << 16 | g << 8 | b;
    }
}