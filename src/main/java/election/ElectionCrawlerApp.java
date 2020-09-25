package election;

import static utils.CommonsFX.onCloseWindow;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import utils.HibernateUtil;
import utils.fx.TaskProgressView;

public class ElectionCrawlerApp extends Application {

    public final Task<String> worker;

    public ElectionCrawlerApp() {
        worker = new CrawlerCandidates2018Task();
    }

    public ElectionCrawlerApp(Task<String> worker) {
        this.worker = worker;
    }

    @Override
	public void start(Stage stage) {
        TaskProgressView view = new TaskProgressView(worker);
        stage.setTitle("Election Crawler");
        stage.setScene(view.getScene());
        onCloseWindow(stage, HibernateUtil::shutdown);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
