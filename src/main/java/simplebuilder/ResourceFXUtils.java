package simplebuilder;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.shape.Mesh;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Note
 *
 *         Easy Methods to get a resource from the resources directory.
 *
 */
public final class ResourceFXUtils {

    private static final Logger LOGGER = HasLogging.log(ResourceFXUtils.class);

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
        try {
            String file = ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile();
            return new File(URLDecoder.decode(file, "UTF-8"));
        } catch (Exception e) {
            LOGGER.error("File Error:" + arquivo, e);
            return null;
        }
    }

    public static InputStream toStream(String arquivo) {
        return ResourceFXUtils.class.getClassLoader().getResourceAsStream(arquivo);
    }

	public static URI toURI(String arquivo) {
		return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile()).toURI();
	}

	public static Path toPath(String arquivo) {
		return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile()).toPath();
	}

    public static Mesh importStlMesh(String arquivo) {
        File file = new File(arquivo);
        return importStlMesh(file);
    }

    public static Mesh importStlMesh(File file) {
        //        StlMeshImporter importer = new StlMeshImporter();
        //        importer.read(file);
        //        return importer.getImport();
        return null;
    }

    public static Mesh importStlMesh(URL file) {
        //        StlMeshImporter importer = new StlMeshImporter();
        //        importer.read(file);
        //        return importer.getImport();
        return null;
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
