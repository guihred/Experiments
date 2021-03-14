package utils.fx;

import static utils.ex.FunctionEx.makeFunction;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class ImageTableCell<T> extends TableCell<T, String> {

    private static final Logger LOG = HasLogging.log();

    public ImageTableCell() {
    }

    public ImageTableCell(TableColumn<T, String> col) {
        updateTableColumn(col);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        if (item == getItem()) {
            return;
        }
        super.updateItem(item, empty);
        if (item == null || empty) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            super.setText(item);
            super.setGraphic(new VBox(Stream.of(item.split(";")).map(this::getImageView).toArray(ImageView[]::new)));
        }
    }

    private ImageView getImageView(String image) {
        return newImage(image, super.widthProperty());
    }

    public static List<ImageView> createImages(String item, ReadOnlyDoubleProperty widthProperty) {
        return Stream.of(item.split(";")).map(e -> ImageTableCell.newImage(e, widthProperty))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static List<ImageView> createImagesMaxWidth(String item, ReadOnlyDoubleProperty maxProperty) {
        List<Image> images = Stream.of(item.split(";")).map(ImageTableCell::getImageLink).filter(Objects::nonNull)
                .map(Image::new).filter(Objects::nonNull)
                .sorted(Comparator.comparing(e -> -e.getWidth() * e.getHeight())).collect(Collectors.toList());
        double maxWidth = images.stream().mapToDouble(Image::getWidth).max().orElse(1);
        return images.stream().map(image -> {
            ImageView imageView = new ImageView(image);
            imageView.fitWidthProperty().bind(maxProperty.multiply(image.getWidth() / maxWidth));
            imageView.setPreserveRatio(true);
            return imageView;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static String getImageLink(String image) {
        if (image.startsWith("http")) {
            return image;
        }
		try {
            return ResourceFXUtils.convertToURL(ResourceFXUtils.getOutFile(image)).toExternalForm();
		} catch (Exception e) {
            LOG.trace("", e);
		}
        if (image.startsWith("C:")) {
            return makeFunction((String e) -> ResourceFXUtils.convertToURL(new File(e)).toString()).apply(image);
        }

        return SupplierEx.getIgnore(
                () -> ResourceFXUtils.convertToURL(ResourceFXUtils.getOutFile("pdf/" + image)).toExternalForm());
    }

    private static ImageView newImage(String image, ReadOnlyDoubleProperty widthProperty) {
        String imageUrl = getImageLink(image);
        if (imageUrl == null) {
            LOG.info("Image not found {}", image);
            return null;
        }
        ImageView imageView = new ImageView(imageUrl);
        imageView.fitWidthProperty().bind(widthProperty);
        imageView.setPreserveRatio(true);
        return imageView;
    }
}