package paintexp.tool;

import static utils.DrawOnPoint.getWithinRange;
import static utils.PixelHelper.replaceColor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import paintexp.PaintModel;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.HasLogging;

public class SelectRectTool extends PaintTool {

    private static final Logger LOG = HasLogging.log();
    private Rectangle icon;
    private Rectangle area;
    protected WritableImage imageSelected;
    protected double initialX;
    protected double initialY;
    private double dragX;
    private double dragY;
    protected SelectOption option = SelectOption.OPAQUE;

    public void copyFromClipboard(PaintModel model) {
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

    public void copyToClipboard(WritableImage image) {
        if (imageSelected == null) {
            double width = area.getWidth();
            double height = area.getHeight();
            imageSelected = new WritableImage((int) width, (int) height);
            int layoutX = (int) area.getLayoutX();
            int layoutY = (int) area.getLayoutY();
            int maxWidth = (int) image.getWidth();
            int maxHeight = (int) image.getHeight();
            BoundingBox bounds = new BoundingBox(Integer.min(Integer.max(layoutX, 0), maxWidth),
                Integer.min(Integer.max(layoutY, 0), maxHeight), width, height);
            copyImagePart(image, imageSelected, bounds);
        }
        Map<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.IMAGE, imageSelected);
        Clipboard.getSystemClipboard().setContent(content);
    }

    public WritableImage createSelectedImage(PaintModel model) {
        WritableImage srcImage = model.getImage();
        return createSelectedImage(model, srcImage);
    }

    public Rectangle getArea() {
        if (area == null) {
            area = new SimpleRectangleBuilder().fill(Color.TRANSPARENT).stroke(Color.BLACK).cursor(Cursor.MOVE)
                .managed(false).strokeDashArray(1, 2, 1, 2).build();
        }
        return area;
    }

    @Override
    public Node getIcon() {
        if (icon == null) {
            icon = new SimpleRectangleBuilder().width(10).height(10).fill(Color.TRANSPARENT).stroke(Color.BLUE)
                .strokeDashArray(1, 2, 1, 2).build();
        }
        return icon;
    }

    @Override
    public Cursor getMouseCursor() {
        return Cursor.CROSSHAIR;
    }

    @Override
    public void handleEvent(MouseEvent e, PaintModel model) {
        simpleHandleEvent(e, model);
    }

    @Override
    public void handleKeyEvent(KeyEvent e, PaintModel model) {
        KeyCode code = e.getCode();
        Bounds bounds = getArea().getBoundsInParent();
        switch (code) {
            case DELETE:
                if (e.getEventType() == KeyEvent.KEY_RELEASED) {
                    deleteImage(model, bounds);
                }
                break;
            case ESCAPE:
                escapeArea(model);
                break;
            case V:
                if (e.isControlDown()) {
                    copyFromClipboard(model);
                }
                break;
            case C:
                if (e.isControlDown()) {
                    copyToClipboard(model.getImage());
                }
                break;
            case X:
                if (e.isControlDown()) {
                    cutImage(model, bounds);
                }
                break;
            case A:
                if (e.isControlDown()) {
                    selectArea(0, 0, (int) model.getImage().getWidth(), (int) model.getImage().getHeight(), model);
                }
                break;
            case RIGHT:
            case LEFT:
            case DOWN:
            case UP:
                RectBuilder.moveArea(code, getArea());
                break;
            default:
                break;
        }
    }

    @Override
    public void onDeselected(PaintModel model) {
        escapeArea(model);
    }

    @Override
    public void onSelected(PaintModel model) {
        model.getToolOptions().getChildren().clear();
        int size = 30;
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
            .addToggle(getIconByURL("opaqueSelection.png", size), SelectOption.OPAQUE)
            .addToggle(getIconByURL("transparentSelection.png", size), SelectOption.TRANSPARENT)
            .onChange((ob, old, newV) -> onChangeOption(newV, model)).select(option).getTogglesAs(Node.class);

        model.getToolOptions().getChildren().addAll(togglesAs);

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
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (!children.contains(getArea())) {
            children.add(getArea());
        }
        area.setStroke(Color.BLACK);
        getArea().setManaged(false);
        getArea().setFill(Color.TRANSPARENT);
        getArea().setLayoutX(initialX);
        getArea().setLayoutY(initialY);
        getArea().setWidth(1);
        getArea().setHeight(1);
    }

