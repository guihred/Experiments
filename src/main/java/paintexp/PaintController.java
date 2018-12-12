package paintexp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import paintexp.tool.PaintTool;
import paintexp.tool.SelectRectTool;

public class PaintController {

    private final PaintModel paintModel = new PaintModel();

    public void adjustColors() {
        PaintImageUtils.adjustColors(paintModel);
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
        PaintEditUtils.copy(paintModel, paintModel.getCurrentSelectTool());
	}

	public void crop() {
        PaintViewUtils.crop(paintModel, paintModel.getSelectedImage());
    }

	public void cut() {
        PaintEditUtils.cut(paintModel, paintModel.getCurrentSelectTool());
	}
	public void flipRotate() {
        PaintViewUtils.flipRotate(paintModel, paintModel.getSelectedImage());
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
        availableColors.add(Color.TRANSPARENT.invert());
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
        PaintImageUtils.invertColors(paintModel);
    }

	public void mirrorHorizontally() {
        PaintImageUtils.mirrorHorizontally(paintModel);
    }

	public void mirrorVertically() {
        PaintImageUtils.mirrorVertically(paintModel);
    }

	public void newFile() {
        PaintFileUtils.newFile(paintModel);
	}

	public Rectangle newRectangle(final Color color) {
		Rectangle rectangle = new Rectangle(20, 20, color);
		rectangle.setStroke(Color.BLACK);
        rectangle.setOnMouseClicked(e -> onColorClicked(color, rectangle, e));
		return rectangle;
	}

    public void openFile(final Window ownerWindow) {
        PaintFileUtils.openFile(ownerWindow, paintModel);
    }

	public void paste() {
        PaintEditUtils.paste(paintModel, paintModel.getCurrentSelectTool());
	}

    public void resize() {
        PaintViewUtils.resize(paintModel, paintModel.getSelectedImage());
    }

	public void saveAsFile(final Stage primaryStage) {
        PaintFileUtils.saveAsFile(primaryStage, paintModel);
    }

	public void saveFile(final Stage primaryStage) {
        PaintFileUtils.saveFile(primaryStage, paintModel);
	}

    public void selectAll() {
        PaintEditUtils.selectAll(paintModel, paintModel.getCurrentSelectTool());
	}

	public void undo() {
        PaintEditUtils.undo(paintModel);
    }


    private void onColorClicked(final Color color, final Rectangle rectangle, final MouseEvent e) {
        if (e.getClickCount() > 1) {
			ColorChooser dialog = new ColorChooser();
            dialog.setCurrentColor(color);
            dialog.setOnUse(() -> {
				Color customColor = dialog.getCurrentColor();
                if (MouseButton.PRIMARY == e.getButton()) {
                    paintModel.setFrontColor(customColor);
                } else {
                    paintModel.setBackColor(customColor);
                }
            });
			dialog.setOnSave(() -> rectangle.setFill(dialog.getCurrentColor()));
            dialog.show();
        } else if (MouseButton.PRIMARY == e.getButton()) {
            paintModel.setFrontColor((Color) rectangle.getFill());
        } else {
            paintModel.setBackColor((Color) rectangle.getFill());
        }
    }
}
