package paintexp;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public abstract class PaintTool extends Group {
    public PaintTool() {
        getChildren().add(getIcon());
    }

    public abstract Node getIcon();

    public abstract Cursor getMouseCursor();

	@SuppressWarnings("unused")
	public void handleEvent(MouseEvent e, WritableImage image, StackPane imageStack) {
		// DOES NOTHING
	}
}