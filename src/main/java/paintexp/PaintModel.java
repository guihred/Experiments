package paintexp;

import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import paintexp.tool.PaintTool;

public class PaintModel {
    private ObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.WHITE);
    private ObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.BLACK);
    private WritableImage image = new WritableImage(500, 500);
    private StackPane imageStack;
	private final ObjectProperty<PaintTool> tool = new SimpleObjectProperty<>();
    private Text imageSize = new Text();
    private Text toolSize = new Text();
    private Text mousePosition = new Text();
	private VBox toolOptions;
	private File currentFile;
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
    public StackPane getImageStack() {
        if (imageStack == null) {
            ImageView imageView = new ImageView(getImage());
            imageView.setLayoutX(0);
            imageView.setLayoutY(0);
            imageView.setManaged(false);
			imageView.setSmooth(false);
            imageStack = new StackPane(imageView);
            imageStack.setAlignment(Pos.TOP_LEFT);
			imageStack.setManaged(false);
			imageStack.setLayoutX(0);
			imageStack.setLayoutY(0);
            imageStack.setMinHeight(200);
        }

        return imageStack;
    }

	public Text getMousePosition() {
        return mousePosition;
    }

    public PaintTool getTool() {
		return tool.get();
    }
    public Node getToolOptions() {
		if (toolOptions == null) {
			Rectangle rectangle = new Rectangle(50, 50, Color.TRANSPARENT);
			rectangle.setStroke(Color.grayRgb(128));
			toolOptions = new VBox(10, rectangle);
			toolOptions.setAlignment(Pos.CENTER);
		}
		return toolOptions;
	}

    public Text getToolSize() {
        return toolSize;
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

	public void setImageStack(final StackPane imageStack) {
        this.imageStack = imageStack;
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
