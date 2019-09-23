package paintexp.tool;

import static utils.DrawOnPoint.within;
import static utils.DrawOnPoint.withinImage;

import javafx.beans.property.Property;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.DrawOnPoint;
import utils.ResourceFXUtils;

public final class PaintToolHelper {
	static final int N_POINTS_MULTIPLIER = 16;

	private PaintToolHelper() {
	}

	public static void copyImagePart(Image srcImage, WritableImage destImage, Bounds bounds) {
		int x = (int) bounds.getMinX();
		int y = (int) bounds.getMinY();
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		new RectBuilder().startX(x).startY(y).width(width).height(height).copyImagePart(srcImage, destImage,
				Color.TRANSPARENT);
	}

	public static void drawCircle(PaintModel model, double centerX, double centerY, double radiusX, double radiusY,
			double startAngle) {
		Color frontColor = model.getFrontColor();
		drawCirclePart(model, centerX, centerY, radiusX, radiusY, startAngle, frontColor);
	}

	public static void drawCircle(PaintModel model, int centerX, int centerY, double radiusX, double radiusY,
			Color color) {
		double nPoints = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
		for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
			int x = (int) Math.round(radiusX * Math.cos(t));
			int y = (int) Math.round(radiusY * Math.sin(t));
			drawPoint(model, x + centerX, y + centerY, color);
		}
	}

	public static void drawCircle(PaintModel model, int centerX, int centerY, double radiusX, double radiusY,
			Color color, double opacity) {
		double nPoints = Math.max(radiusX, radiusY) * N_POINTS_MULTIPLIER;
		for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
			int x = (int) Math.round(radiusX * Math.cos(t));
			int y = (int) Math.round(radiusY * Math.sin(t));
			drawPointTransparency(model, x + centerX, y + centerY, color, opacity);
		}
	}

	public static void drawCirclePart(PaintModel model, double centerX, double centerY, double radiusX, double radiusY,
			double startAngle, Color frontColor) {
		double nPoints2 = Math.max(radiusX, radiusY) * PaintToolHelper.N_POINTS_MULTIPLIER;
		double angle = Math.PI / 2;
		for (double t = 0; t < angle; t += 2 * Math.PI / nPoints2) {
			int x = (int) Math.round(radiusX * Math.cos(t + startAngle));
			int y = (int) Math.round(radiusY * Math.sin(t + startAngle));
			drawPoint(model, x + (int) centerX, y + (int) centerY, frontColor);
		}
	}

	public static void drawLine(PaintModel model, double startX, double startY, double endX, double endY) {
		drawLine(model, startX, startY, endX, endY,
				(x, y) -> model.getImage().getPixelWriter().setColor(x, y, model.getFrontColor()));
	}

	public static void drawLine(PaintModel model, double startX, double startY, double endX, double endY, Color color) {
		drawLine(model, startX, startY, endX, endY, (x, y) -> model.getImage().getPixelWriter().setColor(x, y, color));
	}

	public static void drawLine(PaintModel model, double startX, double startY, double endX, double endY, Color color,
			double opacity) {
		drawLine(model, startX, startY, endX, endY, (x, y) -> drawPointTransparency(model, x, y, color, opacity));
	}

	public static void drawLine(PaintModel model, double startX, double startY, double endX, double endY,
			DrawOnPoint onPoint) {
		double deltaX = endX - startX;
		double a = deltaX == 0 ? Double.NaN : (endY - startY) / deltaX;
		double b = Double.isNaN(a) ? Double.NaN : endY - a * endX;
		// y = x * a + b

		double minX = Math.min(startX, endX);
		double maxX = Math.max(startX, endX);
		double minY = Math.min(startY, endY);
		double maxY = Math.max(startY, endY);
		for (int x = (int) minX; x < maxX; x++) {
			int y = (int) (!Double.isNaN(a) ? Math.round(a * x + b) : endY);
			if (withinImage(x, y, model.getImage()) && within(x, minX - 1, maxX + 1) && within(y, minY - 1, maxY + 1)) {
				onPoint.draw(x, y);
			}
		}
		for (int y = (int) minY; y < maxY; y++) {
			if (a != 0) {
				int x = (int) (Double.isNaN(a) ? startX : Math.round((y - b) / a));
				final int x1 = x;
				final int y1 = y;
				final PaintModel model1 = model;
				if (withinImage(x1, y1, model1.getImage()) && within(x, minX - 1, maxX + 1)
						&& within(y, minY - 1, maxY + 1)) {
					onPoint.draw(x, y);
				}
			}
		}
	}

	public static void drawPoint(PaintModel model, int x2, int y2) {
		Color frontColor = model.getFrontColor();
		drawPoint(model, x2, y2, frontColor);
	}

	public static void drawPoint(PaintModel model, int x2, int y2, Color frontColor) {
		if (withinImage(x2, y2, model.getImage())) {
			model.getImage().getPixelWriter().setColor(x2, y2, frontColor);
		}
	}

	public static void drawPointIf(PaintModel model, int x2, int y2, int color, Color backColor) {
		final int x = x2;
		final int y = y2;
		final PaintModel model1 = model;
		if (withinImage(x, y, model1.getImage())) {
			int argb = model.getImage().getPixelReader().getArgb(x2, y2);
			if (argb == color) {
				model.getImage().getPixelWriter().setColor(x2, y2, backColor);
			}
		}
	}

	public static void drawPointTransparency(PaintModel model, int x2, int y2, Color frontColor, double opacity) {
		final int x = x2;
		final int y = y2;
		final PaintModel model1 = model;
		if (withinImage(x, y, model1.getImage())) {
			int index = Math.max(model.getImageVersions().size() - 1, 0);
			Color color = model.getImageVersions().get(index).getPixelReader().getColor(x2, y2);
			Color color2 = color.interpolate(frontColor, opacity);
			model.getImage().getPixelWriter().setColor(x2, y2, color2);
		}
	}

	public static void drawSquareLine(PaintModel model, int startX, int startY, int w, Color color) {
		for (int x = 0; x < w; x++) {
			PaintToolHelper.drawPoint(model, startX + x, startY, color);
			PaintToolHelper.drawPoint(model, startX, startY + x, color);
			PaintToolHelper.drawPoint(model, startX + x, startY + w, color);
			PaintToolHelper.drawPoint(model, startX + w, startY + x, color);
		}
	}

	public static void drawSquareLine(PaintModel model, int startX, int startY, int w, Color color, double opacity) {
		for (int x = 0; x < w; x++) {
			drawPointTransparency(model, startX + x, startY, color, opacity);
			drawPointTransparency(model, startX, startY + x, color, opacity);
			drawPointTransparency(model, startX + x, startY + w, color, opacity);
			drawPointTransparency(model, startX + w, startY + x, color, opacity);
		}
	}

	public static void drawSquareLine(PaintModel model, int x, int y, int w, int color) {
		Color backColor = model.getBackColor();
		for (int i = 0; i < w; i++) {
			PaintToolHelper.drawPointIf(model, x + i, y, color, backColor);
			PaintToolHelper.drawPointIf(model, x, y + i, color, backColor);
			PaintToolHelper.drawPointIf(model, x + w, y + i, color, backColor);
			PaintToolHelper.drawPointIf(model, x + i, y + w, color, backColor);
		}
	}

	public static ImageView getIconByURL(String src) {
		return PaintToolHelper.getIconByURL(src, 30);

	}

	public static ImageView getIconByURL(String src, double width) {
		ImageView icon1 = new ImageView(ResourceFXUtils.toExternalForm("paint/" + src));
		icon1.setPreserveRatio(true);
		icon1.setFitWidth(width);
		icon1.setFitHeight(width);
		icon1.maxWidth(width);
		icon1.maxHeight(width);
		return icon1;

	}

	public static void handleSlider(KeyEvent e, Property<Number> property, Slider slider) {
		if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.PLUS) {
			property.setValue(
					Math.min(slider.getMax(), slider.getBlockIncrement() + property.getValue().doubleValue()));
		}
		if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
			property.setValue(
					Math.max(slider.getMin(), property.getValue().doubleValue() - slider.getBlockIncrement()));
		}
	}

	public static boolean isEqualImage(WritableImage image, WritableImage image2) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (image.getPixelReader().getArgb(i, j) != image2.getPixelReader().getArgb(i, j)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void moveArea(KeyCode code, Rectangle area2) {
		switch (code) {
			case RIGHT:
				area2.setLayoutX(area2.getLayoutX() + 1);
				break;
			case LEFT:
				area2.setLayoutX(area2.getLayoutX() - 1);
				break;
			case DOWN:
				area2.setLayoutY(area2.getLayoutY() + 1);
				break;
			case UP:
				area2.setLayoutY(area2.getLayoutY() - 1);
				break;
			default:
				break;
		}
	}

	public static void takeSnapshotFill(PaintModel model, Node line2) {
		Bounds bounds = line2.getBoundsInParent();
		int width = (int) bounds.getWidth() + 2;
		int height = (int) bounds.getHeight() + 2;
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		WritableImage textImage = line2.snapshot(params, new WritableImage(width, height));
		int x = (int) bounds.getMinX();
		int y = (int) bounds.getMinY();
		new RectBuilder().startX(0).startY(0).width(width).height(height).endX(x).endY(y).copyImagePart(textImage,
				model.getImage(), Color.TRANSPARENT);
		model.getImageStack().getChildren().clear();
		ImageView imageView = new ImageView(model.getImage());
		model.getImageStack().getChildren().add(model.getRectangleBorder(imageView));
		model.getImageStack().getChildren().add(imageView);
	}

}
