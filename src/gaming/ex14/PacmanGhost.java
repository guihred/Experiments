package gaming.ex14;

import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

public class PacmanGhost extends Region {


	private Circle arc = new Circle(12);

	public PacmanGhost(Color color) {
		setLayoutX(50);
		setLayoutY(50);
		arc.setFill(color);
		Polygon polygon = new Polygon();
		polygon.setFill(color);
		polygon.getPoints().addAll(-12d, 0d, -12d, 20d, -8d, 10d, -4d, 20d, 0d, 10d, 4d, 20d, 8d, 10d, 12d, 20d, 12d,
				0d);

		getChildren().add(arc);
		getChildren().add(polygon);

	}

}
