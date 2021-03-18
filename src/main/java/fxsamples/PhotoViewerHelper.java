package fxsamples;

import static utils.ResourceFXUtils.convertToURL;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.Animation;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import simplebuilder.SimpleSequentialTransitionBuilder;
import utils.CommonsFX;
import utils.ex.RunnableEx;

public class PhotoViewerHelper {
	private static final int DURATION_MILLIS = 500;

    static void addImage(String url, List<String> imageFiles, AtomicInteger currentIndex) {
        if (isValidImageFile(url)) {
            currentIndex.getAndIncrement();
            imageFiles.add(currentIndex.get(), url);
        }
    }

    static void createButtonPanel(Scene scene, Parent buttonGroup, Rectangle buttonArea) {
        // create button panel
        buttonGroup.translateXProperty().bind(scene.widthProperty().subtract(buttonArea.getWidth() + 50));
        buttonGroup.translateYProperty().bind(scene.heightProperty().subtract(buttonArea.getHeight() + 6));
        scene.setOnMouseEntered(me -> new SimpleSequentialTransitionBuilder()
            .addFadeTransition(DURATION_MILLIS, buttonGroup, 0, 1).build().play());
        // Fade out button controls
        scene.setOnMouseExited(me -> new SimpleSequentialTransitionBuilder()
            .addFadeTransition(DURATION_MILLIS, buttonGroup, 1, 0).build().play());
    }

    static void createProgressIndicator(Scene scene, ProgressIndicator progressIndicator) {
        progressIndicator.setVisible(false);
        progressIndicator.layoutXProperty()
            .bind(scene.widthProperty().subtract(progressIndicator.widthProperty()).divide(2));

        progressIndicator.layoutYProperty()
            .bind(scene.heightProperty().subtract(progressIndicator.heightProperty()).divide(2));
    }

    static Group createTickerControl(Scene scene, double rightPadding, Group tickerArea, Rectangle tickerRect,
        Rectangle clipRegion, FlowPane tickerContent) {
        // create ticker area
        // Resize the ticker area when the window is resized
        tickerArea.setTranslateX(6);
        tickerArea.translateYProperty().bind(scene.heightProperty().subtract(tickerRect.getHeight() + 6));
        tickerRect.widthProperty().bind(scene.widthProperty().subtract(rightPadding));
        clipRegion.widthProperty().bind(scene.widthProperty().subtract(rightPadding));
        // news feed container
        // add some news
        tickerContent.translateYProperty()
            .bind(clipRegion.heightProperty().divide(2).subtract(tickerContent.heightProperty().divide(2)));
        final int factor = 40;
        // start ticker after nodes are shown
        double width = 500;
        new SimpleSequentialTransitionBuilder().addTranslateTransition(tickerContent, width * factor,
            scene.widthProperty(), tickerContent.widthProperty().negate()).cycleCount(Animation.INDEFINITE).build()
            .play();
        return tickerArea;
    }

    static int goToImageIndex(ButtonMove direction, List<String> imageFiles, AtomicInteger currentIndex) {
        int size = imageFiles.size();
        if (size == 0) {
            return -1;
        } else if (direction == ButtonMove.NEXT) {
            return (currentIndex.get() + 1) % size;

        } else if (direction == ButtonMove.PREV) {
            return (currentIndex.get() - 1 + size) % size;
        }
        return currentIndex.get();
    }

    static void loadImage(String url, AtomicBoolean loading1, ProgressIndicator progressIndicator1, Text news1,
        ImageView currentImageView2) {
        if (!loading1.getAndSet(true)) {
            final String url1 = url;
            Task<Boolean> loadImage = newWorker(url1, progressIndicator1, currentImageView2, loading1, news1);
            progressIndicator1.setVisible(true);
            progressIndicator1.progressProperty().unbind();
            progressIndicator1.progressProperty().bind(loadImage.progressProperty());
            new Thread(loadImage).start();
        }
    }

    static void tryAddImage(File file, List<String> imageFiles, AtomicInteger currentIndex) {
        RunnableEx.run(() -> addImage(convertToURL(file).toString(), imageFiles, currentIndex));
    }

    private static boolean isValidImageFile(String url) {
        List<String> imgTypes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
        return imgTypes.stream().anyMatch(url::endsWith);
    }

    private static Task<Boolean> newWorker(String url, ProgressIndicator progressIndicator2, ImageView currentImageView2,
        AtomicBoolean loading2, Text news2) {
        return new Task<Boolean>() {
            @Override
			public Boolean call() {
                Image image = new Image(url, false);
                CommonsFX.runInPlatform(() -> {
                    // New code:
                    transitionByFading(image, currentImageView2);
                    progressIndicator2.setVisible(false);
                    loading2.set(false); // free lock
                    news2.setText(url.replaceAll("^.+\\/", ""));
                });
                return true;
            }
        };
    }

    private static void transitionByFading(Image nextImage, ImageView imageView) {
        new SimpleSequentialTransitionBuilder()
            // fade out image view node
            .addFadeTransition(DURATION_MILLIS, imageView, 1, 0, e -> imageView.setImage(nextImage))
            // fade out image view, swap image and fade in image view
            .addFadeTransition(DURATION_MILLIS, imageView, 0, 1).build().play();
    }

    enum ButtonMove {
        NEXT,
        PREV
    }

}
