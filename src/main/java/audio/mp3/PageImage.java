package audio.mp3;

import extract.ImageLoader;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class PageImage extends Application {
    @Override
	public void start(Stage primaryStage) {
        FlowPane root = new FlowPane();
        root.setPrefWidth(200);
        root.setAlignment(Pos.TOP_LEFT);
        ScrollPane scrollPane = new ScrollPane(root);
        TextField textField = new TextField();
        textField.textProperty().addListener((ob, t, value) -> ImageLoader.loadImages(root.getChildren(), value));
        root.getChildren().add(textField);
        textField.setText("Dog");
        Scene scene = new Scene(scrollPane);
        textField.prefWidthProperty().bind(scene.widthProperty().multiply(9. / 10));
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }
}