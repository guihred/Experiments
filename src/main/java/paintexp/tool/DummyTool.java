package paintexp.tool;

import javafx.scene.Node;
import javafx.scene.shape.Circle;
import utils.ImageFXUtils;

public class DummyTool extends PaintTool {
    @Override
    public Node createIcon() {
        return new Circle(10, ImageFXUtils.generateRandomColors(50).get(0));
    }
}