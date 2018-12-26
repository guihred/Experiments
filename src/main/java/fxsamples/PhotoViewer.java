package fxsamples;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleSequentialTransitionBuilder;
import utils.ClassReflectionUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class PhotoViewer extends Application {
    private static final int DURATION_MILLIS = 500;
    private static final Logger LOGGER = HasLogging.log();
    // List of URL strings
    private final List<String> imageFiles = new ArrayList<>();
    // The current index into the imageFile
    private int currentIndex = -1;

    // Current image view display
    private ImageView currentImageView;
    private Text news = new Text();
    // Loading progress indicator
    private ProgressIndicator progressIndicator;
    // mutex */
    private AtomicBoolean loading = new AtomicBoolean();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Photo Viewer");
        Group root = new Group();
        Scene scene = new Scene(root, 500, 500);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("photo-viewer.css"));
        primaryStage.setScene(scene);
        // set up the current image view area
        currentImageView = createImageView(scene.widthProperty());
        // set up drag & drop file abilities
        setupDragNDrop(scene);
        // create button panel controls (left & right arrows)
        Group buttonGroup = createButtonPanel(scene);

        // create a progress indicator
        progressIndicator = createProgressIndicator(scene);
        root.getChildren().addAll(currentImageView, buttonGroup, progressIndicator);
        final int rightPadding = 80;
        root.getChildren().add(createTickerControl(primaryStage, rightPadding));
        primaryStage.show();
        ClassReflectionUtils.displayCSSStyler(scene, "photo-viewer.css");
        addPicturesFromImagesFolder();
    }

    /**
    * Adds the URL string representation of the path to the image file. Based
    * on a URL the method will check if it matches supported image format.
    * 
    * @param url
    *            string representation of the path to the image file.
    */
    private void addImage(String url) {
        if (isValidImageFile(url)) {
            currentIndex += 1;
            imageFiles.add(currentIndex, url);
        }
    }

    private void addPicturesFromImagesFolder() {
        ResourceFXUtils.runOnFiles(ResourceFXUtils.getUserFolder("Pictures"), this::tryAddImage);
        if (currentIndex > -1) {
            loadImage(imageFiles.get(currentIndex));
        }
    }

    /*
     * Returns a custom created button panel containing left and right buttons
     * to see previous and next images.
     * 
     * @param scene The main application scene
     * 
     * @return Group A custom button panel with previous and next buttons
     */
    private Group createButtonPanel(Scene scene) {
        // create button panel
        Group buttonGroup = new Group();
        Rectangle buttonArea = new Rectangle(0, 0, 60, 30);
        buttonArea.getStyleClass().add("button-panel");
        // buttonArea.setP
        buttonGroup.getChildren().add(buttonArea);
        // left arrow button
        Button leftButton = new Button();
        leftButton.getStyleClass().add("left-arrow");
        // return to previous image
        leftButton.setOnMousePressed(e -> {
            LOGGER.info("busy loading? {}", loading.get());
            // if no previous image or currently loading.
            if (loading.get()) {
                return;
            }
            int indx = gotoImageIndex(ButtonMove.PREV);

            if (indx > -1) {
                loadImage(imageFiles.get(indx));
            }
        });
        // right arrow button
        Button rightButton = new Button();
        rightButton.getStyleClass().add("right-arrow");
        // advance to next image
        rightButton.setOnMousePressed(mouseEvent -> {
            LOGGER.info("busy loading? {}", loading.get());
            // if no next image or currently loading.
            if (loading.get()) {
                return;
            }
            int indx = gotoImageIndex(ButtonMove.NEXT);
            if (indx > -1) {
                loadImage(imageFiles.get(indx));
            }
        });
        // add buttons to button group
        buttonGroup.getChildren().addAll(leftButton, rightButton);
        // move button group when scene is resized
        buttonGroup.translateXProperty().bind(scene.widthProperty().subtract(buttonArea.getWidth() + 6));
        buttonGroup.translateYProperty().bind(scene.heightProperty().subtract(buttonArea.getHeight() + 6));

        scene.setOnMouseEntered(me -> new SimpleSequentialTransitionBuilder()
                .addFadeTransition(DURATION_MILLIS, buttonGroup, 0, 1).build().play());
        // Fade out button controls
        scene.setOnMouseExited(me -> new SimpleSequentialTransitionBuilder()
                .addFadeTransition(DURATION_MILLIS, buttonGroup, 1, 0).build().play());

        return buttonGroup;
    }

    /*
     * A factory function returning an ImageView instance to preserve the aspect
     * ratio and bind the instance to the width of the scene for resizing the
     * image.
     * 
     * @param widthProperty is the Scene's read only width property.
     * 
     * @return ImageView newly created image view for current display.
     */
    private ImageView createImageView(ReadOnlyDoubleProperty widthProperty) {
        // maintain aspect ratio
        ImageView imageView = new ImageView();
        // set aspect ratio
        imageView.setPreserveRatio(true);
        // resize based on the scene
        imageView.fitWidthProperty().bind(widthProperty);
        return imageView;
    }

    /*
     * Create a progress indicator control to be centered.
     * 
     * @param scene The primary application scene.
     * 
     * @return ProgressIndicator a new progress indicator centered.
     */
    private ProgressIndicator createProgressIndicator(Scene scene) {
        ProgressIndicator progress = new ProgressIndicator(0);
        progress.setVisible(false);
        progress.layoutXProperty().bind(scene.widthProperty().subtract(progress.widthProperty()).divide(2));

        progress.layoutYProperty().bind(scene.heightProperty().subtract(progress.heightProperty()).divide(2));
        return progress;
    }
    private Group createTickerControl(Stage stage, double rightPadding) {
        Scene scene = stage.getScene();
        // create ticker area
        Group tickerArea = new Group();
        Rectangle tickerRect = new Rectangle(scene.getWidth(), 30);
        tickerRect.getStyleClass().add("ticker-border");
        Rectangle clipRegion = new Rectangle(scene.getWidth(), 30);
        clipRegion.getStyleClass().add("ticker-clip-region");
        tickerArea.setClip(clipRegion);
        // Resize the ticker area when the window is resized
        tickerArea.setTranslateX(6);
        tickerArea.translateYProperty().bind(scene.heightProperty().subtract(tickerRect.getHeight() + 6));
        tickerRect.widthProperty().bind(scene.widthProperty().subtract(rightPadding));
        clipRegion.widthProperty().bind(scene.widthProperty().subtract(rightPadding));
        tickerArea.getChildren().add(tickerRect);
        // news feed container
        FlowPane tickerContent = new FlowPane();
        // add some news
        news.setText("Drag in some Images");
        tickerContent.getChildren().add(news);
        tickerContent.translateYProperty()
                .bind(clipRegion.heightProperty().divide(2).subtract(tickerContent.heightProperty().divide(2)));
        tickerArea.getChildren().add(tickerContent);// scroll news feed

        SequentialTransition build = new SimpleSequentialTransitionBuilder()
                .addTranslateTransition(tickerContent, scene.getWidth() * 40, scene.widthProperty(),
                        tickerContent.widthProperty().negate())
                .cycleCount(Animation.INDEFINITE)
                .build();

        // start ticker after nodes are shown
        stage.setOnShown(windowEvent -> build.play());
        return tickerArea;
    }

    /*
     * Returns a worker task (Task) which will off-load the image on a separate
     * thread when finished; the current image will be displayed on the JavaFX
     * application thread.
     * 
     * @param url string representation of the path to the image file.
     * 
     * @return
     */
    private Task<Boolean> createWorker(final String url) {
        return new Task<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Image image = new Image(url, false);
                Platform.runLater(() -> {
                    // New code:
                    transitionByFading(image, currentImageView);
                    progressIndicator.setVisible(false);
                    loading.set(false); // free lock
                    news.setText(url.replaceAll("^.+\\/", ""));
                });
                return true;
            }
        };
    }

    /**
     * Returns the next index in the list of files to go to next.
     *
     * @param direction
     *            PREV and NEXT to move backward or forward in the list of
     *            pictures.
     * @return int the index to the previous or next picture to be shown.
     */
    private int gotoImageIndex(ButtonMove direction) {
        int size = imageFiles.size();
        if (size == 0) {
            currentIndex = -1;
        } else if (direction == ButtonMove.NEXT) {
            currentIndex = (currentIndex + 1) % size;

        } else if (direction == ButtonMove.PREV) {
            currentIndex = (currentIndex - 1 + size) % size;
        }
        return currentIndex;
    }

    /*
     * This method does the following loads an image, updates a progress bar and
     * spawns a new thread. If another process is already loading the method
     * will return without loading.
     * 
     * @param url string representation of the path to the image file.
     */
    private void loadImage(String url) {
        if (!loading.getAndSet(true)) {
            Task<Boolean> loadImage = createWorker(url);
            progressIndicator.setVisible(true);
            progressIndicator.progressProperty().unbind();
            progressIndicator.progressProperty().bind(loadImage.progressProperty());
            new Thread(loadImage).start();
        }
    }

    /*
     * Sets up the drag and drop capability for files and URLs to be dragged and
     * dropped onto the scene. This will load the image into the current image
     * view area.
     * 
     * @param scene The primary application scene.
     */
    private void setupDragNDrop(Scene scene) {
        // Dragging over surface
        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles() || db.hasUrl() && isValidImageFile(db.getUrl())) {
                event.acceptTransferModes(TransferMode.LINK);
            } else {
                event.consume();
            }
        });
        // Dropping over surface
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            // image from the local file system.
            if (db.hasFiles()) {
                db.getFiles().stream().forEach(this::tryAddImage);
            } else {
                // image from some host
                addImage(db.getUrl());
            }
            if (currentIndex > -1) {
                loadImage(imageFiles.get(currentIndex));
            }
            event.setDropCompleted(true);
            event.consume();
        });
    }

    private void transitionByFading(Image nextImage, ImageView imageView) {

        new SimpleSequentialTransitionBuilder()
                // fade out image view node
                .addFadeTransition(DURATION_MILLIS, imageView, 1, 0, e -> imageView.setImage(nextImage))
                // fade out image view, swap image and fade in image view
                .addFadeTransition(DURATION_MILLIS, imageView, 0, 1).build().play();
    }

    private void tryAddImage(File file) {
        try {
            addImage(file.toURI().toURL().toString());
            LOGGER.trace("{}", imageFiles);
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /*
     * Returns true if URL's file extensions match jpg, jpeg, png and gif.
     * 
     * @param url standard URL path to image file.
     * 
     * @return boolean returns true if URL's extension matches jpg, jpeg, png
     * and gif.
     */
    private static boolean isValidImageFile(String url) {
        List<String> imgTypes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
        return imgTypes.stream().anyMatch(url::endsWith);
    }

    // Enumeration of next and previous button directions
    public enum ButtonMove {
        NEXT,
        PREV
    }
}