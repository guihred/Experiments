package paintexp.tool;

import java.util.Arrays;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleSvgPathBuilder;

public class BlurTool extends PaintTool {

	private Node icon;
    private int y;
    private int x;
    private IntegerProperty length = new SimpleIntegerProperty(10);
	private Color[] colors = new Color[length.get() * length.get() * 4];

    @Override
	public Node getIcon() {
		if (icon == null) {
			icon = new SimpleSvgPathBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.content("M6,0 l -4,6 a5,5 0 1,0 8,0 l -4,-6 z").build();
		}
		return icon;
	}


	@Override
	public void onSelected(final PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());
		length.addListener((o, old, value) -> colors = new Color[value.intValue() * value.intValue() * 4]);
	
	}


    @Override
    protected  void onMouseDragged(final MouseEvent e, final PaintModel model) {
		int y2 = (int) e.getY();
		int x2 = (int) e.getX();
        if (withinRange(x2, y2, model)) {
            drawLine(model, x, y, x2, y2, (x3, y3) -> drawBlur(x3, y3, model));
			y = (int) e.getY();
			x = (int) e.getX();
        }
	}

	@Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
        y = (int) e.getY();
        x = (int) e.getX();
        drawBlur(x, y, model);
    }

    private void drawBlur(final int centerX, final int centerY, final PaintModel model) {
        final int radius = length.get();
        final int diameter = radius * 2;
        final int height = (int) model.getImage().getHeight();
        final int width = (int) model.getImage().getWidth();
        Arrays.fill(colors, null);
        for (double i = 0; i <= radius; i++) {
            double nPoints = 4 * i + 1;
            for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
                int x2 = (int) Math.round(i * Math.cos(t));
                int y2 = (int) Math.round(i * Math.sin(t));
                int a = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                if (withinRange(x2 + centerX, y2 + centerY, model)) {
                    int pix = model.getImage().getPixelReader().getArgb(x2 + centerX, y2 + centerY);
                    a += (pix >> 24 & 0xFF) * 5;
                    r += (pix >> 16 & 0xFF) * 5;
                    g += (pix >> 8 & 0xFF) * 5;
                    b += (pix & 0xFF) * 5;

                    for (int j = -1; j < 2; j += 2) {
                        int argb = model.getImage().getPixelReader().getArgb(x2 + centerX,
                                setWithinRange(y2 + j + centerY, 0, height - 1));
                        a += argb >> 24 & 0xFF;
                        r += argb >> 16 & 0xFF;
                        g += argb >> 8 & 0xFF;
                        b += argb & 0xFF;
                        int argb2 = model.getImage().getPixelReader()
                                .getArgb(setWithinRange(x2 + j + centerX, 0, width - 1), y2 + centerY);
                        a += argb2 >> 24 & 0xFF;
                        r += argb2 >> 16 & 0xFF;
                        g += argb2 >> 8 & 0xFF;
                        b += argb2 & 0xFF;
                    }
                    int red = setWithinRange(r / 9, 0, 255);
                    int green = setWithinRange(g / 9, 0, 255);
                    int blue = setWithinRange(b / 9, 0, 255);
                    double transp = setWithinRange(a / 9.0, 0, 255);
                    int j = setWithinRange(x2 + radius, 0, diameter - 1);
                    int c = setWithinRange(y2 + radius, 0, diameter - 1);
                    colors[j * diameter + c] = Color.rgb(red, green, blue, transp / 255);
                }
            }
        }
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != null) {
                drawPoint(model, i / diameter - radius + centerX, i % diameter - radius + centerY, colors[i]);
            }
        }
    }

}