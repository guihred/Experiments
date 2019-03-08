package utils;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import org.slf4j.Logger;

/**
 * @author Note
 *
 *         Easy Methods to get a resource from the resources directory.
 *
 */
public final class ResourceFXUtils {


    private static final Logger LOGGER = HasLogging.log();

    private ResourceFXUtils() {
    }

    public static double clamp(final double value, final double min, final double max) {
        if (Double.compare(value, min) < 0) {
            return min;
        }
        if (Double.compare(value, max) > 0) {
            return max;
        }
        return value;
    }

    public static Image createImage(final double size1, final float[][] noise) {
        int width = (int) size1;
        int height = (int) size1;

        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float value = noise[x][y];
                double gray = normalizeValue(value, -.5, .5, 0., 1.);
                gray = clamp(gray, 0, 1);
                Color color = Color.RED.interpolate(Color.YELLOW, gray);
                pw.setColor(x, y, color);
            }
        }
        return wr;
    }

    public static Path getFirstPathByExtension(final File dir, final String other) {
        return getPathByExtension(dir, other).stream().findFirst().orElse(null);
    }

    public static List<Path> getPathByExtension(final File dir, final String... other) {
        try (Stream<Path> walk = Files.walk(dir.toPath(), 20, FileVisitOption.FOLLOW_LINKS)) {
            return walk.filter(e -> Stream.of(other).anyMatch(ex -> e.toString().endsWith(ex)))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        return Collections.emptyList();
    }

    public static File getUserFolder(final String dir) {
        String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
        return new File(new File(path).getParentFile(), dir);
    }

    public static Mesh importStlMesh(final File file) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        return importer.getImport();
    }

    public static Mesh importStlMesh(final String arquivo) {
        File file = new File(arquivo);
        return importStlMesh(file);
    }

    public static Mesh importStlMesh(final URL file) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        return importer.getImport();
    }

    public static void initializeFX() {
        Platform.setImplicitExit(false);
        new JFXPanel().toString();
    }

    public static void runOnFiles(final File userFolder,final ConsumerEx<File> run) {
         
        try (Stream<Path> s = Files.list(userFolder.toPath())) {
            s.forEach(ConsumerEx.makeConsumer(e -> run.accept(e.toFile())));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static String take(final Canvas canvas) {
        return take(canvas, canvas.getWidth(), canvas.getHeight());

    }

    public static String take(final Canvas canvas, final double w, final double h) {
        try {
            final WritableImage writableImage = new WritableImage((int) w, (int) h);
            final WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), writableImage);
            File destination = File.createTempFile("snapshot", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "PNG", destination);
            Desktop.getDesktop().open(destination);
            return destination.getAbsolutePath();
        } catch (final IOException e) {
            HasLogging.log(1).error("ERROR ", e);
            return null;
        }
    }

    public static String toExternalForm(final String arquivo) {
        try {
			return ResourceFXUtils.class.getClassLoader().getResource(arquivo).toExternalForm();
		} catch (RuntimeException e) {
			Logger log = HasLogging.log(1);
			log.error("ERRO FILE \"" + arquivo + "\"", e);
			throw e;
		}
    }

    public static File toFile(final String arquivo) {
        return new File(toFullPath(arquivo));
    }

    public static String toFullPath(final String arquivo) {
        try {
            return URLDecoder.decode(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile(), "UTF-8");
        } catch (Exception e) {
            LOGGER.error("File Error:" + arquivo, e);
            return null;
        }
    }

    public static Path toPath(final String arquivo) {
        return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile()).toPath();
    }

    public static InputStream toStream(final String arquivo) {
        return ResourceFXUtils.class.getClassLoader().getResourceAsStream(arquivo);
    }


    public static URI toURI(final String arquivo) {
        return new File(ResourceFXUtils.class.getClassLoader().getResource(arquivo).getFile()).toURI();
    }

    public static URL toURL(final String arquivo) {
        return ResourceFXUtils.class.getClassLoader().getResource(arquivo);
    }

    private static double normalizeValue(final double value, final double min, final double max, final double newMin, final double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

}
