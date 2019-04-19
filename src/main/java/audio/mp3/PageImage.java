package audio.mp3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;

public class PageImage extends Application {
    private static final Logger LOG = HasLogging.log();
    private Thread thread;
    private String text;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FlowPane root = new FlowPane();
        root.setPrefWidth(200);
        root.setAlignment(Pos.TOP_LEFT);
        ScrollPane scrollPane = new ScrollPane(root);
        TextField textField = new TextField();
        textField.textProperty().addListener((ob, t, value) -> addThread(root, value));
        root.getChildren().add(textField);
        textField.setText("Dog");
        Scene scene = new Scene(scrollPane);
        textField.prefWidthProperty().bind(scene.widthProperty().multiply(9. / 10));
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void addImages(Pane root, String text) {
        this.text = text;
        ObservableList<String> images = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        images.addListener((Change<? extends String> c) -> Platform.runLater(() -> addImages(root, text, c)));
        Platform.runLater(() -> {
            Node node = root.getChildren().get(0);
            root.getChildren().clear();
            root.getChildren().add(node);
            LOG.info("CLEARING IMAGES");
        });
        WikiImagesUtils.getImagensForked(text, images);
    }

    private void addImages(Pane root, String text, Change<? extends String> c) {
        LOG.info("ADD IMAGE {}", text);
        while (c.next()) {
            for (String url : c.getAddedSubList()) {
                if (!text.equals(this.text)) {
                    return;
                }
                LOG.info("NEW IMAGE {}", url);
                ObservableList<Node> children = root.getChildren();
                ImageView imageView = WikiImagesUtils.convertToImage(url);
                int i = getIndex(children, imageView);
                children.add(i, imageView);
            }
        }
    }

    private void addThread(Pane root, String value) {
        try {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        } catch (Exception e1) {
            LOG.info("TRYING TO STOP", e1);
        }
        thread = new Thread(() -> addImages(root, value));
        thread.start();
    }

    private double byArea(Node e) {
        return e.getBoundsInLocal().getWidth() * e.getBoundsInLocal().getHeight();
    }

    private int getIndex(ObservableList<Node> children, ImageView imageView) {
        int i = 1;
        for (; i < children.size() ; i++) {
            if(byArea(children.get(i)) < byArea(imageView)) {
                return i;
            }
        }
        return i;
    }

    public static void main(String[] args) {
        launch(args);
    }
}