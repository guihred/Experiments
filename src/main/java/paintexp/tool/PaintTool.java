package paintexp.tool;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import paintexp.PaintModel;

public abstract class PaintTool extends Group {
    public PaintTool() {
        getChildren().add(getIcon());
    }

    public abstract Node getIcon();

    public abstract Cursor getMouseCursor();

	@SuppressWarnings("unused")
    public void handleEvent(final MouseEvent e, final PaintModel model) {
		// DOES NOTHING
	}

	@SuppressWarnings("unused")
	public void handleKeyEvent(final KeyEvent e, final PaintModel paintModel) {
		// DOES NOTHING
	}

    @SuppressWarnings("unused")
	public void onSelected(final PaintModel model) {
		// DOES NOTHING
	}

	protected void drawCircle(final PaintModel model, int centerX, int centerY, double radiusX, double radiusY,
            double nPoints) {
        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x = (int) Math.round(radiusX * Math.cos(t));
            int y = (int) Math.round(radiusY * Math.sin(t));
            drawPoint(model, x + centerX, y + centerY);
        }
    }
    protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX, final double endY) {
        drawLine(model, startX, startY, endX, endY,
                (x, y) -> model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor()));
    }

    protected void drawLine(final PaintModel model, final double startX, final double startY, final double endX,
            final double endY, DrawOnPoint onPoint) {
        double d = endX - startX;
        double a = d == 0 ? Double.NaN : (endY - startY) / d;
        double b = Double.isNaN(a) ? Double.NaN : endY - a * endX;

        double minX = Double.min(startX, endX);
        double maxX = Double.max(startX, endX);
        for (int x = (int) minX; x < maxX; x++) {
            int y = (int) (!Double.isNaN(a) ? Math.round(a * x + b) : endY);
            if (withinRange(x, y, model)) {
                onPoint.draw(x, y);
            }
        }

        double minY = Double.min(startY, endY);
        double maxY = Double.max(startY, endY);
        for (int y = (int) minY; y < maxY; y++) {
            if (a != 0) {
                int x = (int) (Double.isNaN(a) ? startX : Math.round((y - b) / a));
                if (withinRange(x, y, model)) {
                    onPoint.draw(x, y);
                }
            }
        }
    }

    protected void drawPoint(final PaintModel model, final int x2, final int y2) {
		if (withinRange(x2, y2, model)) {
			model.getImage().getPixelWriter().setColor(x2, y2, model.getFrontColor());
		}
	}
    protected void drawRect(final PaintModel model, final double x, final double y, final double w, final double h) {
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (withinRange((int) x + i, (int) y + j, model)) {
					model.getImage().getPixelWriter().setColor((int) x + i, (int) y + j, model.getBackColor());
				}
			}
		}
	}

	protected void drawSquare(final PaintModel model, final int x, final int y, final int w, Color backColor) {
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < w; j++) {
                if (withinRange(x + i, y + j, model)) {
                    model.getImage().getPixelWriter().setColor(x + i, y + j, backColor);
                }
            }
        }
    }


    protected void drawSquare(final PaintModel model, final int x, final int y, final int w, final int color) {
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < w; j++) {
				if (withinRange(x + i, y + j, model)) {
					int argb = model.getImage().getPixelReader().getArgb(x + i, y + j);
					if (argb == color) {
						model.getImage().getPixelWriter().setColor(x + i, y + j, model.getBackColor());
					}
				}
			}
		}
	}

	protected double setWithinRange(double num, double min, double max) {
        return Double.min(Double.max(min, num), max);
    }

	protected boolean within(final int y, final double min) {
		return 0 <= y && y < min;
	}

	protected boolean within(final int y, final double min, final double max) {
		return min <= y && y < max;
    }

    protected boolean withinRange(final int x, final int y, final int initialX, final int initialY, final double bound, final PaintModel model) {
		return within(y, Double.max(initialY - bound, 0), Double.min(initialY + bound, model.getImage().getHeight()))
				&& within(x, Double.max(initialX - bound, 0),
						Double.min(initialX + bound, model.getImage().getWidth()));
	}

	protected boolean withinRange(final int x, final int y, final PaintModel model) {
        return within(y, model.getImage().getHeight()) && within(x, model.getImage().getWidth());
    }

    @FunctionalInterface
interface DrawOnPoint{
        void draw(int x, int y);
}

}