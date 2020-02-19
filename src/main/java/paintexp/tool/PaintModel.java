package paintexp.tool;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import utils.*;

public class PaintModel {
    private static final int MAX_VERSIONS = 50;
    private final ObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.BLACK);
    private WritableImage image = new WritableImage(500, 500);
    private Group imageStack;
    private Text imageSize = new Text();
    private Text toolSize = new Text();
    private Text mousePosition = new Text();
    private VBox toolOptions;
    private final ObjectProperty<File> currentFile = new SimpleObjectProperty<>();
    private final StringProperty filename = filenameProperty();
    private final ObservableList<WritableImage> imageVersions = FXCollections.observableArrayList();
    private final IntegerProperty currentVersion = new SimpleIntegerProperty(0);
    private PixelatedImageView rectangleBorder;
    private ZoomableScrollPane scrollPane;

    public ObjectProperty<Color> backColorProperty() {
        return backColor;
    }

    public void createImageVersion() {

        WritableImage e = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        if (imageVersions.isEmpty() || !PaintToolHelper.isEqualImage(e, getCurrentImage())) {
            int clamp = ResourceFXUtils.clamp(getCurrentVersion() + 1, 0, Math.max(imageVersions.size(), 0));
            if (imageVersions.size() > clamp) {
                for (int i = clamp; i < imageVersions.size();) {
                    imageVersions.remove(i);
                }
            }

            imageVersions.add(clamp, e);
            currentVersion.set(imageVersions.size() - 1);
        }
        if (imageVersions.size() > MAX_VERSIONS) {
            imageVersions.remove(0);
            currentVersion.set(currentVersion.get() - 1);
        }

    }

    public int decrementCurrentVersion() {
        currentVersion.set(ResourceFXUtils.clamp(getCurrentVersion() - 1, 0, Math.max(imageVersions.size() - 1, 0)));
        return currentVersion.get();
    }

    public StringProperty filenameProperty() {
        return SupplierEx.orElse(filename, () -> {
            StringProperty file = new SimpleStringProperty();

            file.bind(Bindings.createStringBinding(() -> currentFile.isNull().get() ? String.format("Paint")
                : String.format("Paint (%s)", currentFile.get().getName()), currentFile));
            return file;
        });
    }

    public ObjectProperty<Color> frontColorProperty() {
        return frontColor;
    }

    public Color getBackColor() {
        return backColor.get();
    }

    public File getCurrentFile() {
        return currentFile.get();
    }

    public WritableImage getCurrentImage() {
        return imageVersions.get(ResourceFXUtils.clamp(getCurrentVersion(), 0, Math.max(imageVersions.size() - 1, 0)));
    }

    public int getCurrentVersion() {
        return currentVersion.get();
    }

    public Color getFrontColor() {
        return frontColor.get();
    }

    public WritableImage getImage() {
        return image;
    }

    public Text getImageSize() {
        return imageSize;
    }

    public Group getImageStack() {
        if (imageStack == null) {
            ImageView imageView = new PixelatedImageView(getImage());
            imageView.setLayoutX(0);
            imageView.setLayoutY(0);
            imageView.setManaged(false);
            imageView.setSmooth(true);

            imageStack = new Group(getRectangleBorder(imageView), imageView);
            imageStack.setManaged(false);
            imageStack.setLayoutX(0);
            imageStack.setLayoutY(0);
        }

        return imageStack;
    }

    public ObservableList<WritableImage> getImageVersions() {
        return imageVersions;
    }

    public Text getMousePosition() {
        return mousePosition;
    }

    public PixelatedImageView getRectangleBorder(final ImageView imageView) {
        if (rectangleBorder == null) {
            WritableImage pattern = DrawOnPoint.drawTransparentPattern(500);

            rectangleBorder = new PixelatedImageView(pattern);
            rectangleBorder.setManaged(false);
        }
        rectangleBorder.layoutXProperty().bind(imageView.layoutXProperty());
        rectangleBorder.layoutYProperty().bind(imageView.layoutYProperty());
        rectangleBorder.fitWidthProperty().bind(image.widthProperty().add(1));
        rectangleBorder.fitHeightProperty().bind(image.heightProperty().add(1));
        return rectangleBorder;
    }

    public ZoomableScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new ZoomableScrollPane(getImageStack());
        }

        return scrollPane;
    }

    public VBox getToolOptions() {
        if (toolOptions == null) {
            toolOptions = new VBox(10);
            toolOptions.setAlignment(Pos.CENTER);
            toolOptions.setId("tools");
            resetToolOptions();
        }
        return toolOptions;
    }

    public Text getToolSize() {
        return toolSize;
    }

    public int incrementCurrentVersion() {
        currentVersion.set(ResourceFXUtils.clamp(getCurrentVersion() + 1, 0, Math.max(imageVersions.size() - 1, 0)));
        return currentVersion.get();
    }

    public Rectangle resetToolOptions() {
        Rectangle rectangle = new Rectangle(50, 50, Color.TRANSPARENT);
        rectangle.setStroke(Color.GRAY);
        final int maxWidth = 150;
        toolOptions.setMaxWidth(maxWidth);
        toolOptions.setSpacing(10);
        toolOptions.getChildren().clear();
        toolOptions.getChildren().add(rectangle);
        return rectangle;
    }

    public void setBackColor(final Color backColor) {
        this.backColor.set(backColor);
    }

    public void setCurrentFile(final File currentFile) {
        this.currentFile.set(currentFile);
    }

    public void setFrontColor(final Color frontColor) {
        this.frontColor.set(frontColor);
    }

    public void setImage(final WritableImage image) {
        this.image = image;
    }

    public void setImageSize(final Text imageSize) {
        this.imageSize = imageSize;
    }

    public void setMousePosition(final Text mousePosition) {
        this.mousePosition = mousePosition;
    }

    public void setToolSize(final Text toolSize) {
        this.toolSize = toolSize;
    }

    public void takeSnapshot(Node line2) {
        ImageView imageView = new PixelatedImageView(image);
        createImageVersion();
        RectBuilder.takeSnapshot(line2, image, getImageStack(), imageView, getRectangleBorder(imageView),
            getCurrentImage());
    }

    public void takeSnapshotFill(Node line2) {
        ImageView imageView = new PixelatedImageView(image);
        RectBuilder.takeSnapshotFill(line2, image, getImageStack(), imageView, getRectangleBorder(imageView));
    }

}
