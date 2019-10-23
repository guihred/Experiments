package election;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.stage.Stage;
import utils.HibernateUtil;
import utils.TaskProgressView;

public class ElectionCrawlerApp extends Application {

    public final Worker<String> worker;

    public ElectionCrawlerApp() {
        worker = new CrawlerCandidates2018Task();
    }

    public ElectionCrawlerApp(Worker<String> worker) {
        this.worker = worker;
    }

    @Override
    public void start(Stage stage) throws Exception {
        TaskProgressView view = new TaskProgressView(worker);
        stage.setTitle("Election Crawler");
        stage.setScene(view.getScene());
        stage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
