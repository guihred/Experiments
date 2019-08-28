package utils;

import java.awt.Desktop;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;

public final class ImageFXUtils {
    private ImageFXUtils() {
    }

    public static String take(final Canvas canvas) {
        return ImageFXUtils.take(canvas, canvas.getWidth(), canvas.getHeight());

    }

    public static String take(final Canvas canvas, final double w, final double h) {
        return SupplierEx.makeSupplier(() -> {
            final WritableImage writableImage = new WritableImage((int) w, (int) h);
            final WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), writableImage);
            File destination = File.createTempFile("snapshot", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "PNG", destination);
            Desktop.getDesktop().open(destination);
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
}
