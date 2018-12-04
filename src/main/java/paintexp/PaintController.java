package paintexp;
import static paintexp.tool.DrawOnPoint.withinImage;

import com.sun.javafx.scene.control.skin.CustomColorDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import paintexp.tool.PaintTool;
import paintexp.tool.SelectRectTool;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CommonsFX;
import utils.HasLogging;

@SuppressWarnings("restriction")
public class PaintController {
	private static final Logger LOG = HasLogging.log();
	private static final String PERCENTAGE_FIELD = "Percentage";

	private PaintModel paintModel = new PaintModel();

    public void adjustColors() {
		Stage stage = new Stage();
		VBox root = new VBox();
		WritableImage image = getImage();
		ImageView e = new ImageView(image);
		e.setFitWidth(300);
		e.setPreserveRatio(true);
		root.getChildren().add(e);

		addAdjustOption(root, image, "Saturate", (color, v) -> color.deriveColor(0, v, 1, 1));
		addAdjustOption(root, image, "Brightness", (color, v) -> color.deriveColor(0, 1, v, 1));
		addAdjustOption(root, image, "Hue", (color, v) -> color.deriveColor(v, 1, 1, 1));
		addAdjustOption(root, image, "Opacity", (color, v) -> color.deriveColor(0, 1, 1, v));
		stage.setScene(new Scene(root));
		stage.show();
	}

	public void changeTool(final Toggle newValue) {
        paintModel.changeTool(newValue == null ? null : (PaintTool) newValue.getUserData());
	}

	public BooleanBinding containsSelectedArea() {
        return Bindings.createBooleanBinding(
                () -> Stream.of(PaintTools.values())
                        .map(PaintTools::getTool)
                        .filter(SelectRectTool.class::isInstance)
                        .map(SelectRectTool.class::cast)
                        .anyMatch(e -> paintModel.getImageStack().getChildren().contains(e.getArea())),
                paintModel.getImageStack().getChildren());
	}

	public void copy() {
        SelectRectTool a = getCurrentSelectTool();
		a.copyToClipboard(paintModel);
	}

	public void crop() {
        WritableImage image = getImage();
        paintModel.getImageStack().getChildren().clear();
        ImageView imageView = new ImageView(image);
        SelectRectTool tool = getCurrentSelectTool();
        tool.setImageSelected(null);
        paintModel.setImage(image);
        paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        paintModel.getImageStack().getChildren().add(imageView);
        paintModel.createImageVersion();
    }
	public void cut() {
        SelectRectTool a = getCurrentSelectTool();
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
        paintModel.createImageVersion();
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
        paintModel.createImageVersion();
    }

