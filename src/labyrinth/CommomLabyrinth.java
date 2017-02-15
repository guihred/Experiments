package labyrinth;

import java.util.Collection;
import java.util.stream.Stream;
import javafx.geometry.Bounds;
import javafx.scene.PerspectiveCamera;

public interface CommomLabyrinth {
	Collection<LabyrinthWall> getLabyrinthWalls();
	PerspectiveCamera getCamera();
	default void endKeyboard() {
	}
	default boolean checkColision(Bounds boundsInParent) {
		Stream<Bounds> walls = getLabyrinthWalls().stream().parallel()
				.map(LabyrinthWall::getBoundsInParent);
		return walls.anyMatch(b -> b.intersects(boundsInParent));
	}
}
