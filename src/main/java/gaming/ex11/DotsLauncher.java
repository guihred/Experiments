package gaming.ex11;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DotsLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        final Group gridPane = new Group();
        final BorderPane borderPane = new BorderPane(gridPane);

        final Scene scene = new Scene(borderPane);
        DotsModel mazeModel = new DotsModel(gridPane, borderPane);
        gridPane.getChildren().add(mazeModel.getLine());
        stage.setScene(scene);
        stage.setWidth(DotsModel.MAZE_SIZE * DotsSquare.SQUARE_SIZE + 20);
        stage.setHeight((DotsModel.MAZE_SIZE + 1) * DotsSquare.SQUARE_SIZE + 20);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
