package extract;

import static utils.RunnableEx.ignore;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;

public class ImageLoader {
    private static final Logger LOG = HasLogging.log();
    private Thread thread;
    private String text;

    private void addImages(ObservableList<Node> children, String text1) {
        text = text1;
        ObservableList<String> images = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        images.addListener((Change<? extends String> c) -> Platform.runLater(() -> addImages(children, text1, c)));
        Platform.runLater(() -> {
            children.clear();
            LOG.trace("CLEARING IMAGES");
        });
        WikiImagesUtils.getImagensForked(text1, images);
    }


    private void addImages(ObservableList<Node> children, String text1, Change<? extends String> c) {
        LOG.trace("ADD IMAGE {}", text1);
        while (c.next()) {
            addImageViews(children, text1, c.getAddedSubList());
        }
    }

    private void addImageViews(ObservableList<Node> children, String text1, List<? extends String> addedSubList) {
        ignore(() -> {
            int size = addedSubList.size();
            for (int j = 0; j < size; j++) {
                if (!text1.equals(text)) {
                    return;
                }
                addImageView(children, addedSubList, j);
            }
        });

    }

    private void addThread(ObservableList<Node> root, String value) {
        ignore(() -> {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        });
        thread = new Thread(() -> addImages(root, value));
        thread.start();
    }

    public static double byArea(Node e) {
        return e.getBoundsInLocal().getWidth() * e.getBoundsInLocal().getHeight();
    }


    public static ImageView convertToImage(String url) {
        return convertToImage("en.wikipedia.org", url);
    }

    public static ImageView convertToImage(String domain, String url) {
        if (url.startsWith("data:image/")) {
            BufferedImage image = WikiImagesUtils.decodeToImage(url);
            Image image2 = SwingFXUtils.toFXImage(image, null);
            ImageView imageView = new ImageView(image2);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("wiki");
            return imageView;
        }
        String addDomain = ExtractUtils.addDomain(domain, url);
        ImageView imageView = new ImageView(addDomain);
        imageView.getStyleClass().add("wiki");
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public static void loadImages(ObservableList<Node> root, String... value) {
        Set<String> keywords = Stream.of(value).filter(StringUtils::isNotBlank).map(String::trim)
            .flatMap(e -> Stream.of(e.split("\\s+-\\s+"))).collect(Collectors.toSet());
        ImageLoader imageLoader = new ImageLoader();
        for (String string : keywords) {
            imageLoader.addThread(root, string);
        }
    }

    private static void addImageView(ObservableList<Node> children, List<? extends String> addedSubList, int j) {
        ignore(() -> {
            String url = addedSubList.get(j);
            LOG.info("NEW IMAGE {}", url);
            ImageView imageView = ImageLoader.convertToImage(url);
            int i = getIndex(children, imageView);
            children.add(i, imageView);
        });
    }

    private static int getIndex(ObservableList<Node> children, ImageView imageView) {
        int i = 0;
        for (; i < children.size(); i++) {
            if (byArea(children.get(i)) < byArea(imageView)) {
                return i;
            }
        }
        return i;
    }

}
