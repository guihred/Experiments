package fxsamples;

import static fxsamples.PhotoViewerHelper.*;

import fxsamples.PhotoViewerHelper.ButtonMove;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class PhotoViewer extends Application {
    private static final Logger LOG = HasLogging.log();
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Rectangle buttonArea;
    @FXML
    private ImageView currentImageView;
    @FXML
    private Text news;
    @FXML
    private Group buttonGroup;
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
            setupDragNDrop(scene, imageFiles, currentIndex);
            createButtonPanel(scene, buttonGroup, buttonArea);
            createProgressIndicator(scene, progressIndicator);
            final int rightPadding = 80;
            createTickerControl(scene, rightPadding, tickerArea, tickerRect, clipRegion, tickerContent);
            scene.getStylesheets().add(ResourceFXUtils.toExternalForm("photo-viewer.css"));
        });
        addPicturesImageFolder(currentIndex, imageFiles);
    }

    public void onMousePressedButton1() {
        LOG.info("busy loading? {}", loading.get());
        // if no previous image or currently loading.
        if (loading.get()) {
            return;
        }
        currentIndex.set(goToImageIndex(ButtonMove.PREV, imageFiles, currentIndex));
        if (currentIndex.get() > -1) {
            loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
        }
    }

    public void onMousePressedButton2() {
        LOG.info("busy loading? {}", loading.get());
        // if no next image or currently loading.
        if (loading.get()) {
            return;
        }
        currentIndex.set(goToImageIndex(ButtonMove.NEXT, imageFiles, currentIndex));
        if (currentIndex.get() > -1) {
            loadImage(imageFiles.get(currentIndex.get()), loading, progressIndicator, news, currentImageView);
        }
    }

    @Override
	public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Photo Viewer", "PhotoViewer.fxml", this, primaryStage, 500, 500);
    }

    private void addPicturesImageFolder(AtomicInteger currentIndex2, List<String> imageFiles2) {
        ResourceFXUtils.runOnFiles(ResourceFXUtils.getUserFolder("Pictures"),
            t -> tryAddImage(t, imageFiles2, currentIndex2));
        if (currentIndex2.get() > -1) {
            loadImage(imageFiles2.get(currentIndex2.get()), loading, progressIndicator, news, currentImageView);
        }
    }

    private void setupDragNDrop(Scene scene, List<String> imageFiles2, AtomicInteger currentIndex2) {
        CommonsFX.initSceneDragAndDrop(scene, url -> {
            addImage(url, imageFiles2, currentIndex2);
            if (currentIndex2.get() > -1) {
                loadImage(imageFiles2.get(currentIndex2.get()), loading, progressIndicator, news, currentImageView);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }


}
