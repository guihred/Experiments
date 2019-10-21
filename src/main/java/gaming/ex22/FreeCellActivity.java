package gaming.ex22;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class FreeCellActivity extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
		FreeCellView freeCellView = new FreeCellView();

        primaryStage.setScene(new Scene(new BorderPane(freeCellView)));
		primaryStage.show();
    }

	public static void main(String[] args) {
		launch(args);
	}

}
