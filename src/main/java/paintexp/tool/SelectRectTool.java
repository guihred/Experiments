package paintexp.tool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;
import utils.HasLogging;

public class SelectRectTool extends PaintTool {

	private static final Logger LOG = HasLogging.log();
	private Rectangle icon;
	private Rectangle area;
	protected WritableImage imageSelected;
	private double initialX;
	private double initialY;
	private double dragX;
	private double dragY;


	public void copyFromClipboard(final PaintModel model) {
		Clipboard systemClipboard = Clipboard.getSystemClipboard();
		Image image = systemClipboard.getImage();
		if (image != null) {
			copyImage(model, image, imageSelected);
		} else if (systemClipboard.getFiles() != null) {
			copyFromFile(model, systemClipboard.getFiles());
        }
        if (imageSelected != null) {
            getArea().setFill(new ImagePattern(imageSelected));
        }
	}

	public void copyToClipboard(final PaintModel model) {
        if (imageSelected == null) {
            double width = area.getWidth();
            double height = area.getHeight();
            imageSelected = new WritableImage((int) width, (int) height);
            int layoutX = (int) area.getLayoutX();
            int layoutY = (int) area.getLayoutY();
            int maxWidth = (int) model.getImage().getWidth();
            int maxHeight = (int) model.getImage().getHeight();
            copyImagePart(model.getImage(), imageSelected, Integer.min(Integer.max(layoutX, 0), maxWidth),
                    Integer.min(Integer.max(layoutY, 0), maxHeight), width, height);
        }
        Map<DataFormat, Object> content = new HashMap<>();
		content.put(DataFormat.IMAGE, imageSelected);
		Clipboard.getSystemClipboard().setContent(content);
	}

	public WritableImage createSelectedImage(final PaintModel model) {
		if (imageSelected == null) {
            int width = (int) area.getWidth();
            int height = (int) area.getHeight();
            imageSelected = new WritableImage(width, height);
            int layoutX = (int) area.getLayoutX();
            int layoutY = (int) area.getLayoutY();
            copyImagePart(model.getImage(), imageSelected, layoutX, layoutY, width, height);
            replaceColor(imageSelected, model.getBackColor(), Color.TRANSPARENT.invert());
            getArea().setFill(new ImagePattern(imageSelected));
            drawRect(model, layoutX, layoutY, width, height);
		}
		return imageSelected;
	}

    public Rectangle getArea() {
		if (area == null) {
			area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.cursor(Cursor.MOVE)
					.strokeDashArray(1, 2, 1, 2).build();
		}
		return area;
	}

