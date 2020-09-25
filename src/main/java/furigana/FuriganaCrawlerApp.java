package furigana;

import static utils.CommonsFX.onCloseWindow;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import utils.fx.TaskProgressView;

public class FuriganaCrawlerApp extends Application {


    @Override
	public void start(Stage stage) {
        final Task<String> worker = new CrawlerFuriganaTask();
        TaskProgressView view = new TaskProgressView(worker);
        stage.setTitle("Furigana Converter");
        stage.setScene(view.getScene());
        onCloseWindow(stage, worker::cancel);
        stage.show();
    }


	public static void main(String[] args) {
        launch(args);
    }

}
