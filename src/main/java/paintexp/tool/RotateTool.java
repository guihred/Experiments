package paintexp.tool;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import simplebuilder.SimpleSvgPathBuilder;

public class RotateTool extends SelectRectTool {

	@Override
	public Node getIcon() {
		return new SimpleSvgPathBuilder()
				.fill(Color.BLACK)
				.stroke(Color.BLACK)
				.content("M5,0 l 0,1 a5,5 -5 1,0 5,5 l-1,0  a4,4 5 1,1 -4,-4 l 0,1 1.5,-1.5 z")
				.build();
	}

}

