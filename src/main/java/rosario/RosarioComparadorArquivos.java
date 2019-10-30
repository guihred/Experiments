package rosario;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.CommonsFX;

public class RosarioComparadorArquivos extends Application {

    @Override
	public void start(Stage primaryStage) {
		CommonsFX.loadFXML("Comparação Estoque e ANVISA", "RosarioComparadorArquivos.fxml", primaryStage, 1000, 500);
    }


    public static void main(String[] args) {
        launch(args);
    }

}