	@Override
	public Node getIcon() {
		if (icon == null) {
			icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT).stroke(Color.BLACK)
					.strokeDashArray(1, 2, 1, 2).build();
		}
		return icon;
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.CROSSHAIR;
	}


    @Override
    public void handleEvent(MouseEvent e, PaintModel model) {
        EventType<? extends MouseEvent> eventType = e.getEventType();
        if (MouseEvent.MOUSE_PRESSED.equals(eventType)) {
            onMousePressed(e, model);
        }
        
        if (MouseEvent.MOUSE_DRAGGED.equals(eventType)) {
            onMouseDragged(e, model);
        }
        
        if (MouseEvent.MOUSE_RELEASED.equals(eventType)) {
            onMouseReleased(model);
        }
    }

    @Override
    public void handleKeyEvent(final KeyEvent e, final PaintModel model) {
        KeyCode code = e.getCode();
        Bounds bounds = getArea().getBoundsInParent();
        switch (code) {
            case DELETE:
                drawRect(model, bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
                break;
            case V:
                if (e.isControlDown()) {
                    copyFromClipboard(model);
                }
                break;
            case C:
                if (e.isControlDown()) {
                    copyToClipboard(model);
                }
                break;
            case X:
                if (e.isControlDown()) {
                    copyToClipboard(model);
                    drawRect(model, bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
                    model.createImageVersion();
                }
                break;
            case A:
                if (e.isControlDown()) {
                    selectArea(0, 0, model.getImage().getWidth(), model.getImage().getHeight(), model);
                }
                break;
            case ESCAPE:
                escapeArea(model);
                break;
            default:
                break;
        }
    }
    public void selectArea(final int x, final int y, final double w, final double h, final PaintModel model) {
		initialX = x;
		initialY = y;
		addRect(model);
		dragTo(w, h);
		onMouseReleased(model);
	}

	public void setImageSelected(WritableImage imageSelected) {
        this.imageSelected = imageSelected;
    }

	protected void addRect(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (!children.contains(getArea())) {
			children.add(getArea());
		}
		area.setStroke(Color.BLACK);
		getArea().setManaged(false);
		getArea().setFill(Color.TRANSPARENT);
		getArea().setLayoutX(initialX);
		getArea().setLayoutY(initialY);
		getArea().setWidth(1);
		getArea().setHeight(1);
	}

    protected void copyImage(final PaintModel model, final Image srcImage, final WritableImage destImage) {
        double width = srcImage.getWidth();
        double height = srcImage.getHeight();
        if (destImage == null) {
            imageSelected = new WritableImage((int) width, (int) height);
        }
        WritableImage writableImage = destImage != null ? destImage : imageSelected;
        copyImagePart(srcImage, writableImage, 0, 0, width, height, 0, 0, model.getBackColor());
        replaceColor(writableImage, model.getBackColor(), Color.TRANSPARENT);

        selectArea(0, 0, srcImage.getWidth(), srcImage.getHeight(), model);
    }

    @Override
    protected  void onMouseDragged(final MouseEvent e, final PaintModel model) {
		double x = e.getX();
		double y = e.getY();
		double width = model.getImage().getWidth();
		double height = model.getImage().getHeight();
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (children.contains(getArea()) && imageSelected != null) {
			getArea().setLayoutX(Double.max(x - dragX, -width / 4));
			getArea().setLayoutY(Double.max(y - dragY, -height / 4));
			return;
		}
        dragTo(setWithinRange(x, 0, width), setWithinRange(y, 0, height));
	}

    @Override
    protected  void onMousePressed(final MouseEvent e, final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();

        if (children.contains(getArea())) {
            if (containsPoint(getArea(), e.getX(), e.getY())) {
				createSelectedImage(model);
				dragX = e.getX() - getArea().getLayoutX();
				dragY = e.getY() - getArea().getLayoutY();
                return;
            }
            if (imageSelected != null) {
                setIntoImage(model);
            }
        }
        initialX = e.getX();
        initialY = e.getY();
        addRect(model);
	}

	@Override
    protected  void onMouseReleased(final PaintModel model) {
		ObservableList<Node> children = model.getImageStack().getChildren();
		if (getArea().getWidth() < 2 && children.contains(getArea()) && imageSelected != null) {

			children.remove(getArea());
		}
		area.setStroke(Color.BLUE);
	}

	protected void setIntoImage(final PaintModel model) {
		int x = (int) getArea().getLayoutX();
		int y = (int) getArea().getLayoutY();
		double width = getArea().getWidth();
		double height = getArea().getHeight();
        copyImagePart(imageSelected, model.getImage(), 0, 0, width, height, x, y, Color.TRANSPARENT);
		imageSelected = null;
		model.getImageStack().getChildren().remove(getArea());
        model.createImageVersion();
	}

    private void copyFromFile(final PaintModel model, final List<File> files) {
		if (!files.isEmpty()) {
			File file = files.get(0);
			try {
				Image image2 = new Image(file.toURI().toURL().toExternalForm());
				copyImage(model, image2, imageSelected);
			} catch (Exception e1) {
				LOG.error("", e1);
			}
		}
	}

	private void dragTo(final double x, final double y) {
		getArea().setLayoutX(Double.min(x, initialX));
		getArea().setLayoutY(Double.min(y, initialY));
		getArea().setWidth(Math.abs(x - initialX));
		getArea().setHeight(Math.abs(y - initialY));
	}

	private void escapeArea(final PaintModel model) {
        if (imageSelected != null) {
        	setIntoImage(model);
        }
        if (model.getImageStack().getChildren().contains(getArea())) {
        	model.getImageStack().getChildren().remove(getArea());
        }
    }

	private void replaceColor(WritableImage writableImage, Color backColor, Color transparent) {
        PixelReader pixelReader = writableImage.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        for (int i = 0; i < writableImage.getWidth(); i++) {
            for (int j = 0; j < writableImage.getHeight(); j++) {
                if(pixelReader.getColor(i, j).equals(backColor)) {
                    pixelWriter.setColor(i, j, transparent);
                }
            }
        }
        
        
    }

}