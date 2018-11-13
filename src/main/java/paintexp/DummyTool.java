package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import utils.CommonsFX;

public class DummyTool extends PaintTool {
    
    private Circle circle;
    
    @Override
    public Node getIcon() {
        if (circle == null) {
            circle = new Circle(10, CommonsFX.generateRandomColors(50).get(0));
        }

        return circle;
    }
    
    @Override
    public Cursor getMouseCursor() {
        return Cursor.CROSSHAIR;
    }
    
}