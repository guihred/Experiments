package rosario;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.CommonsFX;

public class RosarioComparadorArquivos extends Application {

    @Override
	public void start(Stage primaryStage) {
		int i = 500;
        CommonsFX.loadFXML("Comparação Estoque e ANVISA", "RosarioComparadorArquivos.fxml", primaryStage, i * 2, i);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
