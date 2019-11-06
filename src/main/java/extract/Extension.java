package extract;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.scene.image.Image;
import utils.ResourceFXUtils;

public enum Extension {
    FILE("file.png"),
    FOLDER("folder.png"),
    NET("netfol.png", ".html"),
    PICTURES("pictures.png", ".png", ".jpg", ".ico"),
    RINGTONES("ringtones.png", "mp3", "wma", "mp4"),
    SETTINGS("settings.png", "xml");

    private final List<String> extensions;
    private final Image image;

    Extension(String file, String... extensions) {
        this.extensions = Arrays.asList(extensions);
        image = new Image(ResourceFXUtils.toExternalForm("extensions/" + file));
    }

    public Image getFile() {
        return image;
    }

    public static Extension getExtension(File f) {
        if (f == null) {
            return FILE;
        }
        if (f.isDirectory()) {
            return FOLDER;
        }
        for (Extension ex : values()) {
            if (ex.extensions.stream().anyMatch(s -> f.getName().endsWith(s))) {
                return ex;
            }
        }
        return FILE;
    }
}