package paintexp.tool;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;

public class BorderTool extends RectangleTool {

    private ImageView icon;
    private IntegerProperty length = new SimpleIntegerProperty(2);
	@Override
    public ImageView getIcon() {
		if (icon == null) {
            icon = getIconByURL("Border.png");

		}
		return icon;
	}



	@Override
	public void onSelected(final PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
		model.getToolOptions().getChildren()
				.add(new SimpleSliderBuilder(0, 6, 1).bindBidirectional(length).prefWidth(50).build());
	
	}


    protected void drawBorders(final int layoutX, final int layoutY,final int width, final int height, final PaintModel model) {
		Color[][] color2 = new Color[width][height];
		PixelHelper pixel = new PixelHelper();
		PixelHelper pixel2 = new PixelHelper();
		PixelHelper pixel3 = new PixelHelper();
		PixelHelper pixel4 = new PixelHelper();
		PixelReader reader = model.getImage().getPixelReader();
		int round = 1 << length.get();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixel.reset();
				pixel2.reset();
				pixel3.reset();
				pixel4.reset();
				if (withinRange(i + layoutX, j + layoutY, model)) {
					for (int k = -1; k < 2; k++) {
						for (int l = -1; l < 2; l++) {
							int x = setWithinRange(i + k + layoutX, 0, width - 1 + layoutX);
							int y = setWithinRange(j + l + layoutY, 0, height - 1 + layoutY);
							int argb2 = reader.getArgb(x, y);
							pixel.add(argb2, k + l);
							pixel2.add(argb2, k - l);
							pixel3.add(argb2, -k + l);
							pixel4.add(argb2, -k - l);
						}
					}
					int argb = pixel.toArgb(round);
					int argb2 = pixel2.toArgb(round);
					int argb3 = pixel3.toArgb(round);
					int argb4 = pixel4.toArgb(round);
					color2[i][j] = PixelHelper.asColor(argb | argb2 | argb3 | argb4).invert();
				}
			}
		}
		for (int i = 0; i < color2.length; i++) {
			for (int j = 0; j < color2[i].length; j++) {
				drawPoint(model, i + layoutX, j + layoutY, color2[i][j]);
			}
		}
	}



	@Override
	protected void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getWidth() > 2 && children.contains(getArea())) {
			Bounds boundsInLocal = getArea().getBoundsInParent();
            int startX = (int) boundsInLocal.getMinX();
            int startY = (int) boundsInLocal.getMinY();
            int height = (int) boundsInLocal.getHeight();
            int width = (int) boundsInLocal.getWidth();

			drawBorders(startX, startY, width, height, model);
		}
		children.remove(getArea());
	}

}
