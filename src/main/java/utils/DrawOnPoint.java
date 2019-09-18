package utils;

import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

@FunctionalInterface
public interface DrawOnPoint {
	void draw(int x, int y);

    static WritableImage drawTransparentPattern(int size) {
        WritableImage transparentPattern = new WritableImage(size, size);
        return transparentImage(size, transparentPattern);
    }
	
    static double getWithinRange(final double num, final double min, final double max) {
	    return Math.min(Math.max(min, num), max);
	}
	
    static int getWithinRange(final int num, final int min, final int max) {
	    return Math.min(Math.max(min, num), max);
	}
	
    static WritableImage transparentImage(int size, WritableImage transparentPattern) {
        int squareSize = size / 16;
        for (int x = 0; x < transparentPattern.getWidth(); x++) {
            for (int y = 0; y < transparentPattern.getHeight(); y++) {
                transparentPattern.getPixelWriter().setColor(x, y,
                    x / squareSize % 2 == y / squareSize % 2 ? Color.WHITE : Color.GRAY);
            }
        }
        return transparentPattern;
    }

    static boolean within(final double y, final double max) {
	    return 0 <= y && y < max;
	}

    static boolean within(final int y, final double min, final double max) {
        return min <= y && y < max;
    }

    static boolean withinImage(final double x, final double y, final WritableImage image) {
        return within(y, image.getHeight()) && within(x, image.getWidth());
    }

    static boolean withinImage(final int x, final int y, final WritableImage image) {
        return within(y, image.getHeight()) && within(x, image.getWidth());
    }

}