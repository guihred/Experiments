package furigana;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.stage.Stage;
import utils.TaskProgressView;

public class FuriganaCrawlerApp extends Application {

    public final Worker<String> worker = new CrawlerFuriganaTask();

	private TaskProgressView view = new TaskProgressView(worker);
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Furigana Converter");
        stage.setScene(view.getScene());
        stage.show();
    }


	public static void main(String[] args) {
        launch(args);
    }

}
