package paintexp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import paintexp.tool.AreaTool;
import paintexp.tool.PaintModel;
import paintexp.tool.PaintTool;
import paintexp.tool.PaintTools;
import utils.PixelatedImageView;
import utils.ZoomableScrollPane;

public class PaintController {

    private final PaintModel paintModel = new PaintModel();

    private final ObjectProperty<PaintTool> tool = paintModel.toolProperty();

    public void changeTool(final PaintTool newValue) {
        paintModel.resetToolOptions();
        ZoomableScrollPane parent = paintModel.getScrollPane();
        double hvalue = parent.getHvalue();
        double vvalue = parent.getVvalue();
        paintModel.getImageStack().getChildren().clear();
        ImageView imageView = new PixelatedImageView(paintModel.getImage());
        paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
        paintModel.getImageStack().getChildren().add(imageView);
        parent.setHvalue(hvalue);
        parent.setVvalue(vvalue);
        if (newValue != null) {
            PaintTool oldTool = getTool();
            if (oldTool != null) {
                oldTool.onDeselected(paintModel);
                paintModel.getToolOptions().getChildren().clear();
            }
            setTool(newValue);
            PaintTool paintTool = getTool();
            paintTool.onSelected(oldTool, paintModel);
        }
        paintModel.getToolOptions().setVisible(!paintModel.getToolOptions().getChildren().isEmpty());
        paintModel.getToolOptions().setManaged(!paintModel.getToolOptions().getChildren().isEmpty());
    }

    public BooleanBinding containsSelectedArea() {
        return Bindings.createBooleanBinding(
            () -> Stream.of(PaintTools.values()).map(PaintTools::getTool).filter(AreaTool.class::isInstance)
                .map(AreaTool.class::cast)
                .anyMatch(e -> paintModel.getImageStack().getChildren().contains(e.getArea())),
            paintModel.getImageStack().getChildren());
    }

    public AreaTool getCurrentSelectTool() {
        return PaintTools.getSelectRectTool(getTool(), paintModel.getImageStack());
    }

    public PaintModel getPaintModel() {
        return paintModel;
    }

    public WritableImage getSelectedImage() {
        AreaTool selectTool = getCurrentSelectTool();
        if (paintModel.getImageStack().getChildren().contains(selectTool.getArea())) {
            return selectTool.createSelectedImage(paintModel);
        }
        return paintModel.getImage();
    }

    public PaintTool getTool() {
        return tool.get();
    }

    public void handleKeyBoard(final KeyEvent e) {
        PaintTool paintTool = getTool();
        if (paintTool != null) {
            paintTool.handleKeyEvent(e, paintModel);
        }
    }

    public void handleMouse(final MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        paintModel.getMousePosition().setText(x > 0 && y > 0 ? String.format("%.0fx%.0f", x, y) : "");
        paintModel.getImageSize()
                .setText(String.format("%.0fx%.0f", paintModel.getImage().getWidth(), paintModel.getImage().getHeight()));

        PaintTool paintTool = getTool();
        if (paintTool != null) {
            paintModel.getImageStack().setCursor(paintTool.getMouseCursor());
            double hvalue = paintModel.getScrollPane().getHvalue();
            double vvalue = paintModel.getScrollPane().getVvalue();
            paintTool.handleEvent(e, paintModel);
            paintModel.getScrollPane().setHvalue(hvalue);
            paintModel.getScrollPane().setVvalue(vvalue);
        } else {
            paintModel.getImageStack().setCursor(Cursor.DEFAULT);
        }
    }

    public Rectangle newRectangle(final Color color) {
        Rectangle rectangle = new Rectangle(20, 20, color);
        rectangle.setStroke(Color.BLACK);
        rectangle.setOnMouseClicked(e -> onColorClicked(color, rectangle, e));
        return rectangle;
    }

    public void setFinalImage(final WritableImage writableImage) {
        AreaTool selectTool = getCurrentSelectTool();
        if (paintModel.getImageStack().getChildren().contains(selectTool.getArea())) {
            selectTool.getArea().setWidth(writableImage.getWidth());
            selectTool.getArea().setHeight(writableImage.getHeight());
            selectTool.getArea().setFill(new ImagePattern(writableImage));
            selectTool.setImageSelected(writableImage);
        } else {
            paintModel.getImageStack().getChildren().clear();
            ImageView imageView = new PixelatedImageView(writableImage);
            paintModel.setImage(writableImage);
            paintModel.getImageStack().getChildren().add(paintModel.getRectangleBorder(imageView));
            paintModel.getImageStack().getChildren().add(imageView);
        }
    }

    public void setTool(final PaintTool tool) {
        this.tool.set(tool);
    }

    public ObjectProperty<PaintTool> toolProperty() {
        return tool;
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

    public static List<Color> getColors() {
        List<Color> availableColors = new ArrayList<>();
        final double step = 30;
        final int colorDiff = 64;
        for (int i = 0; i < 2; i++) {
            availableColors.add(Color.grayRgb(i * colorDiff));
        }
        for (int i = 0; i < 12; i++) {
            availableColors.add(Color.hsb(i * step, 1, 1));
        }
        availableColors.add(Color.WHITE);
        availableColors.add(Color.grayRgb(colorDiff * 2));
        for (int i = 0; i < 6; i++) {
            availableColors.add(Color.hsb(i * step, 1, 0.5));
        }
        for (int i = 6; i < 11; i++) {
            availableColors.add(Color.hsb(i * step, .5, 1));
        }
        availableColors.add(Color.TRANSPARENT.invert());
        return availableColors;
    }
}
