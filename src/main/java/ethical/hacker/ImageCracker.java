package ethical.hacker;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.Tesseract;
import utils.DrawOnPoint;
import utils.FileTreeWalker;
import utils.PixelHelper;
import utils.ResourceFXUtils;
import utils.ex.SupplierEx;

public final class ImageCracker {
    private static final Tesseract INSTANCE = getInstance();

    private ImageCracker() {
    }

    public static String crackImage(File imageFile) {
        return SupplierEx.get(() -> INSTANCE.doOCR(imageFile));
    }

    public static String crackImage(Image img) {
        return SupplierEx.get(() -> {
            File outFile = ResourceFXUtils.getOutFile("png/captchaOut.png");
            try (FileOutputStream out = new FileOutputStream(outFile)) {
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "PNG", out);
            }
            return crackImage(outFile);
        }, "Error writing image");
    }

    public static Map<File, String> crackImages(File imageFile) {
        List<Path> pathByExtension = FileTreeWalker.getPathByExtension(imageFile, ".png", ".PNG");
        return pathByExtension.stream().map(Path::toFile)
                .collect(Collectors.toMap(e -> e, ImageCracker::crackImage));
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
                int blacks = countDarkPoints(image, pixelReader, x, y);
                if (DrawOnPoint.withinImage(x, y, image)) {
                    Color color = pixelReader.getColor(x, y);
                    double brightness = color.getBrightness();
                    image.getPixelWriter().setArgb(x, y, finalColor(white, black, blacks, brightness));
                }
            }
        }
        return image;
    }

    private static int countDarkPoints(WritableImage image, PixelReader pixelReader, int x, int y) {
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
        return blacks;
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