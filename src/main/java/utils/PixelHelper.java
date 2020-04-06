package utils;

import static utils.DrawOnPoint.getWithinRange;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class PixelHelper {

    public static final int MAX_BYTE = 255;
    private int a;
    private int r;
    private int i;
    private int g;
    private int b;

    public PixelHelper() {
    }

    public PixelHelper(int argb) {
        reset(argb);
    }

    public void add(final int argb) {
        a += getByte(argb, 3);
        r += getByte(argb, 2);
        g += getByte(argb, 1);
        b += getByte(argb, 0);
        i++;
    }

    public void add(final int argb, final int mul) {
        a += getByte(argb, 3) * mul;
        r += getByte(argb, 2) * mul;
        g += getByte(argb, 1) * mul;
        b += getByte(argb, 0) * mul;
        i += mul;
    }

    public int diff(final int argb) {
        int trans = a - getByte(argb, 3);
        int red = r - getByte(argb, 2);
        int green = g - getByte(argb, 1);
        int blue = b - getByte(argb, 0);
        return Math.abs(red) + Math.abs(green) + Math.abs(blue) + Math.abs(trans);
    }
    public int modulus() {
        return Math.abs(r) + Math.abs(g) + Math.abs(b) + Math.abs(a);
    }

    public final void reset() {
        a = b = r = g = i = 0;
    }

    public final void reset(int argb) {
        a = getByte(argb, 3);
        r = getByte(argb, 2);
        g = getByte(argb, 1);
        b = getByte(argb, 0) & 0xFF;
        i = 1;
    }

    public int toArgb() {
        int red = getWithinRange(i == 0 ? r : r / i, 0, MAX_BYTE);
        int green = getWithinRange(i == 0 ? g : g / i, 0, MAX_BYTE);
        int blue = getWithinRange(i == 0 ? b : b / i, 0, MAX_BYTE);
        int trans = getWithinRange(i == 0 ? a : a / i, 0, MAX_BYTE);

        return trans << 8 * 3 | red << 8 * 2 | green << 8 | blue;
    }


    public Color toColor() {
        int red = getWithinRange(i == 0 ? r : r / i, 0, MAX_BYTE);
        int green = getWithinRange(i == 0 ? g : g / i, 0, MAX_BYTE);
        int blue = getWithinRange(i == 0 ? b : b / i, 0, MAX_BYTE);
        double transp = getWithinRange(i == 0 ? MAX_BYTE : a / (double) i, 0.0, MAX_BYTE) / MAX_BYTE;
        return Color.rgb(red, green, blue, transp);
    }

    public static Color asColor(final int argb) {
        int a = getByte(argb, 3);
        int r = getByte(argb, 2);
        int g = getByte(argb, 1);
        int b = getByte(argb, 0);
        return Color.rgb(r, g, b, a / (double) MAX_BYTE);
    }

    public static int getByte(final int argb, int i) {
        return argb >> 8 * i & 0xFF;
    }

    public static void replaceColor(WritableImage writableImage, Color backColor, Color transparent) {
        int colorToBe = PixelHelper.toArgb(transparent);
        int colorReplace = PixelHelper.toArgb(backColor);
        for (int i = 0; i < writableImage.getWidth(); i++) {
            for (int j = 0; j < writableImage.getHeight(); j++) {
                if (writableImage.getPixelReader().getArgb(i, j) == colorReplace) {
                    writableImage.getPixelWriter().setArgb(i, j, colorToBe);
                }
            }
        }
    }

    public static int toArgb(Color c) {
        int b = (int) (c.getBlue() * MAX_BYTE);
        int r = (int) (c.getRed() * MAX_BYTE);
        int g = (int) (c.getGreen() * MAX_BYTE);
        int a = (int) (c.getOpacity() * MAX_BYTE);
        return a << 8 * 3 | r << 8 * 2 | g << 8 | b;
    }
}