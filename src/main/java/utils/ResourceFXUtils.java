package utils;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
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
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
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

    public static void initializeFX() {
        Platform.setImplicitExit(false);
        new JFXPanel().toString();
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
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        return importer.getImport();
//        return null;
    }

    public static Mesh importStlMesh(URL file) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(file);
        return importer.getImport();
//        return null;
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

    public static void executeInConsole(String cmd) {
        try {
            LOGGER.info("Executing \"{}\"", cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("Success")) {
                    LOGGER.info("\"{}\"", line);
                }
            }
            p.waitFor();
            in.close();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public static Image extractEmbeddedImage(File mp3) {
        MP3File mp31 = new MP3File(mp3);
        try {
            for (ID3Tag tag : mp31.getTags()) {

                if (tag instanceof ID3V2_3_0Tag) {
                    ID3V2_3_0Tag tag2 = (ID3V2_3_0Tag) tag;

                    if (tag2.getAPICFrames() != null && tag2.getAPICFrames().length > 0) {
                        // Simply take the first image that is available.
                        APICID3V2Frame frame = tag2.getAPICFrames()[0];
                        return new Image(new ByteArrayInputStream(frame.getPictureData()));
                    }
                }
            }
            ID3V2Tag id3v2Tag = mp31.getID3V2Tag();
            ID3V2Frame[] singleFrames = id3v2Tag.getSingleFrames();
            String singleFramesStr = Arrays.toString(singleFrames);
            LOGGER.trace("SingleFrames={}", singleFramesStr);
        } catch (Exception e) {
            LOGGER.trace("", e);
        }
        return null;
    }

    public static Image createImage(double size1, float[][] noise) {
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

    public static double clamp(double value, double min, double max) {
        if (Double.compare(value, min) < 0) {
            return min;
        }
        if (Double.compare(value, max) > 0) {
            return max;
        }
        return value;
    }

    private static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }


}
