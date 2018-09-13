package labyrinth;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public interface CommomLabyrinth {
	Collection<LabyrinthWall> getLabyrinthWalls();
	PerspectiveCamera getCamera();
	default void endKeyboard() {
		// DOES NOTHING
	}
	default boolean checkColision(Bounds boundsInParent) {
		Stream<Bounds> walls = getLabyrinthWalls().stream().parallel()
				.map(LabyrinthWall::getBoundsInParent);
		return walls.anyMatch(b -> b.intersects(boundsInParent));
	}

    static Sphere checkBalls(Bounds boundsInParent, Sphere[][] balls2) {
        return Stream.of(balls2).flatMap(Stream::of)
                .filter(Objects::nonNull)
                .filter(b -> b.getBoundsInParent().intersects(boundsInParent))
                .findFirst().orElse(null);
    }


	default void displayEndOfGame(Runnable run) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.WINDOW_MODAL);
		Button button = new Button("Ok.");
		button.setOnAction(e -> {
			getCamera().setTranslateX(0);
			getCamera().setTranslateY(0);
			getCamera().setTranslateZ(0);
			run.run();
			dialogStage.close();
		});
		VBox vbox = new VBox();
		vbox.getChildren().addAll(new Text("Você Morreu"), button);
		vbox.setAlignment(Pos.CENTER);
		vbox.setPadding(new Insets(5));
		dialogStage.setScene(new Scene(vbox));
		dialogStage.show();
	}

}
