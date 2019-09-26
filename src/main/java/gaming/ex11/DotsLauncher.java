package gaming.ex11;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DotsLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        final BorderPane borderPane = new BorderPane();

        final Scene scene = new Scene(borderPane);
        DotsModel.createModel(borderPane);

        stage.setScene(scene);
		stage.setWidth(DotsHelper.MAZE_SIZE * DotsSquare.SQUARE_SIZE + 20.);
		stage.setHeight((DotsHelper.MAZE_SIZE + 1) * DotsSquare.SQUARE_SIZE + 20.);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
