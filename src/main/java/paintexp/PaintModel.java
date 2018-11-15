package paintexp;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import paintexp.tool.PaintTool;

public class PaintModel {
    private ObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.WHITE);
    private ObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.BLACK);
    private WritableImage image = new WritableImage(500, 500);
    private StackPane imageStack;
    private ObjectProperty<PaintTool> tool = new SimpleObjectProperty<>();
    private Text imageSize = new Text();
    private Text toolSize = new Text();
    private Text mousePosition = new Text();

    public ObjectProperty<Color> backColorProperty() {
        return backColor;
    }

    public ObjectProperty<Color> frontColorProperty() {
        return frontColor;
    }

    public Color getBackColor() {
        return backColor.get();
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
            imageStack.setMinWidth(200);
            imageStack.setMinHeight(200);
        }

        return imageStack;
    }
    public Text getMousePosition() {
        return mousePosition;
    }

    public ObjectProperty<PaintTool> getTool() {
        return tool;
    }

    public Text getToolSize() {
        return toolSize;
    }
    public void setBackColor(Color backColor) {
        this.backColor.set(backColor);
    }

    public void setFrontColor(Color frontColor) {
        this.frontColor.set(frontColor);
    }
    public void setImage(WritableImage image) {
        this.image = image;
    }

    public void setImageSize(Text imageSize) {
        this.imageSize = imageSize;
    }

    public void setImageStack(StackPane imageStack) {
        this.imageStack = imageStack;
    }
    public void setMousePosition(Text mousePosition) {
        this.mousePosition = mousePosition;
    }

    public void setTool(ObjectProperty<PaintTool> tool) {
        this.tool = tool;
    }

    public void setToolSize(Text toolSize) {
        this.toolSize = toolSize;
    }

}
