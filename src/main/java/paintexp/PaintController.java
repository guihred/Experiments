package paintexp;

import com.sun.javafx.scene.control.skin.CustomColorDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Toggle;
import javafx.scene.image.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import paintexp.tool.PaintTool;
import paintexp.tool.SelectRectTool;
import utils.HasLogging;

@SuppressWarnings("restriction")
public class PaintController {
	private static final Logger LOG = HasLogging.log();
	private PaintModel paintModel = new PaintModel();

	public void changeTool(final Toggle newValue) {
        getPaintModel().resetToolOptions();
		getPaintModel().getImageStack().getChildren().clear();
        ImageView imageView = new ImageView(getPaintModel().getImage());
        getPaintModel().getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        getPaintModel().getImageStack().getChildren().add(imageView);
		if (newValue != null) {
			getPaintModel().setTool((PaintTool) newValue.getUserData());
			PaintTool paintTool = getPaintModel().getTool();
			paintTool.onSelected(getPaintModel());
		}

	}

    public BooleanBinding containsSelectedArea() {
		SelectRectTool a = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
		return Bindings.createBooleanBinding(()->paintModel.getImageStack().getChildren().contains(a.getArea()), paintModel.getImageStack().getChildren());
	}

	public void copy() {
		paintModel.setTool(PaintTools.SELECT_RECT.getTool());
		changeTool(null);
		SelectRectTool a = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
		a.copyToClipboard(paintModel);
	}

