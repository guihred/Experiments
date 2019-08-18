package audio.mp3;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
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
            Node node = children.get(0);
            children.clear();
            children.add(node);
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
        try {
            int size = addedSubList.size();
            for (int j = 0; j < size; j++) {
				if (!text1.equals(text)) {
                    return;
                }
                addImageView(children, addedSubList, j);
            }
        } catch (Exception e) {
            LOG.trace("ERROR {}", e);
        }
    }

	private void addThread(ObservableList<Node> root, String value) {
        try {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
        } catch (Exception e1) {
            LOG.trace("TRYING TO STOP", e1);
        }
        thread = new Thread(() -> addImages(root, value));
        thread.start();
    }

    public static void loadImages(ObservableList<Node> root, String... value) {
        Set<String> keywords = Stream.of(value).filter(StringUtils::isNotBlank).map(String::trim)
            .flatMap(e -> Stream.of(e.split("\\s+-\\s+")))
            .collect(Collectors.toSet());
        for (String string : keywords) {
            new ImageLoader().addThread(root, string);
        }
    }

    private static void addImageView(ObservableList<Node> children, List<? extends String> addedSubList, int j) {
        try {
            String url = addedSubList.get(j);
            LOG.trace("NEW IMAGE {}", url);
            ImageView imageView = WikiImagesUtils.convertToImage(url);
            int i = getIndex(children, imageView);
            children.add(i, imageView);
        } catch (Exception e) {
            LOG.trace("ERROR {}", e);
        }
    }

    private static double byArea(Node e) {
        return e.getBoundsInLocal().getWidth() * e.getBoundsInLocal().getHeight();
    }

    private static int getIndex(ObservableList<Node> children, ImageView imageView) {
        int i = 1;
        for (; i < children.size(); i++) {
            if (byArea(children.get(i)) < byArea(imageView)) {
                return i;
            }
        }
        return i;
    }

}