    protected void copyImage(PaintModel model, Image srcImage, WritableImage destImage) {
        Color backColor = model.getBackColor();
        double width = srcImage.getWidth();
        double height = srcImage.getHeight();
        if (destImage == null) {
            imageSelected = new WritableImage((int) width, (int) height);
        }
        WritableImage writableImage = destImage != null ? destImage : imageSelected;
        new RectBuilder().width(width).height(height).copyImagePart(srcImage, writableImage, backColor);
        if (option == SelectOption.TRANSPARENT) {
            replaceColor(writableImage, backColor, Color.TRANSPARENT);
        }

        selectArea(0, 0, (int) srcImage.getWidth(), (int) srcImage.getHeight(), model);
    }

    @Override
    protected void onMouseDragged(MouseEvent e, PaintModel model) {
        double x = e.getX();
        double y = e.getY();
        double width = model.getImage().getWidth();
        double height = model.getImage().getHeight();
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea()) && imageSelected != null) {
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
        ObservableList<Node> children = model.getImageStack().getChildren();

        if (children.contains(getArea())) {
            if (containsPoint(getArea(), e.getX(), e.getY())) {
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
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (getArea().getWidth() < 2 && children.contains(getArea()) && imageSelected != null) {
            children.remove(getArea());
        }
        area.setStroke(Color.BLUE);
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
        new RectBuilder().width(width).height(height).endX(x).endY(y).copyImagePart(imageSelected, model.getImage(),
            Color.TRANSPARENT);
        imageSelected = null;
        model.getImageStack().getChildren().remove(getArea());
        model.createImageVersion();
        model.getScrollPane().setHvalue(hvalue);
        model.getScrollPane().setVvalue(vvalue);
    }

    private void copyFromFile(PaintModel model, List<File> files) {
        if (!files.isEmpty()) {
            File file = files.get(0);
            try {
                Image image2 = new Image(file.toURI().toURL().toExternalForm());
                copyImage(model, image2, imageSelected);
            } catch (Exception e1) {
                LOG.error("", e1);
            }
        }
    }

    private WritableImage createSelectedImage(PaintModel model, WritableImage srcImage) {
        if (imageSelected == null) {
            int width = (int) area.getWidth();
            int height = (int) area.getHeight();
            imageSelected = new WritableImage(width, height);
            int layoutX = (int) area.getLayoutX();
            int layoutY = (int) area.getLayoutY();
            BoundingBox bounds = new BoundingBox(layoutX, layoutY, width, height);
            copyImagePart(srcImage, imageSelected, bounds);
            if (option == SelectOption.TRANSPARENT) {
                replaceColor(imageSelected, model.getBackColor(), Color.TRANSPARENT.invert());
            }

            getArea().setFill(new ImagePattern(imageSelected));
            new RectBuilder().startX(layoutX).startY(layoutY).width(width).height(height).drawRect(model,
                model.getBackColor());
        }
        return imageSelected;
    }

    private void cutImage(PaintModel model, Bounds bounds) {
        copyToClipboard(model.getImage());
        new RectBuilder().startX(bounds.getMinX()).startY(bounds.getMinY()).width(bounds.getWidth() - 1)
            .height(bounds.getHeight() - 1).drawRect(model, model.getBackColor());
        model.createImageVersion();
    }

    private void deleteImage(PaintModel model, Bounds bounds) {
        if (imageSelected == null) {
            new RectBuilder().startX(bounds.getMinX()).startY(bounds.getMinY()).width(bounds.getWidth() - 1)
                .height(bounds.getHeight() - 1).drawRect(model, model.getBackColor());
        }
        imageSelected = null;
        ObservableList<Node> children = model.getImageStack().getChildren();
        if (children.contains(getArea())) {
            children.remove(getArea());
        }
        model.createImageVersion();
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

    enum SelectOption {
        TRANSPARENT,
        OPAQUE;
    }

}