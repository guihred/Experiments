package paintexp.svgcreator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SVGCreator extends Application {

    @Override
    public void start(final Stage stage) throws Exception {
        SVGModel svgModel = new SVGModel();
        Scene scene = new Scene(svgModel.createBorderPane());
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) {
                svgModel.undo();
            }
        });
        stage.setTitle("SVG Creator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
