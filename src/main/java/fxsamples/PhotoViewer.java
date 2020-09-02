package fxsamples;

import static fxsamples.PhotoViewerHelper.*;

import fxsamples.PhotoViewerHelper.ButtonMove;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.FileChooserBuilder;
import utils.CommonsFX;
import utils.ResourceFXUtils;
import utils.RotateUtils;

public class PhotoViewer extends Application {
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Rectangle buttonArea;
    @FXML
    private ImageView currentImageView;
    @FXML
    private Text news;
    @FXML
    private Parent buttonGroup;
    @FXML
    private Group tickerArea;
    @FXML
    private FlowPane tickerContent;
    @FXML
    private Rectangle clipRegion;
    @FXML
    private Rectangle tickerRect;

    private final List<String> imageFiles = new ArrayList<>();
    private AtomicInteger currentIndex = new AtomicInteger(-1);
    private AtomicBoolean loading = new AtomicBoolean();

    public void initialize() {
        progressIndicator.sceneProperty().addListener(e -> {
            Scene scene = progressIndicator.getScene();
            // Dragging over surface
            setupDragNDrop(scene);
            createButtonPanel(scene, buttonGroup, buttonArea);
            createProgressIndicator(scene, progressIndicator);
            final int rightPadding = 80;
            createTickerControl(scene, rightPadding, tickerArea, tickerRect, clipRegion, tickerContent);
            CommonsFX.addCSS(scene, "photo-viewer.css");
        });
        addPicturesImageFolder();
    }

    public void onAddFolder(ActionEvent event) {
        new FileChooserBuilder().onSelect(f -> {
            imageFiles.clear();
            ResourceFXUtils.runOnFiles(f, t -> tryAddImage(t, imageFiles, currentIndex));
            loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
        }).openDirectoryAction(event);
    }

    public void onMousePressedButton1() {
        runIfNotLoading(() -> {
            currentIndex.set(goToImageIndex(ButtonMove.PREV, imageFiles, currentIndex));
            loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
        });
    }

    public void onMousePressedButton2() {
        runIfNotLoading(() -> {
            currentIndex.set(goToImageIndex(ButtonMove.NEXT, imageFiles, currentIndex));
            loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Photo Viewer", "PhotoViewer.fxml", this, primaryStage, 500, 500);
    }

    private void addPicturesImageFolder() {
        ResourceFXUtils.runOnFiles(ResourceFXUtils.getUserFolder("Pictures"),
                t -> tryAddImage(t, imageFiles, currentIndex));
        loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
    }

    private void runIfNotLoading(Runnable run) {
        if (loading.get()) {
            return;
        }
        run.run();
    }

    private void setupDragNDrop(Scene scene) {
        RotateUtils.initSceneDragAndDrop(scene, url -> {
            addImage(url, imageFiles, currentIndex);
            loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}
