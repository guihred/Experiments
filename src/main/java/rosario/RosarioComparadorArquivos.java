package rosario;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class RosarioComparadorArquivos extends Application implements HasLogging {

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL file = ResourceFXUtils.toURL("RosarioComparadorArquivos.fxml");
        Parent content = FXMLLoader.load(file);
        Scene scene = new Scene(content, 1000, 500, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Comparação Estoque e ANVISA");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
