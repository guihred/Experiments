package rosario;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class RosarioComparadorArquivos extends Application implements HasLogging {

    static final Logger LOG = HasLogging.log();

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Comparação Estoque e ANVISA");
        URL file = ResourceFXUtils.toURL("RosarioComparadorArquivos.fxml");
        Parent content = FXMLLoader.load(file);
        Scene scene = new Scene(content, 1000, 500, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
