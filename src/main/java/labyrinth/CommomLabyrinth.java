package labyrinth;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.geometry.Bounds;
import javafx.scene.PerspectiveCamera;
import javafx.scene.shape.Sphere;

public interface CommomLabyrinth {
    default boolean checkColision(Bounds boundsInParent) {
        return checkColision(getLabyrinthWalls(), boundsInParent);
    }

    default void endKeyboard() {
        // DOES NOTHING
    }

    PerspectiveCamera getCamera();

    Collection<LabyrinthWall> getLabyrinthWalls();

    static Sphere checkBalls(Bounds boundsInParent, Sphere[][] balls2) {
        return Stream.of(balls2).flatMap(Stream::of).filter(Objects::nonNull)
            .filter(b -> b.getBoundsInParent().intersects(boundsInParent)).findFirst().orElse(null);
    }

    static boolean checkColision(Collection<LabyrinthWall> walls1, Bounds boundsInParent) {
        Stream<Bounds> walls = walls1.stream().parallel().map(LabyrinthWall::getBoundsInParent);
        return walls.anyMatch(b -> b.intersects(boundsInParent));
    }

}
