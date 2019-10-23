package paintexp;

import java.io.File;
import java.io.FileInputStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import paintexp.tool.PaintModel;
import utils.HasLogging;
import utils.PixelatedImageView;
import utils.RunnableEx;

public final class PaintFileUtils {
    private static final Logger LOG = HasLogging.log();
    private static File defaultFile;
    private static final int DEFAULT_SIZE = 500;

    private PaintFileUtils() {
    }

    public static void newFile(PaintModel paintModel) {
        paintModel.setImage(new WritableImage(DEFAULT_SIZE, DEFAULT_SIZE));
        int w = (int) paintModel.getImage().getWidth();
        int h = (int) paintModel.getImage().getHeight();
        paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, new SimplePixelReader(paintModel.getBackColor()),
            0, 0);
        paintModel.getImageStack().getChildren().clear();
        ImageView imageView = new PixelatedImageView(paintModel.getImage());
        paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        paintModel.getImageStack().getChildren().add(imageView);

    }

    public static void openFile(final Window ownerWindow, PaintModel paintModel) {
        FileChooser fileChooser2 = new FileChooser();
        fileChooser2.setTitle("Open File");
        fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
        paintModel.setCurrentFile(fileChooser2.showOpenDialog(ownerWindow));
        if (paintModel.getCurrentFile() != null) {
            RunnableEx.run(() -> {
                Image image2 = new Image(new FileInputStream(paintModel.getCurrentFile()));
                int w = (int) image2.getWidth();
                int h = (int) image2.getHeight();
                paintModel.setImage(new WritableImage(w, h));
                paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, image2.getPixelReader(), 0, 0);
                paintModel.getImageStack().getChildren().clear();
                ImageView imageView = new PixelatedImageView(paintModel.getImage());
                paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
                paintModel.getImageStack().getChildren().add(imageView);
                paintModel.createImageVersion();
            });
        }
    }

    public static void saveAsFile(final Window primaryStage, PaintModel paintModel) {
        paintModel.setCurrentFile(null);
        saveFile(primaryStage, paintModel);
    }

    public static void saveFile(final Window primaryStage, PaintModel paintModel) {
        RunnableEx.run(() -> {
            if (paintModel.getCurrentFile() == null) {
                FileChooser fileChooser2 = new FileChooser();
                fileChooser2.setTitle("Save File");
                if (defaultFile != null) {
                    fileChooser2.setInitialDirectory(defaultFile);
                }
                fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
                paintModel.setCurrentFile(fileChooser2.showSaveDialog(primaryStage));
            }
            if (paintModel.getCurrentFile() != null) {
                File destination = paintModel.getCurrentFile();
                WritableImage image = paintModel.getImage();
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", destination);
            }
        });
    }

    public static void setDefaultFile(File defaultFile) {
        PaintFileUtils.defaultFile = defaultFile;
        LOG.info("DEFAULT FILE SET TO {}", defaultFile);
    }

}
