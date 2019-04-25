package labyrinth;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.geometry.Bounds;
import javafx.scene.PerspectiveCamera;
import javafx.scene.shape.Sphere;
import utils.CommonsFX;

public interface CommomLabyrinth {
    default boolean checkColision(Bounds boundsInParent) {
        Stream<Bounds> walls = getLabyrinthWalls().stream().parallel().map(LabyrinthWall::getBoundsInParent);
        return walls.anyMatch(b -> b.intersects(boundsInParent));
    }

    default void displayEndOfGame(Runnable run) {
        CommonsFX.displayDialog("Você Morreu", "Ok.", () -> {
            getCamera().setTranslateX(0);
            getCamera().setTranslateY(0);
            getCamera().setTranslateZ(0);
            run.run();
        });
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

}
