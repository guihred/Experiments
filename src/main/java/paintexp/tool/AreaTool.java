package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;
import static utils.PixelHelper.replaceColor;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import simplebuilder.SimpleRectangleBuilder;
import utils.CommonsFX;
import utils.ImageFXUtils;
import utils.RectBuilder;

public abstract class AreaTool extends PaintTool {
    protected WritableImage imageSelected;
    protected double initialX;
    private double dragX;
    private double dragY;
    protected double initialY;
    protected SelectOption option = SelectOption.TRANSPARENT;
    private Rectangle area;

    public final WritableImage copyToClipboard(WritableImage image) {
        if (imageSelected == null) {
            double width = Math.max(1, getArea().getWidth());
            double height = Math.max(1, getArea().getHeight());
            imageSelected = new WritableImage((int) width, (int) height);
            RectBuilder.copyImagePart(image, imageSelected, getArea());
        }
        ImageFXUtils.setClipboardContent(imageSelected);
        return imageSelected;
    }

    public WritableImage createSelectedImage(PaintModel model) {
        WritableImage srcImage = model.getImage();
        return createSelectedImage(model, srcImage);
    }



    public void deleteImage(PaintModel model, Bounds bounds) {
        if (imageSelected == null) {
            RectBuilder.build().startX(bounds.getMinX()).startY(bounds.getMinY()).width(bounds.getWidth() - 1)
                .height(bounds.getHeight() - 1).drawRect(model.getImage(), model.getBackColor());
        }
        imageSelected = null;
        if (model.getImageStack().getChildren().contains(getArea())) {
            model.getImageStack().getChildren().remove(getArea());
        }
        model.createImageVersion();
    }

    public Rectangle getArea() {
        if (area == null) {
            area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK).cursor(Cursor.MOVE)
                .managed(false).strokeDashArray(1, 2, 1, 2).build();
        }
        return area;
    }

    public WritableImage getImageSelected() {
        return imageSelected;
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
        }
    }

    @Override
    public void onDeselected(PaintModel model) {
        escapeArea(model);
    }

    @Override
    public void onMouseDragged(MouseEvent e, PaintModel model) {
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
    public void onMousePressed(MouseEvent e, PaintModel model) {
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
    }

    @Override
    public void onMouseReleased(PaintModel model) {
        if (getArea().getWidth() < 2 && model.getImageStack().getChildren().contains(getArea())
            && imageSelected != null) {
            model.getImageStack().getChildren().remove(getArea());
        } else {
            createSelectedImage(model);
        }
        getArea().setStroke(Color.BLUE);
    }

    @Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();

        model.getToolOptions().getChildren()
            .addAll(SelectOption.createSelectOptions((ob, old, newV) -> onChangeOption(newV, model), option));

    }

    public final WritableImage pasteFromClipboard(PaintModel model) {
        Image image = ImageFXUtils.getClipboardImage();
        if (image != null) {
            copyImage(model, image, imageSelected);
        }
        if (imageSelected != null) {
            getArea().setFill(new ImagePattern(imageSelected));
        }
        return imageSelected;
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
        getArea().setScaleX(1);
        getArea().setScaleY(1);
        getArea().setHeight(1);
    }

    protected void setIntoImage(PaintModel model) {
        int x = (int) getArea().getLayoutX();
        int y = (int) getArea().getLayoutY();
        double width = imageSelected.getWidth();
        double height = imageSelected.getHeight();
        double newWidth = width * getArea().getScaleX();
        double newHeight = height * getArea().getScaleY();
        double offX = (newWidth - width) / 2;   
        double offY = (newHeight - height) / 2;

        WritableImage newImage = RectBuilder.resizeImage(imageSelected, newWidth, newHeight);
        RectBuilder.build().width(newWidth).height(newHeight).endX(x - offX).endY(y - offY).copyImagePartTransparency(
                newImage,
            model.getImage(),
                model.getImage());
        imageSelected = null;
        model.getImageStack().getChildren().remove(getArea());
        model.createImageVersion();
    }

    private final void copyImage(PaintModel model, Image srcImage, WritableImage destImage) {
        Color backColor = model.getBackColor();
        double width = Math.max(srcImage.getWidth(), 1);
        double height = Math.max(srcImage.getHeight(), 1);
        if (destImage == null) {
            imageSelected = new WritableImage((int) width, (int) height);
        }
        WritableImage writableImage = destImage != null ? destImage : imageSelected;
        RectBuilder.build().width(width).height(height).copyImagePart(srcImage, writableImage, backColor);
        if (option == SelectOption.TRANSPARENT) {
            replaceColor(writableImage, backColor, Color.TRANSPARENT);
        }
        double hvalue = model.getScrollPane().getHvalue();
        double vvalue = model.getScrollPane().getVvalue();
        double hv = Math.max(hvalue * model.getCurrentImage().getWidth() - srcImage.getWidth(), 0);
        double vv = Math.max(vvalue * model.getCurrentImage().getHeight() - srcImage.getHeight(), 0);
        int x = (int) hv;
        int y = (int) vv;
        selectArea(x, y, x + (int) srcImage.getWidth(), y + (int) srcImage.getHeight(), model);
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
                imageSelected,
                model.getBackColor());
        }
        return imageSelected;
    }

    private void dragTo(double x, double y) {
        getArea().setLayoutX(Math.min(x, initialX));
        getArea().setLayoutY(Math.min(y, initialY));
        getArea().setWidth(Math.abs(x - initialX));
        getArea().setHeight(Math.abs(y - initialY));
    }

    private void escapeArea(PaintModel model) {
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
            resize(1. / 20);
            return true;
        }
        if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) {
            resize(-1. / 20);
            return true;
        }
        return false;

    }

    private void onChangeOption(Toggle newV, PaintModel model) {
        SelectOption oldOption = option;
        option = newV == null ? SelectOption.TRANSPARENT : (SelectOption) newV.getUserData();
        if (oldOption != option && imageSelected != null) {
            if (option == SelectOption.OPAQUE) {
                replaceColor(imageSelected, Color.TRANSPARENT, model.getBackColor());
            } else {
                replaceColor(imageSelected, model.getBackColor(), Color.TRANSPARENT.invert());
            }
        }
    }

    private void resize(double scale) {
        if (imageSelected == null) {
            return;
        }
        double newScale = getArea().getScaleX() + scale;
        long newWidth = Math.round(imageSelected.getWidth() * newScale);
        long newHeight = Math.round(imageSelected.getHeight() * newScale);
        if (newWidth == 0 || newHeight == 0) {
            return;
        }

        getArea().setScaleX(newScale);
        getArea().setScaleY(newScale);
        getArea().setFill(new ImagePattern(imageSelected));
    }

}
