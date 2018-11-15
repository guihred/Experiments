package paintexp;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import paintexp.tool.PaintTool;
import utils.HasLogging;

public class PaintController {
	private static final Logger LOG = HasLogging.log();
	private PaintModel paintModel = new PaintModel();

	public List<Color> getColors() {
		List<Color> availableColors = new ArrayList<>();
		int a = 360 / 12;
		for (int i = 0; i < 128; i += 64) {
			availableColors.add(Color.grayRgb(i));
		}
		for (int i = 0; i < 360; i += a) {
			availableColors.add(Color.hsb(i, 1, 1));
		}
		availableColors.add(Color.WHITE);
		availableColors.add(Color.grayRgb(128));
		for (int i = 0; i < 360; i += a) {
			availableColors.add(Color.hsb(i, .5, 0.5));
		}
		return availableColors;
	}

	public Rectangle newRectangle(final Color color) {
		Rectangle rectangle = new Rectangle(20, 20, color);
		rectangle.setStroke(Color.BLACK);
		rectangle.setOnMouseClicked(e -> {
			if (MouseButton.PRIMARY == e.getButton()) {
				paintModel.setFrontColor(color);
			} else {
				paintModel.setBackColor(color);
			}

		});

		return rectangle;
	}

	public void changeTool(final Toggle newValue) {
		paintModel.getImageStack().getChildren().clear();
		paintModel.getImageStack().getChildren().add(new ImageView(paintModel.getImage()));
		paintModel.getTool().set((PaintTool) newValue.getUserData());
		PaintTool paintTool = paintModel.getTool().get();
		paintTool.onSelected(paintModel);

	}

	public void newFile() {
		paintModel.setImage(new WritableImage(500, 500));
		int w = (int) paintModel.getImage().getWidth();
		int h = (int) paintModel.getImage().getHeight();
		paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, new SimplePixelReader(paintModel.getBackColor()),
				0, 0);
		paintModel.getImageStack().getChildren().clear();
		paintModel.getImageStack().getChildren().add(new ImageView(paintModel.getImage()));

	}

	public void openFile(final Window ownerWindow) {
		FileChooser fileChooser2 = new FileChooser();
		fileChooser2.setTitle("Open File");
		fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
		File showOpenDialog = fileChooser2.showOpenDialog(ownerWindow);
		if (showOpenDialog != null) {
			try {
				Image image2 = new Image(new FileInputStream(showOpenDialog));
				int w = (int) image2.getWidth();
				int h = (int) image2.getHeight();
				paintModel.setImage(new WritableImage(w, h));
				paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, image2.getPixelReader(), 0, 0);
				paintModel.getImageStack().getChildren().clear();
				paintModel.getImageStack().getChildren().add(new ImageView(paintModel.getImage()));
			} catch (Exception e) {
				LOG.error("", e);
			}
		}
	}
}