	public void cut() {
		paintModel.setTool(PaintTools.SELECT_RECT.getTool());
		changeTool(null);
		SelectRectTool a = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
		a.copyToClipboard(paintModel);
        Bounds bounds = a.getArea().getBoundsInParent();
        a.drawRect(paintModel, bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
	}
	public void flipRotate() {
        WritableImage image = getImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(height, width);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setArgb(height - j - 1, i, pixelReader.getArgb(i, j));
            }
        }
        setImage(writableImage);
    }

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
        for (int i = 0; i < 360 / 2; i += a) {
            availableColors.add(Color.hsb(i, 1, 0.5));
        }
        for (int i = 360 / 2; i < 330; i += a) {
            availableColors.add(Color.hsb(i, .5, 1));
        }
        availableColors.add(Color.TRANSPARENT);
        return availableColors;
    }

    public PaintModel getPaintModel() {
		return paintModel;
	}

	public void handleKeyBoard(final KeyEvent e) {
		switch (e.getCode()) {
			case V:
			case A:
				if (e.isControlDown()) {
					paintModel.setTool(PaintTools.SELECT_RECT.getTool());
				}
				break;
			default:
				break;
		}
		PaintTool paintTool = paintModel.getTool();
		if (paintTool != null) {
			paintTool.handleKeyEvent(e, paintModel);
		}
	}

	public void handleMouse(final MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		paintModel.getMousePosition().setText(x > 0 && y > 0 ? String.format("%.0fx%.0f", x, y) : "");
		paintModel.getImageSize().setText(
				String.format("%.0fx%.0f", paintModel.getImage().getWidth(), paintModel.getImage().getHeight()));

		PaintTool paintTool = paintModel.getTool();
		if (paintTool != null) {
			paintModel.getImageStack().setCursor(paintTool.getMouseCursor());
			paintTool.handleEvent(e, paintModel);
		} else {
			paintModel.getImageStack().setCursor(Cursor.DEFAULT);
		}
	}

	public void invertColors() {
        WritableImage image = getImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setColor(i, j, pixelReader.getColor(i, j).invert());
            }
        }
        setImage(writableImage);
	    
	    
    }

	public void newFile() {
		getPaintModel().setImage(new WritableImage(500, 500));
		int w = (int) getPaintModel().getImage().getWidth();
		int h = (int) getPaintModel().getImage().getHeight();
		getPaintModel().getImage().getPixelWriter().setPixels(0, 0, w, h,
				new SimplePixelReader(getPaintModel().getBackColor()), 0, 0);
		getPaintModel().getImageStack().getChildren().clear();
        ImageView imageView = new ImageView(getPaintModel().getImage());
        getPaintModel().getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        getPaintModel().getImageStack().getChildren().add(imageView);

	}

	public Rectangle newRectangle(final Color color) {
		Rectangle rectangle = new Rectangle(20, 20, color);
		rectangle.setStroke(Color.BLACK);

        rectangle.setOnMouseClicked(e -> onColorClicked(color, rectangle, e));

		return rectangle;
	}

	public void openFile(final Window ownerWindow) {
        FileChooser fileChooser2 = new FileChooser();
        fileChooser2.setTitle("Open File");
        fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
        paintModel.setCurrentFile(fileChooser2.showOpenDialog(ownerWindow));
        if (paintModel.getCurrentFile() != null) {
            try {
                Image image2 = new Image(new FileInputStream(paintModel.getCurrentFile()));
                int w = (int) image2.getWidth();
                int h = (int) image2.getHeight();
                getPaintModel().setImage(new WritableImage(w, h));
                getPaintModel().getImage().getPixelWriter().setPixels(0, 0, w, h, image2.getPixelReader(), 0, 0);
                getPaintModel().getImageStack().getChildren().clear();
                ImageView imageView = new ImageView(getPaintModel().getImage());
                getPaintModel().getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
                getPaintModel().getImageStack().getChildren().add(imageView);

            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    public void paste() {
		paintModel.setTool(PaintTools.SELECT_RECT.getTool());
		changeTool(null);
		SelectRectTool a = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
		a.copyFromClipboard(paintModel);
	}

	public void saveAsFile(final Stage primaryStage) {
	    paintModel.setCurrentFile(null);
	    saveFile(primaryStage);
	}

	public void saveFile(final Stage primaryStage) {
		try {
			if (paintModel.getCurrentFile() == null) {
				FileChooser fileChooser2 = new FileChooser();
				fileChooser2.setTitle("Save File");
				fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
				paintModel.setCurrentFile(fileChooser2.showOpenDialog(primaryStage));
			}
			if (paintModel.getCurrentFile() != null) {
				File destination = paintModel.getCurrentFile();
				WritableImage image = getPaintModel().getImage();
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", destination);
			}
		} catch (IOException e) {
			LOG.error("", e);
		}
	}

    public void selectAll() {
		paintModel.setTool(PaintTools.SELECT_RECT.getTool());
		changeTool(null);
		SelectRectTool a = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
		a.selectArea(0, 0, paintModel.getImage().getWidth(), paintModel.getImage().getHeight(), paintModel);
	}

	private WritableImage getImage() {
        SelectRectTool tool = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
        WritableImage image;
        if (paintModel.getImageStack().getChildren().contains(tool.getArea())) {
            image = tool.createSelectedImage(paintModel);
        } else {
            image = paintModel.getImage();
        }
        return image;
    }

    private void onColorClicked(final Color color, Rectangle rectangle, MouseEvent e) {
        if (e.getClickCount() > 1) {
            CustomColorDialog dialog = new CustomColorDialog(rectangle.getScene().getWindow());
            dialog.setCurrentColor(color);
            dialog.setOnUse(() -> {
                Color customColor = dialog.getCustomColor();
                if (MouseButton.PRIMARY == e.getButton()) {
                    getPaintModel().setFrontColor(customColor);
                } else {
                    getPaintModel().setBackColor(customColor);
                }
            });
            dialog.setOnSave(() -> rectangle.setFill(dialog.getCustomColor()));
            dialog.show();
        } else if (MouseButton.PRIMARY == e.getButton()) {
            getPaintModel().setFrontColor((Color) rectangle.getFill());
        } else {
            getPaintModel().setBackColor((Color) rectangle.getFill());
        }
    }

    private void setImage(WritableImage writableImage) {
        SelectRectTool tool = (SelectRectTool) PaintTools.SELECT_RECT.getTool();
        if (paintModel.getImageStack().getChildren().contains(tool.getArea())) {
            tool.getArea().setWidth(writableImage.getWidth());
            tool.getArea().setHeight(writableImage.getHeight());
            tool.getArea().setFill(new ImagePattern(writableImage));
            tool.setImageSelected(writableImage);
        } else {
            getPaintModel().getImageStack().getChildren().clear();
            ImageView imageView = new ImageView(writableImage);
            getPaintModel().setImage(writableImage);
            getPaintModel().getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
            getPaintModel().getImageStack().getChildren().add(imageView);
        }
    }

}
