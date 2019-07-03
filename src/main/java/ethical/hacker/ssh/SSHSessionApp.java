package ethical.hacker.ssh;
import javafx.application.Application;
import javafx.stage.Stage;

public class SSHSessionApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("SSH Session APP");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
