package pdfreader;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.CommonsFX;

public class PdfReader extends Application {

    @Override
    public void start(Stage primaryStage) {
        final int width = 500;
        CommonsFX.loadFXML("PDF Read Helper", "PdfReader.fxml", primaryStage, width, width);
    }
    public static void main(String[] args) {
        launch(args);
    }

}
