package paintexp;

import java.io.File;
import java.io.FileInputStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import paintexp.tool.PaintModel;
import simplebuilder.FileChooserBuilder;
import utils.*;

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

    public static void openFile(ActionEvent e, PaintModel paintModel) {

        FileChooserBuilder chooser = new FileChooserBuilder();
        chooser.title("Open File");
        chooser.extensions("Image", "*.png", "*.jpg", "*.jpeg");
        chooser.initialDir(SupplierEx.getFirst(() -> FunctionEx.mapIf(paintModel.getCurrentFile(), File::getParentFile),
                () -> defaultFile));
        chooser.onSelect(file -> {
            paintModel.setCurrentFile(file);
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
        chooser.openFileAction().handle(e);
    }

    public static void print(PaintModel paintModel) {
        new PrintConfig(paintModel.getImage()).show();
    }

    public static void saveAsFile(ActionEvent primaryStage, PaintModel paintModel) {
        paintModel.setCurrentFile(null);
        saveFile(primaryStage, paintModel);
    }

    public static void saveFile(ActionEvent event, PaintModel paintModel) {
        if (paintModel.getCurrentFile() == null) {
            FileChooserBuilder chooser = new FileChooserBuilder();
            chooser.title("Save File");
            chooser.initialDir(SupplierEx.getFirst(
                    () -> FunctionEx.mapIf(paintModel.getCurrentFile(), File::getParentFile), () -> defaultFile));
            chooser.extensions("Image", "*.png");
            chooser.onSelect(paintModel::setCurrentFile);
            chooser.saveFileAction().handle(event);
        }
        RunnableEx.runIf(paintModel.getCurrentFile(), file -> {
            WritableImage image = paintModel.getImage();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
        });

    }

    public static void setDefaultFile(File defaultFile) {
        PaintFileUtils.defaultFile = defaultFile;
        LOG.info("DEFAULT FILE SET TO {}", defaultFile);
    }

}
