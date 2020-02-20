package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;
import static utils.PixelHelper.replaceColor;
import static utils.ResourceFXUtils.convertToURL;

import java.io.File;
import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleRectangleBuilder;
import utils.CommonsFX;
import utils.RunnableEx;

public abstract class AreaTool extends PaintTool {
    protected WritableImage imageSelected;
    protected double initialX;
    private double dragX;
    private double dragY;
    protected double initialY;
    protected SelectOption option = SelectOption.OPAQUE;
    private Rectangle area;

    public final void copyFromClipboard(PaintModel model) {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        Image image = systemClipboard.getImage();
        if (image != null) {
            copyImage(model, image, imageSelected);
        } else if (systemClipboard.getFiles() != null) {
            copyFromFile(model, systemClipboard.getFiles());
        }
        if (imageSelected != null) {
            getArea().setFill(new ImagePattern(imageSelected));
        }
    }

    public final void copyToClipboard(WritableImage image) {
        if (imageSelected == null) {
            double width = Math.max(1, getArea().getWidth());
            double height = Math.max(1, getArea().getHeight());
            imageSelected = new WritableImage((int) width, (int) height);
            RectBuilder.copyImagePart(image, imageSelected, getArea());
        }
        PaintToolHelper.setClipboardContent(imageSelected);
    }

    public WritableImage createSelectedImage(PaintModel model) {
        WritableImage srcImage = model.getImage();
        return createSelectedImage(model, srcImage);
    }

    public void cutImage(PaintModel model) {
        copyToClipboard(model.getImage());
        if (model.getImageStack().getChildren().contains(getArea())) {
            model.getImageStack().getChildren().remove(getArea());
        }
        imageSelected = null;
        model.createImageVersion();
    }

