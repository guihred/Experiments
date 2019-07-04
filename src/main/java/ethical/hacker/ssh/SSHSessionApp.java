package ethical.hacker.ssh;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.sshd.server.SshServer;
import utils.CommonsFX;

public class SSHSessionApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        HBox root = new HBox();
        root.getChildren().add(CommonsFX.newButton("", e -> {
            SshServer server = SSHClientUtils.setupTestServer();
        }));

        primaryStage.setTitle("SSH Session APP");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}
