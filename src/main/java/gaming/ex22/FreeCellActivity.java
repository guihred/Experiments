package gaming.ex22;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simplebuilder.SimpleButtonBuilder;

public class FreeCellActivity extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
		FreeCellView freeCellView = new FreeCellView();
        BorderPane root = new BorderPane(freeCellView);
        root.setBottom(SimpleButtonBuilder.newButton("Back", e -> freeCellView.getBackInHistory()));
        primaryStage.setScene(new Scene(root));
		primaryStage.show();
    }

	public static void main(String[] args) {
		launch(args);
	}

}