    public Rectangle getArea() {
        if (area == null) {
            area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK).cursor(Cursor.MOVE)
                .managed(false).strokeDashArray(1, 2, 1, 2).build();
        }
        return area;
    }

    @Override
    public void handleEvent(MouseEvent e, PaintModel model) {
        simpleHandleEvent(e, model);
    }

    @Override
    public void handleKeyEvent(KeyEvent e, PaintModel model) {
        KeyCode code = e.getCode();
        if (PaintTool.moveArea(code, getArea())) {
            e.consume();
            return;
        }
        if (e.getEventType() != KeyEvent.KEY_PRESSED && e.getEventType() != KeyEvent.KEY_TYPED) {
            return;
        }
        if (handleDeleteEscape(e, model)) {
            e.consume();
            return;
        }
        if (e.isControlDown() && handleControlDown(model, code)) {
            e.consume();
        }
    }

    @Override
    public void onDeselected(PaintModel model) {
        escapeArea(model);
    }

    @Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();

        model.getToolOptions().getChildren()
            .addAll(SelectOption.createSelectOptions((ob, old, newV) -> onChangeOption(newV, model), option));

    }

    public void selectArea(int x, int y, int endX, int endY, PaintModel model) {
        initialX = x;
        initialY = y;
        addRect(model);
        dragTo(endX, endY);
        onMouseReleased(model);
    }

    public void setImageSelected(WritableImage imageSelected) {
        this.imageSelected = imageSelected;
    }

    protected void addRect(PaintModel model) {
        if (!model.getImageStack().getChildren().contains(getArea())) {
            model.getImageStack().getChildren().add(getArea());
        }
        area.setStroke(Color.BLACK);
        getArea().setManaged(false);
        getArea().setFill(Color.TRANSPARENT);
        getArea().setLayoutX(initialX);
        getArea().setLayoutY(initialY);
        getArea().setWidth(1);
        getArea().setHeight(1);
    }

    protected final void copyImage(PaintModel model, Image srcImage, WritableImage destImage) {
        Color backColor = model.getBackColor();
        double width = srcImage.getWidth();
        double height = srcImage.getHeight();
        if (destImage == null) {
            imageSelected = new WritableImage((int) width, (int) height);
        }
        WritableImage writableImage = destImage != null ? destImage : imageSelected;
        RectBuilder.build().width(width).height(height).copyImagePart(srcImage, writableImage, backColor);
        if (option == SelectOption.TRANSPARENT) {
            replaceColor(writableImage, backColor, Color.TRANSPARENT);
        }

        selectArea(0, 0, (int) srcImage.getWidth(), (int) srcImage.getHeight(), model);
    }

    protected void dragTo(double x, double y) {
        getArea().setLayoutX(Math.min(x, initialX));
        getArea().setLayoutY(Math.min(y, initialY));
        getArea().setWidth(Math.abs(x - initialX));
        getArea().setHeight(Math.abs(y - initialY));
    }

    protected void escapeArea(PaintModel model) {
        double hvalue = model.getScrollPane().getHvalue();
        double vvalue = model.getScrollPane().getVvalue();
        if (imageSelected != null) {
            setIntoImage(model);
        }
        if (model.getImageStack().getChildren().contains(getArea())) {
            model.getImageStack().getChildren().remove(getArea());
        }
        model.getScrollPane().setHvalue(hvalue);
        model.getScrollPane().setVvalue(vvalue);
    }

    @Override
    protected void onMouseDragged(MouseEvent e, PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        double width = model.getImage().getWidth();
        double height = model.getImage().getHeight();
        if (model.getImageStack().getChildren().contains(getArea()) && imageSelected != null) {
            getArea().setLayoutX(Math.max(x - dragX, -width / 4));
            getArea().setLayoutY(Math.max(y - dragY, -height / 4));
            return;
        }
        dragTo(getWithinRange(x, 0, width), getWithinRange(y, 0, height));
    }

    @Override
    protected void onMousePressed(MouseEvent e, PaintModel model) {
        double hvalue = model.getScrollPane().getHvalue();
        double vvalue = model.getScrollPane().getVvalue();
        if (model.getImageStack().getChildren().contains(getArea())) {
            if (CommonsFX.containsMouse(area, e)) {
                createSelectedImage(model);
                dragX = e.getX() - getArea().getLayoutX();
                dragY = e.getY() - getArea().getLayoutY();
                return;
            }
            if (imageSelected != null) {
                setIntoImage(model);
            }
        }
        initialX = e.getX();
        initialY = e.getY();
        addRect(model);
        model.getScrollPane().setHvalue(hvalue);
        model.getScrollPane().setVvalue(vvalue);
    }

    @Override
    protected void onMouseReleased(PaintModel model) {
        double hvalue = model.getScrollPane().getHvalue();
        double vvalue = model.getScrollPane().getVvalue();
        if (getArea().getWidth() < 2 && model.getImageStack().getChildren().contains(getArea())
            && imageSelected != null) {
            model.getImageStack().getChildren().remove(getArea());
        } else {
            createSelectedImage(model);
        }
        getArea().setStroke(Color.BLUE);
        model.getScrollPane().setHvalue(hvalue);
        model.getScrollPane().setVvalue(vvalue);
    }

    protected void setIntoImage(PaintModel model) {
        double hvalue = model.getScrollPane().getHvalue();
        double vvalue = model.getScrollPane().getVvalue();
        int x = (int) getArea().getLayoutX();
        int y = (int) getArea().getLayoutY();
        double width = getArea().getWidth();
        double height = getArea().getHeight();
        RectBuilder.build().width(width).height(height).endX(x).endY(y).copyImagePart(imageSelected, model.getImage(),
            Color.TRANSPARENT);
        imageSelected = null;
        model.getImageStack().getChildren().remove(getArea());
        model.createImageVersion();
        model.getScrollPane().setHvalue(hvalue);
        model.getScrollPane().setVvalue(vvalue);
    }

    private void copyFromFile(PaintModel model, List<File> files) {
        if (!files.isEmpty()) {
            RunnableEx
                .run(() -> copyImage(model, new Image(convertToURL(files.get(0)).toExternalForm()), imageSelected));
        }
    }

    private WritableImage createSelectedImage(PaintModel model, WritableImage srcImage) {
        if (imageSelected == null) {
            int width = Math.max(1, (int) getArea().getWidth());
            int height = Math.max(1, (int) getArea().getHeight());
            imageSelected = new WritableImage(width, height);
            int layoutX = (int) getArea().getLayoutX();
            int layoutY = (int) getArea().getLayoutY();
            RectBuilder.copyImagePart(srcImage, imageSelected, getArea());
            if (option == SelectOption.TRANSPARENT) {
                replaceColor(imageSelected, model.getBackColor(), Color.TRANSPARENT.invert());
            }

            getArea().setFill(new ImagePattern(imageSelected));
            RectBuilder.build().startX(layoutX).startY(layoutY).width(width).height(height).drawRect(model.getImage(),
                model.getBackColor());
            model.createImageVersion();
        }
        return imageSelected;
    }

    private void deleteImage(PaintModel model, Bounds bounds) {
        if (imageSelected == null) {
            final PaintModel model1 = model;
            RectBuilder.build().startX(bounds.getMinX()).startY(bounds.getMinY()).width(bounds.getWidth() - 1)
                .height(bounds.getHeight() - 1).drawRect(model1.getImage(), model.getBackColor());
        }
        imageSelected = null;
        if (model.getImageStack().getChildren().contains(getArea())) {
            model.getImageStack().getChildren().remove(getArea());
        }
        model.createImageVersion();
    }

    private boolean handleControlDown(PaintModel model, KeyCode code) {
        switch (code) {
            case C:
                copyToClipboard(model.getImage());
                return true;
            case V:
                onDeselected(model);
                copyFromClipboard(model);
                return true;
            case X:
                cutImage(model);
                return true;
            case A:
                onDeselected(model);
                selectArea(0, 0, (int) model.getImage().getWidth(), (int) model.getImage().getHeight(), model);
                return true;
            default:
                return false;
        }
    }

    private boolean handleDeleteEscape(KeyEvent e, PaintModel model) {
        KeyCode code = e.getCode();
        if (code == KeyCode.DELETE) {
            deleteImage(model, getArea().getBoundsInParent());
            return true;
        }
        if (code == KeyCode.ESCAPE) {
            escapeArea(model);
            return true;
        }
        if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.PLUS) {
            resize(1.05);
            return true;
        }
        if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) {
            resize(0.95);
            return true;
        }
        return false;

    }

    private void onChangeOption(Toggle newV, PaintModel model) {
        SelectOption oldOption = option;
        option = newV == null ? SelectOption.OPAQUE : (SelectOption) newV.getUserData();
        if (oldOption != option && imageSelected != null) {
            if (option == SelectOption.OPAQUE) {
                replaceColor(imageSelected, Color.TRANSPARENT, model.getBackColor());
            } else {
                replaceColor(imageSelected, model.getBackColor(), Color.TRANSPARENT.invert());
            }
        }
    }

    private void resize(double scale) {
        long newWidth = Math.round(imageSelected.getWidth() * scale);
        long newHeight = Math.round(imageSelected.getHeight() * scale);
        if (newWidth == 0 || newHeight == 0) {
            return;
        }

        WritableImage newImage = RectBuilder.resizeImage(imageSelected, newWidth, newHeight);
        getArea().setWidth(newWidth);
        getArea().setHeight(newHeight);
        getArea().setFill(new ImagePattern(newImage));
        imageSelected = newImage;
    }

}
