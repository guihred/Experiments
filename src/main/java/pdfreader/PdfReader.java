package pdfreader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.ResourceFXUtils;

public class PdfReader extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int width = 500;
        Parent root = FXMLLoader.load(ResourceFXUtils.toURL("PdfReader.fxml"));
        Scene scene = new Scene(root, width, width);
        primaryStage.setTitle("PDF Read Helper");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
