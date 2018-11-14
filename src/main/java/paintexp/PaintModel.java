package paintexp;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class PaintModel {
    private Color backColor = Color.WHITE;
    private Color frontColor = Color.BLACK;
    private WritableImage image = new WritableImage(500, 500);
    private StackPane imageStack;
    private ObjectProperty<PaintTool> tool = new SimpleObjectProperty<>();
    private Text imageSize = new Text();
    private Text toolSize = new Text();
    private Text mousePosition = new Text();

    public Color getBackColor() {
        return backColor;
    }

    public WritableImage getImage() {
        return image;
    }
    public Text getImageSize() {
        return imageSize;
    }

    public StackPane getImageStack() {
        if (imageStack == null) {
            imageStack = new StackPane(new ImageView(getImage()));
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
        this.backColor = backColor;
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

    public Color getFrontColor() {
        return frontColor;
    }

    public void setFrontColor(Color frontColor) {
        this.frontColor = frontColor;
    }

}
