package utils;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Mesh;
import javax.swing.filechooser.FileSystemView;
import org.assertj.core.api.exception.RuntimeIOException;
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


	public static URL convertToURL(File arquivo) {
	    return SupplierEx.get(()->arquivo.toURI().toURL());
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

    public static File getOutFile() {
		File parentFile = toFile("alice.txt").getParentFile();
		File file = new File(parentFile, "out");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getOutFile(String out) {
        File parentFile = toFile("alice.txt").getParentFile();
        File file = new File(parentFile, "out");
        if (!file.exists()) {
            file.mkdir();
        }
        return new File(file, out);
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

	public static int getYearCreation(Path path ) {
        try {
            BasicFileAttributes readAttributes = Files.readAttributes(path, BasicFileAttributes.class);
            Instant l = readAttributes.creationTime().toInstant();
            return ZonedDateTime.ofInstant(l, ZoneId.systemDefault()).getYear();
        } catch (IOException e) {
            LOGGER.trace("", e);
            return ZonedDateTime.now().getYear();
        }
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

	public static String toExternalForm(final String arquivo) {
		try {
            return Thread.currentThread().getContextClassLoader().getResource(arquivo).toExternalForm();
		} catch (RuntimeException e) {
			throw new RuntimeIOException("ERRO FILE \"" + arquivo + "\"", e);
		}
	}
	public static File toFile(final String arquivo) {
		return new File(toFullPath(arquivo));
	}

	public static String toFullPath(final String arquivo) {
		try {
            return URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource(arquivo).getFile(),
                "UTF-8");
		} catch (Exception e) {
			LOGGER.trace("File Error:" + arquivo, e);
			try {
				return URLDecoder.decode(new File(arquivo).getAbsolutePath(), "UTF-8");
			} catch (Exception e1) {
				LOGGER.error("File Error:" + arquivo, e1);
				return null;
			}
		}
	}

	public static Path toPath(final String arquivo) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(arquivo).getFile()).toPath();
	}


	public static InputStream toStream(final String arquivo) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(arquivo);
	}

    public static URI toURI(final String arquivo) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(arquivo).getFile()).toURI();
	}

    public static URL toURL(final String arquivo) {
        return Thread.currentThread().getContextClassLoader().getResource(arquivo);
	}

    private static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
		return (value - min) * (newMax - newMin) / (max - min) + newMin;
	}

}
