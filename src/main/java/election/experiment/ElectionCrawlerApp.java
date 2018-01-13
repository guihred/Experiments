package election.experiment;

import fxproexercises.ch06.TaskProgressView;
import japstudy.db.HibernateUtil;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.stage.Stage;

public class ElectionCrawlerApp extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    public final Worker<String> worker = new CrawlerCompleteCandidateTask();
    private TaskProgressView view = new TaskProgressView(worker);


	@Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Worker and Task Example");
        stage.setScene(view.getScene());
        stage.show();
        stage.setOnCloseRequest(e -> HibernateUtil.shutdown());
    }

}
