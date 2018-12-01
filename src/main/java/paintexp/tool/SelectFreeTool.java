package paintexp.tool;
import static paintexp.tool.DrawOnPoint.getWithinRange;
import static paintexp.tool.DrawOnPoint.withinRange;

import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.SVGPath;
import paintexp.PaintModel;
import paintexp.PaintTools;
import simplebuilder.SimpleSvgPathBuilder;

public class SelectFreeTool extends PaintTool {

    private SVGPath icon;
    private Polygon area;

	public Polygon getArea() {
		if (area == null) {
			area = new Polygon();
			area.setFill(Color.TRANSPARENT);
            area.setStroke(Color.BLUE);
            area.setCursor(Cursor.MOVE);
            area.getStrokeDashArray().addAll(1D, 2D, 1D, 2D);
			area.setManaged(false);
		}
		return area;
	}

	@Override
    public SVGPath getIcon() {
		if (icon == null) {
            icon = new SimpleSvgPathBuilder()
                    .content("M 10,5 a 5,5 0 0 0 -3,5q 0,1 -2,2 a 4,3 1 1 0 7,0 c -3,-3 4,-4 0,-7z")
                    .stroke(Color.BLUE)
                    .strokeDashArray(1,2,1,2)
                    .fill(Color.TRANSPARENT)
                    .build();
		}
		return icon;
	}


	@Override
	public Cursor getMouseCursor() {
		return Cursor.DISAPPEAR;
	}

	@Override
	public void handleEvent(final MouseEvent e, final PaintModel model) {
		EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            onMouseReleased(model);
        }
		if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
			onMousePressed(e, model);
		}
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged(e, model);
        }
	}
	@Override
    protected void onMouseDragged(MouseEvent e, PaintModel model) {
        double x = getWithinRange(e.getX(), 0, model.getImage().getWidth());
        double y = getWithinRange(e.getY(), 0, model.getImage().getHeight());
        getArea().getPoints().addAll(x, y);
    }

    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getArea())) {
			children.add(getArea());
		}
        double x = getWithinRange(e.getX(), 0, model.getImage().getWidth());
        double y = getWithinRange(e.getY(), 0, model.getImage().getHeight());
        getArea().getPoints().clear();
		getArea().getPoints().addAll(x, y);
	}

	@Override
	protected void onMouseReleased(PaintModel model) {
        ObservableList<Node> children = model.getImageStack().getChildren();
        Bounds bounds = getArea().getBoundsInParent();
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();
        int minX = (int) bounds.getMinX();
        int minY = (int) bounds.getMinY();
        int endX = width + minX;
        int endY = height + minY;
        WritableImage selectedImage = createSelectedImage(model, minX, minY, width, height);
        SelectRectTool tool = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
        model.changeTool(tool);
        tool.setImageSelected(selectedImage);
        tool.selectArea(minX, minY, endX, endY, model);
        tool.getArea().setFill(new ImagePattern(selectedImage));
        if (children.contains(getArea())) {
            children.remove(getArea());
        }
    }

    private WritableImage createSelectedImage(PaintModel model, int minX, int minY, int width, int height) {
        PixelWriter currentImageWriter = model.getImage().getPixelWriter();
        PixelReader currentImageReader = model.getImage().getPixelReader();
        WritableImage selectedImage = new WritableImage(width, height);
        PixelWriter finalImage = selectedImage.getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int localX = i + minX;
                int localY = j + minY;
                if (withinRange(localX, localY, model)) {
                    if (getArea().contains(localX, localY)) {
                        finalImage.setColor(i, j, currentImageReader.getColor(localX, localY));
                        currentImageWriter.setColor(localX, localY, model.getBackColor());
                    } else {
                        finalImage.setColor(i, j, Color.TRANSPARENT);
                    }

                }
            }
        }
        return selectedImage;
    }





}