package paintexp;

import java.io.File;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import paintexp.tool.PaintTool;
import paintexp.tool.SelectRectTool;

public class PaintModel {
    private static final int MAX_VERSIONS = 50;
    private final ObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.WHITE);
	private final ObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.BLACK);
    private WritableImage image = new WritableImage(500, 500);
    private Group imageStack;
	private final ObjectProperty<PaintTool> tool = new SimpleObjectProperty<>();
    private Text imageSize = new Text();
    private Text toolSize = new Text();
    private Text mousePosition = new Text();
	private VBox toolOptions;
	private File currentFile;
    private final ObservableList<WritableImage> imageVersions = FXCollections.observableArrayList();
    private Rectangle rectangleBorder;

    public ObjectProperty<Color> backColorProperty() {
        return backColor;
	}

    public void changeTool(final PaintTool newValue) {
        resetToolOptions();
        getImageStack().getChildren().clear();
        ImageView imageView = new ImageView(getImage());
        getImageStack().getChildren().add(getRectangleBorder(imageView));
        getImageStack().getChildren().add(imageView);
        if (newValue != null) {
            PaintTool oldTool = getTool();
            if (oldTool != null) {
                oldTool.onDeselected(this);
            }
            setTool(newValue);
            PaintTool paintTool = getTool();
            paintTool.onSelected(this);
        }

    }
    public void createImageVersion() {
        imageVersions
                .add(new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight()));
        if (imageVersions.size() > MAX_VERSIONS) {
			imageVersions.remove(0);
		}

    }

	public ObjectProperty<Color> frontColorProperty() {
        return frontColor;
    }

    public Color getBackColor() {
        return backColor.get();
    }

    public File getCurrentFile() {
		return currentFile;
	}

    public SelectRectTool getCurrentSelectTool() {
        return Stream.of(PaintTools.values()).map(PaintTools::getTool)
                .filter(SelectRectTool.class::isInstance).map(SelectRectTool.class::cast)
                .filter(e -> getImageStack().getChildren().contains(e.getArea()))
                .findFirst()
                .orElseGet(() -> getTool() instanceof SelectRectTool ? (SelectRectTool) getTool()
                        : (SelectRectTool) PaintTools.SELECT_RECT.getTool());
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
            imageView.setSmooth(true);

            imageStack = new Group(getRectangleBorder(imageView), imageView);
			imageStack.setManaged(false);
			imageStack.setLayoutX(0);
			imageStack.setLayoutY(0);
        }

        return imageStack;
    }
    public ObservableList<WritableImage> getImageVersions() {
        return imageVersions;
    }

    public Text getMousePosition() {
        return mousePosition;
    }

    public Rectangle getRectangleBorder(final ImageView imageView) {
        if (rectangleBorder == null) {
			rectangleBorder = new Rectangle(10, 10, new ImagePattern(ColorChooser.drawTransparentPattern(100)));
            rectangleBorder.setStroke(Color.BLACK);
        }
        rectangleBorder.setManaged(false);
        rectangleBorder.layoutXProperty().bind(imageView.layoutXProperty());
        rectangleBorder.layoutYProperty().bind(imageView.layoutYProperty());
        rectangleBorder.widthProperty().bind(image.widthProperty().add(1));
        rectangleBorder.heightProperty().bind(image.heightProperty().add(1));
        return rectangleBorder;
    }

    public WritableImage getSelectedImage() {
        SelectRectTool selectTool = getCurrentSelectTool();
        if (getImageStack().getChildren().contains(selectTool.getArea())) {
            return selectTool.createSelectedImage(this);
        }
        return getImage();
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
        rectangle.setStroke(Color.GRAY);
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
    public void setFinalImage( final WritableImage writableImage) {
            SelectRectTool selectTool = getCurrentSelectTool();
            if (getImageStack().getChildren().contains(selectTool.getArea())) {
                selectTool.getArea().setWidth(writableImage.getWidth());
                selectTool.getArea().setHeight(writableImage.getHeight());
                selectTool.getArea().setFill(new ImagePattern(writableImage));
                selectTool.setImageSelected(writableImage);
            } else {
                getImageStack().getChildren().clear();
                ImageView imageView = new ImageView(writableImage);
                setImage(writableImage);
                getImageStack().getChildren().add(getRectangleBorder(imageView));
                getImageStack().getChildren().add(imageView);
            }
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