	public void mirrorHorizontally() {
        WritableImage image = getImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setColor(width - i - 1, j, pixelReader.getColor(i, j));
            }
        }
        setImage(writableImage);
        paintModel.createImageVersion();
    }

	public void mirrorVertically() {
        WritableImage image = getImage();
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        PixelReader pixelReader = image.getPixelReader();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixelWriter.setColor(i, height - j - 1, pixelReader.getColor(i, j));
            }
        }
        setImage(writableImage);
        paintModel.createImageVersion();
    }

	public void newFile() {
		paintModel.setImage(new WritableImage(500, 500));
		int w = (int) paintModel.getImage().getWidth();
		int h = (int) paintModel.getImage().getHeight();
		paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h,
				new SimplePixelReader(paintModel.getBackColor()), 0, 0);
		paintModel.getImageStack().getChildren().clear();
        ImageView imageView = new ImageView(paintModel.getImage());
        paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        paintModel.getImageStack().getChildren().add(imageView);

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
                paintModel.setImage(new WritableImage(w, h));
                paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, image2.getPixelReader(), 0, 0);
                paintModel.getImageStack().getChildren().clear();
                ImageView imageView = new ImageView(paintModel.getImage());
                paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
                paintModel.getImageStack().getChildren().add(imageView);
				paintModel.createImageVersion();
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    public void paste() {
		if (!(paintModel.getTool() instanceof SelectRectTool)) {
			paintModel.setTool(PaintTools.SELECT_RECT.getTool());
			changeTool(null);
		}
		SelectRectTool a = getCurrentSelectTool();
		a.copyFromClipboard(paintModel);
	}

	public void resize() {
        WritableImage image = getImage();
        Stage stage = new Stage();
        VBox root = new VBox();
        root.getChildren().add(new Text("Redimension"));
        SimpleToggleGroupBuilder groupBuilder = new SimpleToggleGroupBuilder();
		List<RadioButton> togglesAs = groupBuilder.addRadioToggle(PERCENTAGE_FIELD)
                .addRadioToggle("Pixels")
                .select(0)
                .getTogglesAs(RadioButton.class);
        HBox row1 = new HBox(new Text("By:"));
        row1.getChildren().addAll(togglesAs);
        root.getChildren().add(row1);
        TextField widthField = new TextField("100");
        root.getChildren().add(new HBox(new Text("Width:"), widthField));
        TextField heightField = new TextField("100");
        root.getChildren().add(new HBox(new Text("Height:"), heightField));
        CheckBox keepProportion = new CheckBox("Keep Proportion");
        keepProportion.setSelected(true);
        root.getChildren().add(keepProportion);
        groupBuilder.onChange(
                (o, old, newV) -> {
					boolean pencentage = PERCENTAGE_FIELD.equals(((RadioButton) newV).getText());
                    widthField.setText(pencentage ? "100" : "" + (int) image.getWidth());
                    heightField.setText(pencentage ? "100" : "" + (int) image.getHeight());
                });
        double ratio = image.getWidth() / image.getHeight();
        keepProportion
                .setOnAction(e -> onResizeOptionsChange(groupBuilder, keepProportion, widthField, heightField, ratio));
        widthField.textProperty()
                .addListener(e -> onResizeOptionsChange(groupBuilder, keepProportion, widthField, heightField, ratio));
        heightField.textProperty()
                .addListener(
                        e -> onResizeOptionsChange(groupBuilder, keepProportion, heightField, widthField, 1 / ratio));
		root.getChildren()
				.add(CommonsFX.newButton("Resize", e -> {
					finishResize(image, groupBuilder, widthField, heightField);
					stage.close();
					paintModel.createImageVersion();
				}));
        stage.setScene(new Scene(root));
        stage.show();
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
                paintModel.setCurrentFile(fileChooser2.showSaveDialog(primaryStage));
			}
			if (paintModel.getCurrentFile() != null) {
				File destination = paintModel.getCurrentFile();
				WritableImage image = paintModel.getImage();
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", destination);
			}
		} catch (IOException e) {
			LOG.error("", e);
		}
	}

	public void selectAll() {
		if (!(paintModel.getTool() instanceof SelectRectTool)) {
			paintModel.setTool(PaintTools.SELECT_RECT.getTool());
			changeTool(null);
		}
		SelectRectTool a = getCurrentSelectTool();
        a.selectArea(0, 0, (int) paintModel.getImage().getWidth(), (int) paintModel.getImage().getHeight(), paintModel);
	}

	public void undo() {
        List<WritableImage> imageVersions = paintModel.getImageVersions();
        if (!imageVersions.isEmpty()) {
            WritableImage writableImage = imageVersions.remove(imageVersions.size() - 1);
            paintModel.getImageStack().getChildren().clear();
            ImageView imageView = new ImageView(writableImage);
            paintModel.setImage(writableImage);
            paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
            paintModel.getImageStack().getChildren().add(imageView);
        }
    }

    private void addAdjustOption(final VBox root, final WritableImage image, final String text, final BiFunction<Color, Double, Color> func) {
		root.getChildren().add(new Text(text));
		Slider saturation = new SimpleSliderBuilder(0, 10, 1).build();
		saturation.valueProperty().addListener(
				(ob, old, value) -> {
					updateImage(saturation, image, value, func);
				});

		root.getChildren().add(saturation);
	}

    private void changeIfDifferent(final TextField heightField, final String replaceAll) {
        if (!heightField.getText().equals(replaceAll) && Math.abs(tryParse(heightField) - tryParse(replaceAll)) > 1) {
            heightField.setText(replaceAll);
        }
    }

	private void finishResize(final WritableImage image, final SimpleToggleGroupBuilder groupBuilder, final TextField widthField,
			final TextField heightField) {
		ToggleButton selectedItem = (ToggleButton) groupBuilder.selectedItem();
		double newWidth = PERCENTAGE_FIELD.equals(selectedItem.getText())
				? tryParse(widthField) * image.getWidth() / 100
				: tryParse(widthField);
		double newHeight = PERCENTAGE_FIELD.equals(selectedItem.getText())
				? tryParse(heightField) * image.getHeight() / 100
				: tryParse(heightField);
		WritableImage newImage = new WritableImage((int) newWidth, (int) newHeight);
		double width = image.getWidth();
		double height = image.getHeight();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Color color = image.getPixelReader().getColor(i, j);
				double yRatio = newHeight / height;
				int y = (int) (j * yRatio);
				double xRatio = newWidth / width;
				int x = (int) (i * xRatio);
                if (withinImage(x, y, newImage)) {
					newImage.getPixelWriter().setColor(x, y, color);
				}
				for (int l = 0; l < xRatio; l++) {
					for (int k = 0; k < yRatio; k++) {
                        if (withinImage(x + l, y + k, newImage)) {
							newImage.getPixelWriter().setColor(x + l, y + k, color);
						}
					}
				}
			}
		}
		setImage(newImage);
	}

    private SelectRectTool getCurrentSelectTool() {
		return Stream.of(PaintTools.values()).map(PaintTools::getTool)
                .filter(SelectRectTool.class::isInstance).map(SelectRectTool.class::cast)
                .filter(e -> paintModel.getImageStack().getChildren().contains(e.getArea()))
                .findFirst()
                .orElseGet(() -> (SelectRectTool) PaintTools.SELECT_RECT.getTool());
    }

    private WritableImage getImage() {
        SelectRectTool tool = getCurrentSelectTool();
        if (paintModel.getImageStack().getChildren().contains(tool.getArea())) {
            return tool.createSelectedImage(paintModel);
        }
        return paintModel.getImage();
    }

    private void onColorClicked(final Color color, final Rectangle rectangle, final MouseEvent e) {
        if (e.getClickCount() > 1) {
            CustomColorDialog dialog = new CustomColorDialog(rectangle.getScene().getWindow());
            dialog.setCurrentColor(color);
            dialog.setOnUse(() -> {
                Color customColor = dialog.getCustomColor();
                if (MouseButton.PRIMARY == e.getButton()) {
                    paintModel.setFrontColor(customColor);
                } else {
                    paintModel.setBackColor(customColor);
                }
            });
            dialog.setOnSave(() -> rectangle.setFill(dialog.getCustomColor()));
            dialog.show();
        } else if (MouseButton.PRIMARY == e.getButton()) {
            paintModel.setFrontColor((Color) rectangle.getFill());
        } else {
            paintModel.setBackColor((Color) rectangle.getFill());
        }
    }

    private void onResizeOptionsChange(final SimpleToggleGroupBuilder groupBuilder, final CheckBox keepProportion,
            final TextField changedField, final TextField otherField, final double ratio) {
        RadioButton selectedItem = (RadioButton) groupBuilder.selectedItem();
        if (keepProportion.isSelected()) {
			if (PERCENTAGE_FIELD.equals(selectedItem.getText())) {
                changeIfDifferent(otherField, changedField.getText());
                return;
            }
            if (StringUtils.isNumeric(changedField.getText())) {
                int width2 = tryParse(changedField);
                String newHeight = "" + (int) (ratio * width2);
                changeIfDifferent(otherField, newHeight);
            }
        }

    }

    private void setImage(final WritableImage writableImage) {
        SelectRectTool tool = getCurrentSelectTool();
        if (paintModel.getImageStack().getChildren().contains(tool.getArea())) {
            tool.getArea().setWidth(writableImage.getWidth());
            tool.getArea().setHeight(writableImage.getHeight());
            tool.getArea().setFill(new ImagePattern(writableImage));
            tool.setImageSelected(writableImage);
        } else {
            paintModel.getImageStack().getChildren().clear();
            ImageView imageView = new ImageView(writableImage);
            paintModel.setImage(writableImage);
            paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
            paintModel.getImageStack().getChildren().add(imageView);
        }
    }

	private void updateImage(final Slider saturation, final WritableImage image, final Number value,
			final BiFunction<Color, Double, Color> func) {
		if (!saturation.isValueChanging()) {
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					Color color = image.getPixelReader().getColor(x, y);
					double v = value.doubleValue();
					Color deriveColor = func.apply(color, v);
					image.getPixelWriter().setColor(x, y, deriveColor);
				}
			}
		}
	}

    private static int tryParse(final String widthField) {
        try {
            return Integer.parseInt(widthField);
        } catch (NumberFormatException e) {
            LOG.trace("whatever",e);
            return 0;
        }
    }

	private static int tryParse(final TextField widthField) {
		return tryParse(widthField.getText().replaceAll("\\D", ""));
	}

}
