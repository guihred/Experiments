package paintexp.tool;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import paintexp.PaintModel;
import simplebuilder.SimpleSliderBuilder;

public class BlurTool extends PaintTool {

    private ImageView icon;


    private int y;

    private int x;


    private IntegerProperty length = new SimpleIntegerProperty(10);

	@Override
	public Node getIcon() {
		if (icon == null) {
            icon = getIconByURL("blur.png");
		}
		return icon;
	}

    @Override
	public Cursor getMouseCursor() {
        return Cursor.DEFAULT;
	}

    @Override
	public void onSelected(final PaintModel model) {
	    model.getToolOptions().getChildren().clear();
        model.getToolOptions().setSpacing(5);
        model.getToolOptions().getChildren()
                .add(new SimpleSliderBuilder(1, 50, 10).bindBidirectional(length).prefWidth(50).build());

	
	}

	@Override
    protected  void onMouseDragged(final MouseEvent e, final PaintModel model) {
		int y2 = (int) e.getY();
		int x2 = (int) e.getX();
        if (withinRange(x2, y2, model)) {
            drawLine(model, x, y, x2, y2, (x3, y3) -> drawUponOption(model, x3, y3));
			y = (int) e.getY();
			x = (int) e.getX();
        }
	}


    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
        y = (int) e.getY();
        x = (int) e.getX();
        drawUponOption(model, x, y);
    }

    private void drawBlur(int centerX, int centerY, PaintModel model) {
        int radius = length.get();
        double nPoints = 4 * radius;
        Color frontColor = model.getFrontColor();
        
        for (double t = 0; t < 2 * Math.PI; t += 2 * Math.PI / nPoints) {
            int x2 = (int) Math.round(radius * Math.cos(t));
            int y2 = (int) Math.round(radius * Math.sin(t));
            drawPoint(model, x2 + centerX, y2 + centerY, frontColor);
        }
    }

    private void drawUponOption(final PaintModel model, final int x2, final int y2) {

        if (withinRange(x2, y2, model)) {
            double r = length.getValue().doubleValue();
            drawPoint(model, x2, y2);
            for (double i = 1; i <= r; i++) {
                drawCircle(model, x2, y2, i, i, 12 * i, model.getFrontColor());
            }
        }
    }

}