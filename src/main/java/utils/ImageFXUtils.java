package utils;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javax.imageio.ImageIO;

public final class ImageFXUtils {
    private static boolean showImage = true;

    private ImageFXUtils() {
    }

    public static WritableImage flip(Image selectedImage) {
        int height = (int) selectedImage.getHeight();
    	int width = (int) selectedImage.getWidth();
        WritableImage writableImage = new WritableImage(height, width);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
    	PixelReader pixelReader = selectedImage.getPixelReader();
    	for (int i = 0; i < selectedImage.getWidth(); i++) {
    		for (int j = 0; j < selectedImage.getHeight(); j++) {
                pixelWriter.setArgb(height - j - 1, i, pixelReader.getArgb(i, j));
            }
        }
        return writableImage;
    }

    static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

    public static Image createImage(double size1, float[][] noise) {
        int width = (int) size1;
        int height = (int) size1;
    
        WritableImage wr = new WritableImage(width, height);
        PixelWriter pw = wr.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float value = noise[x][y];
                double gray = ImageFXUtils.normalizeValue(value, -.5, .5, 0., 1.);
                gray = ResourceFXUtils.clamp(gray, 0, 1);
                Color color = Color.RED.interpolate(Color.YELLOW, gray);
                pw.setColor(x, y, color);
            }
        }
        return wr;
    }

    public static WritableImage imageCopy(Image image) {
        return new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
    }

    public static void openInDesktop(File destination) {
        RunnableEx.run(() -> {
            if (showImage) {
                Desktop.getDesktop().open(destination);
            }
        });
    }

    public static void setShowImage(boolean showImage) {
        ImageFXUtils.showImage = showImage;
    }

    public static String take(final Canvas canvas) {
        return ImageFXUtils.take(canvas, canvas.getWidth(), canvas.getHeight());

    }

    public static String take(final Node canvas) {
        return ImageFXUtils.take(canvas, canvas.getBoundsInParent().getWidth(), canvas.getBoundsInParent().getHeight(),
                canvas.getScaleX());

    }

    public static String take(final Node canvas, final double w, final double h) {
        return take(canvas, w, h, 1);
    }

    public static String take(final Node canvas, final double w, final double h, final double scale) {
        return SupplierEx.makeSupplier(() -> {
            final WritableImage writableImage = new WritableImage((int) (w * scale), (int) (h * scale));
            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(new Scale(scale, scale));
            final WritableImage snapshot = canvas.snapshot(params, writableImage);
            File destination = File.createTempFile("snapshot", ".png");
            BufferedImage fromFXImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(fromFXImage, "PNG", destination);
            openInDesktop(destination);
            return destination.getAbsolutePath();
        }).get();
    }

    public static WritableImage take(Node canvas, Rectangle2D viewport) {
        return SupplierEx.makeSupplier(() -> {
            WritableImage writableImage = new WritableImage((int) viewport.getWidth(), (int) viewport.getHeight());
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(viewport);
            return canvas.snapshot(params, writableImage);
        }).get();
    }

    public static BufferedImage toBufferedImage(final Node canvas, final double w, final double h, final double scale) {
        return SupplierEx.get(() -> {
            final WritableImage writableImage = new WritableImage((int) (w * scale), (int) (h * scale));
            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(new Scale(scale, scale));
            final WritableImage snapshot = canvas.snapshot(params, writableImage);
            return SwingFXUtils.fromFXImage(snapshot, null);
        });
    }
}
