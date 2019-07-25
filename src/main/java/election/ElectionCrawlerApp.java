package election;

import japstudy.db.HibernateUtil;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.stage.Stage;
import utils.TaskProgressView;

public class ElectionCrawlerApp extends Application {

    public final Worker<String> worker;
    private TaskProgressView view;

    public ElectionCrawlerApp() {
        worker = new CrawlerCandidates2018Task();
        view = new TaskProgressView(worker);
    }

    public ElectionCrawlerApp(Worker<String> worker) {
        this.worker = worker;
        view = new TaskProgressView(worker);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Election Crawler");
        stage.setScene(view.getScene());
        stage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
