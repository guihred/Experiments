package paintexp.tool;

import javafx.scene.image.WritableImage;
import paintexp.PaintModel;

@FunctionalInterface
public interface DrawOnPoint {
	void draw(int x, int y);

    static double getWithinRange(final double num, final double min, final double max) {
	    return Math.min(Math.max(min, num), max);
	}
	
    static int getWithinRange(final int num, final int min, final int max) {
	    return Math.min(Math.max(min, num), max);
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

    static boolean withinRange(final double x, final double y, final PaintModel model) {
        return withinImage(x, y, model.getImage());
    }

    static boolean withinRange(final int x, final int y, final PaintModel model) {
        WritableImage image = model.getImage();
        return withinImage(x, y, image);
    }

}