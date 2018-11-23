package paintexp;

import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import paintexp.tool.PaintTool;

public class PaintModel {
    private ObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.WHITE);
    private ObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.BLACK);
    private WritableImage image = new WritableImage(500, 500);
    private Group imageStack;
	private final ObjectProperty<PaintTool> tool = new SimpleObjectProperty<>();
    private Text imageSize = new Text();
    private Text toolSize = new Text();
    private Text mousePosition = new Text();
	private VBox toolOptions;
	private File currentFile;
    private Rectangle rectangleBorder;
    public ObjectProperty<Color> backColorProperty() {
        return backColor;
	}public ObjectProperty<Color> frontColorProperty() {
        return frontColor;
    }

    public Color getBackColor() {
        return backColor.get();
    }

    public File getCurrentFile() {
		return currentFile;
	}

    public Color getFrontColor() {
        return frontColor.get();
    }
    public WritableImage getImage() {
        return image;
    }

    public Text getImageSize() {
        return imageSize;
    }

    public Group getImageStack() {
        if (imageStack == null) {
            ImageView imageView = new ImageView(getImage());
            imageView.setLayoutX(0);
            imageView.setLayoutY(0);
            imageView.setManaged(false);
			imageView.setSmooth(false);

            imageStack = new Group(imageView);
			imageStack.setManaged(false);
			imageStack.setLayoutX(0);
			imageStack.setLayoutY(0);
        }

        return imageStack;
    }

    public Text getMousePosition() {
        return mousePosition;
    }

    public Rectangle getRectangleBorder(ImageView imageView) {
        if (rectangleBorder == null) {
            rectangleBorder = new Rectangle(10, 10, Color.TRANSPARENT);
            rectangleBorder.setStroke(Color.BLACK);
        }
        rectangleBorder.setManaged(false);
        rectangleBorder.layoutXProperty().bind(imageView.layoutXProperty());
        rectangleBorder.layoutYProperty().bind(imageView.layoutYProperty());
        rectangleBorder.widthProperty().bind(image.widthProperty().add(1));
        rectangleBorder.heightProperty().bind(image.heightProperty().add(1));
        return rectangleBorder;
    }

    public PaintTool getTool() {
		return tool.get();
    }

    public VBox getToolOptions() {
		if (toolOptions == null) {
            toolOptions = new VBox(10);
            toolOptions.setAlignment(Pos.CENTER);
			toolOptions.setId("tools");
            resetToolOptions();
		}
		return toolOptions;
	}

    public Text getToolSize() {
        return toolSize;
    }

    public Rectangle resetToolOptions() {
        Rectangle rectangle = new Rectangle(50, 50, Color.TRANSPARENT);
        rectangle.setStroke(Color.grayRgb(128));
		toolOptions.setMaxWidth(150);
        toolOptions.setSpacing(10);
        toolOptions.getChildren().clear();
        toolOptions.getChildren().add(rectangle);
        return rectangle;
    }
    public void setBackColor(final Color backColor) {
        this.backColor.set(backColor);
    }


    public void setCurrentFile(final File currentFile) {
		this.currentFile = currentFile;
	}
    public void setFrontColor(final Color frontColor) {
        this.frontColor.set(frontColor);
    }

	public void setImage(final WritableImage image) {
        this.image = image;
    }

    public void setImageSize(final Text imageSize) {
        this.imageSize = imageSize;
    }

	public void setMousePosition(final Text mousePosition) {
        this.mousePosition = mousePosition;
    }

	public void setTool(final PaintTool tool) {
		this.tool.set(tool);
	}

	public void setToolSize(final Text toolSize) {
		this.toolSize = toolSize;
	}

	public ObjectProperty<PaintTool> toolProperty() {
    	return tool;
	}

}
