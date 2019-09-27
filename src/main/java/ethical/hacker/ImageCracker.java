package ethical.hacker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import org.slf4j.Logger;
import utils.DrawOnPoint;
import utils.HasLogging;
import utils.PixelHelper;
import utils.ResourceFXUtils;

public final class ImageCracker {
    private static final Tesseract INSTANCE = getInstance();
    private static final Logger LOG = HasLogging.log();

    private ImageCracker() {
    }


    public static String crackImage(File imageFile) {
        try {
            return INSTANCE.doOCR(imageFile);
        } catch (Exception e) {
            String format = String.format("ERROR IN %s", imageFile);
            LOG.error(format, e);
            return "Error while reading image";
        }
    }
    public static String crackImage(Image img) {
        File outFile = ResourceFXUtils.getOutFile("captchaOut.png");
		try (FileOutputStream out = new FileOutputStream(outFile)) {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", out);
        } catch (IOException e) {
            LOG.error("", e);
            return "Error writing image";
        }
        return crackImage(outFile);
    }

    public static WritableImage createSelectedImage(Image image) {
        PixelReader pixelReader = image.getPixelReader();
        return createSelectedImage(new WritableImage(pixelReader, (int) image.getWidth(), (int) image.getHeight()));
    }

    public static WritableImage createSelectedImage(WritableImage image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        int white = PixelHelper.toArgb(Color.WHITE);
        int black = PixelHelper.toArgb(Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int blacks = 0;
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int y2 = y + j - 1;
                        int x2 = x + i - 1;
                        if (DrawOnPoint.withinImage(x2, y2, image)) {
                            Color color = pixelReader.getColor(x2, y2);
                            double brightness = color.getBrightness();
                            blacks += brightness < 0.5 ? 1 : -1;
                        }
                    }
                }
                if (DrawOnPoint.withinImage(x, y, image)) {
                    Color color = pixelReader.getColor(x, y);
                    double brightness = color.getBrightness();
                    image.getPixelWriter().setArgb(x, y, finalColor(white, black, blacks, brightness));
                }
            }
        }
        return image;
    }

    private static int finalColor(int white, int black, int blacks, double brightness) {
        int limit = 2;
        if (Math.abs(blacks) > limit) {
            return blacks > 0 ? black : white;
        }
		return brightness > 0.5 ? white : black;
    }

    private static Tesseract getInstance() {
        String parent = ResourceFXUtils.toFile("tessdata/eng.traineddata").getParent();
        Tesseract instance = new Tesseract();
        instance.setDatapath(parent);
        return instance;
    }

}