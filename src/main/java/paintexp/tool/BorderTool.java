package paintexp.tool;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleSvgPathBuilder;

public class BorderTool extends RectangleTool {

	private SVGPath icon;
    private IntegerProperty length = new SimpleIntegerProperty(10);
	@Override
	public SVGPath getIcon() {
		if (icon == null) {
			icon = new SimpleSvgPathBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.content("M6,0 l -4,6 l -4,-6 z").build();
		}
		return icon;
	}



	@Override
	public void onSelected(final PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());
	
	}


    protected void drawBorders(final int layoutX, final int layoutY,final int width, final int height, final PaintModel model) {
		Color[][] color2 = new Color[width][height];
		PixelHelper pixel = new PixelHelper();
		PixelReader reader = model.getImage().getPixelReader();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixel.reset();
				if (withinRange(i + layoutX, j + layoutY, model)) {
					for (int k = -1; k < 2; k++) {
						for (int l = -1; l < 2; l++) {
							int x = setWithinRange(i + k + layoutX, 0, width - 1 + layoutX);
							int y = setWithinRange(j + l + layoutY, 0, height - 1 + layoutY);
							int argb2 = reader.getArgb(x, y);
							pixel.add(argb2, k + l);
						}
					}
					color2[i][j] = pixel.toColor(2).invert();
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
