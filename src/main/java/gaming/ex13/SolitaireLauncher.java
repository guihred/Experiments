package gaming.ex13;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class SolitaireLauncher extends Application {


    @Override
    public void start(Stage stage) throws Exception {
		final Pane group = new Pane();
		final BorderPane borderPane = new BorderPane(group);
		borderPane.setStyle("-fx-background-color:green;");
        final Scene scene = new Scene(borderPane);
		SolitaireModel.create(group, scene);
        stage.setScene(scene);
		stage.setWidth(700);
		stage.setHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
