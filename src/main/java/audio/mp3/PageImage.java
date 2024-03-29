package audio.mp3;

import extract.web.ImageLoader;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PageImage extends Application {
    @Override
	public void start(Stage primaryStage) {
        FlowPane root = new FlowPane();
        root.setAlignment(Pos.CENTER);
        ScrollPane scrollPane = new ScrollPane(root);
        root.prefWidthProperty().bind(scrollPane.widthProperty());
        TextField textField = new TextField();
        textField.textProperty().addListener((ob, t, value) -> ImageLoader.loadImages(root.getChildren(), value));
        Scene scene = new Scene(new VBox(textField, scrollPane));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        textField.prefWidthProperty().bind(scene.widthProperty().multiply(9. / 10));
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    public static void main(String[] args) {
        launch(args);
    }
}