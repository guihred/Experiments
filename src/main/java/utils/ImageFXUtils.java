package utils;

import static utils.ResourceFXUtils.convertToURL;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javax.imageio.ImageIO;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class ImageFXUtils {
    private static boolean showImage = true;

    private ImageFXUtils() {
    }

    public static WritableImage copyImage(Image selectedImage, int width, int height) {
        int max = (int) Math.max(selectedImage.getWidth(), width);
        int max2 = (int) Math.max(selectedImage.getHeight(), height);
        WritableImage wr = new WritableImage(max, max2);
        PixelWriter pixelWriter = wr.getPixelWriter();
        PixelReader pixelReader = selectedImage.getPixelReader();
        for (int i = 0; i < selectedImage.getWidth(); i++) {
            for (int j = 0; j < selectedImage.getHeight(); j++) {
                pixelWriter.setArgb(i, j, pixelReader.getArgb(i, j));
            }
        }
        return wr;
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
                gray = DrawOnPoint.clamp(gray, 0, 1);
                Color color = Color.RED.interpolate(Color.YELLOW, gray);
                pw.setColor(x, y, color);
            }
        }
        return wr;
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

    public static List<Color> generateRandomColors(final int size) {
        final int maxByte = 255;
        int max = 256;
        List<Color> availableColors = new ArrayList<>();
        int cubicRoot = Integer.max((int) Math.ceil(Math.pow(size, 1.0 / 3.0)), 2);
        for (int i = 0; i < cubicRoot * cubicRoot * cubicRoot; i++) {
            Color rgb = Color.rgb(Math.abs(maxByte - i / cubicRoot / cubicRoot % cubicRoot * max / cubicRoot) % max,
                    Math.abs(maxByte - i / cubicRoot % cubicRoot * max / cubicRoot) % max,
                    Math.abs(maxByte - i % cubicRoot * max / cubicRoot) % max);

            availableColors.add(rgb);
        }
        Collections.shuffle(availableColors);
        return availableColors;
    }

    public static Image getClipboardImage() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        return SupplierEx.orElse(systemClipboard.getImage(),
                () -> ImageFXUtils.gatherImages(systemClipboard.getFiles()));
    }

    public static String getClipboardString() {
        return Clipboard.getSystemClipboard().getString();
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

    public static void printInDesktop(File destination) {
        RunnableEx.run(() -> {
            if (showImage) {
                Desktop.getDesktop().print(destination);
            }
        });
    }

    public static void saveImage(WritableImage image, File file) throws IOException {
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
    }

    public static void setClipboardContent(File file) {
        Map<DataFormat, Object> content = FXCollections.observableHashMap();
        content.put(DataFormat.FILES, Arrays.asList(file));
        Clipboard.getSystemClipboard().setContent(content);
    }

    public static void setClipboardContent(Image imageSelected2) {
        Map<DataFormat, Object> content = FXCollections.observableHashMap();
        content.put(DataFormat.IMAGE, imageSelected2);
        Clipboard.getSystemClipboard().setContent(content);
    }

    public static void setClipboardContent(String collect) {
        Map<DataFormat, Object> content = FXCollections.observableHashMap();
        content.put(DataFormat.PLAIN_TEXT, collect);
        Clipboard.getSystemClipboard().setContent(content);
    }

    public static void setShowImage(boolean showImage) {
        ImageFXUtils.showImage = showImage;
    }

    public static File take(final Canvas canvas) {
        return ImageFXUtils.take(canvas, canvas.getWidth(), canvas.getHeight());

    }

    public static File take(final Node canvas) {
        return ImageFXUtils.take(canvas, canvas.getBoundsInParent().getWidth(), canvas.getBoundsInParent().getHeight(),
                canvas.getScaleX());

    }

    public static File take(final Node canvas, final double w, final double h) {
        return take(canvas, w, h, 1);
    }

    public static File take(final Node canvas, final double w, final double h, final double scale) {
        return SupplierEx.get(() -> take(canvas, w, h, scale,
                File.createTempFile("snapshot", ".png", ResourceFXUtils.getOutFile("png"))));
    }

    public static File take(final Node canvas, File out) {
        return ImageFXUtils.take(canvas, canvas.getBoundsInLocal().getWidth(), canvas.getBoundsInLocal().getHeight(),
                canvas.getScaleX(), out);

    }

    public static WritableImage take(Node canvas, Rectangle2D viewport) {
        return SupplierEx.get(() -> {
            WritableImage writableImage = new WritableImage((int) viewport.getWidth(), (int) viewport.getHeight());
            SnapshotParameters params = new SnapshotParameters();
            params.setViewport(viewport);
            return canvas.snapshot(params, writableImage);
        });
    }

    public static BufferedImage toBufferedImage(final Node canvas) {
        return ImageFXUtils.toBufferedImage(canvas, Math.max(100, canvas.getBoundsInLocal().getWidth()),
                Math.max(100, canvas.getBoundsInLocal().getHeight()), canvas.getScaleX());

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

    public static byte[] toByteArray(Image image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", output);
        return output.toByteArray();
    }

    public static WritableImage toImage(final Node canvas) {
        return toImage(canvas, canvas.getScaleX());
    }

    public static WritableImage toImage(final Node canvas, double scale) {
        return SupplierEx.get(() -> {
            double d = Math.max(100, canvas.getBoundsInLocal().getWidth()) * scale;
            double e = Math.max(100, canvas.getBoundsInLocal().getHeight()) * scale;
            final WritableImage writableImage = new WritableImage((int) d, (int) e);
            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(new Scale(scale, scale));
            return canvas.snapshot(params, writableImage);
        });

    }

    private static Image gatherImages(List<File> files) {
        List<Image> collect =
                files.stream().map(FunctionEx.makeFunction(f -> new Image(convertToURL(f).toExternalForm())))
                        .filter(Objects::nonNull).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return null;
        }
        if (collect.size() == 1) {
            return collect.get(0);
        }
        double width = collect.stream().mapToDouble(Image::getWidth).max().orElse(0);
        double height = collect.stream().mapToDouble(Image::getHeight).max().orElse(0);
        WritableImage writableImage = new WritableImage((int) width, (int) height * collect.size());
        int x = 0;
        for (Image image : collect) {
            int height2 = (int) image.getHeight();
            writableImage.getPixelWriter().setPixels(0, x, (int) image.getWidth(), height2, image.getPixelReader(), 0,
                    0);
            x += height2;
        }
        return writableImage;
    }

    private static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
        return (value - min) * (newMax - newMin) / (max - min) + newMin;
    }

    private static File take(final Node canvas, final double w, final double h, final double scale, File destination) {
        return SupplierEx.get(() -> {
            final WritableImage writableImage = new WritableImage((int) (w * scale), (int) (h * scale));
            SnapshotParameters params = new SnapshotParameters();
            params.setTransform(new Scale(scale, scale));
            final WritableImage snapshot = canvas.snapshot(params, writableImage);
            BufferedImage fromFXImage = SwingFXUtils.fromFXImage(snapshot, null);
            ImageIO.write(fromFXImage, "PNG", destination);
            return destination;
        });
    }
}
