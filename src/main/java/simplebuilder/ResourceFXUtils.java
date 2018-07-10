package simplebuilder;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.slf4j.LoggerFactory;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;

/**
 * @author Note
 *
 *         Easy Methods to get a resource from the resources directory.
 *
 */
public final class ResourceFXUtils {

	private ResourceFXUtils() {
	}

	public static URL toURL(String arquivo) {
		return ResourceFXUtils.class.getClassLoader().getResource(arquivo);
	}

	public static String toExternalForm(String arquivo) {
		return ResourceFXUtils.class.getClassLoader().getResource(arquivo).toExternalForm();
	}

	public static String toFullPath(String arquivo) {
		return ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile();
	}

	public static File toFile(String arquivo) {
		return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile());
	}

	public static URI toURI(String arquivo) {
		return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile()).toURI();
	}

	public static Path toPath(String arquivo) {
		return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile()).toPath();
	}

    public static String take(final Canvas canvas) {
        try {
            final WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            final WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), writableImage);
            File destination = File.createTempFile("snapshot", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "PNG", destination);
            Desktop.getDesktop().open(destination);
            return destination.getAbsolutePath();
        } catch (final IOException e) {
            LoggerFactory.getLogger(ResourceFXUtils.class).error("ERROR ", e);
            return null;
        }

    }

}
