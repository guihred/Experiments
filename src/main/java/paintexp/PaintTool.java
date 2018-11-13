package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;

public abstract class PaintTool extends Group {
    public PaintTool() {
        getChildren().add(getIcon());
    }

    public abstract Node getIcon();

    public abstract Cursor getMouseCursor();
}