package paintexp.tool;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import simplebuilder.SimpleRectangleBuilder;

public class SelectRectTool extends AreaTool {

	@Override
	public Node createIcon() {
		return new SimpleRectangleBuilder().width(30).height(30).fill(Color.TRANSPARENT).stroke(Color.BLUE)
				.strokeDashArray(1, 2, 1, 2).build();
	}

	@Override
	public Cursor getMouseCursor() {
		return Cursor.CROSSHAIR;
	}


}