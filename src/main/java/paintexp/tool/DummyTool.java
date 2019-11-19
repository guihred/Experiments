package paintexp.tool;

import javafx.scene.Node;
import javafx.scene.shape.Circle;
import utils.CommonsFX;

public class DummyTool extends PaintTool {
    @Override
    public Node createIcon() {
        return new Circle(10, CommonsFX.generateRandomColors(50).get(0));
    }
}