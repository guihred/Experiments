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
import simplebuilder.SimpleSvgPathBuilder;

public class SelectFreeTool extends SelectRectTool {

    private SVGPath icon;
    private Polygon area;

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


	public Polygon getPolygon() {
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
	public void handleEvent(final MouseEvent e, final PaintModel model) {
		if (imageSelected != null) {
			super.handleEvent(e, model);
            return;
		}
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            onMouseReleased2(model);
        }
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMousePressed2(e, model);
        }
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged2(e, model);
        }
	}

	protected void onMouseDragged2(final MouseEvent e, final PaintModel model) {
        double x = getWithinRange(e.getX(), 0, model.getImage().getWidth());
        double y = getWithinRange(e.getY(), 0, model.getImage().getHeight());
		getPolygon().getPoints().addAll(x, y);
    }

	protected void onMousePressed2(final MouseEvent e, final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getPolygon())) {
			children.add(getPolygon());
		}
        double x = getWithinRange(e.getX(), 0, model.getImage().getWidth());
        double y = getWithinRange(e.getY(), 0, model.getImage().getHeight());
		getPolygon().getPoints().clear();
		getPolygon().getPoints().addAll(x, y);
	}

	protected void onMouseReleased2(final PaintModel model) {
        if (!model.getImageStack().getChildren().contains(getPolygon()) || getPolygon().getPoints().size() <= 4) {
			model.getImageStack().getChildren().remove(getPolygon());
			return;
		}

		Bounds bounds = getPolygon().getBoundsInParent();
        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();
        int minX = (int) bounds.getMinX();
        int minY = (int) bounds.getMinY();
        int endX = width + minX;
        int endY = height + minY;
        WritableImage selectedImage = createSelectedImage(model, minX, minY, width, height);
		model.changeTool(this);
        setImageSelected(selectedImage);
        selectArea(minX, minY, endX, endY, model);
        getArea().setFill(new ImagePattern(selectedImage));
        ObservableList<Node> children = model.getImageStack().getChildren();
		if (children.contains(getPolygon())) {
			children.remove(getPolygon());
        }
    }

    @Override
    protected void setIntoImage(final PaintModel model) {
    	super.setIntoImage(model);
    	getPolygon().getPoints().clear();
		if (model.getImageStack().getChildren().contains(getArea())) {
			getArea().setWidth(0);
			getArea().setHeight(0);
			model.getImageStack().getChildren().remove(getArea());
		}
    }

	private WritableImage createSelectedImage(final PaintModel model, final int minX, final int minY, final int width, final int height) {
        WritableImage image = model.getImage();
		PixelWriter currentImageWriter = image.getPixelWriter();
        PixelReader currentImageReader = image.getPixelReader();
        WritableImage selectedImage = new WritableImage(width, height);
        PixelWriter finalImage = selectedImage.getPixelWriter();
        int backColor = PixelHelper.toArgb(model.getBackColor());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int localX = i + minX;
                int localY = j + minY;
                if (withinRange(localX, localY, model)) {
                    if (getPolygon().contains(localX, localY) && (option == SelectOption.OPAQUE
                            || currentImageReader.getArgb(localX, localY) != backColor)) {
                        finalImage.setArgb(i, j, currentImageReader.getArgb(localX, localY));
                        currentImageWriter.setArgb(localX, localY, backColor);
                    } else {
                        finalImage.setColor(i, j, Color.TRANSPARENT);
                    }

                }
            }
        }
        return selectedImage;
    }



}