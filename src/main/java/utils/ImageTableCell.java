package utils;

import static utils.FunctionEx.makeFunction;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;

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

    public static ImageView newImage(String image, ReadOnlyDoubleProperty widthProperty) {
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

    private static String getImageLink(String image) {
        if (image.startsWith("http")) {
            return image;
        }
		try {
			return ResourceFXUtils.toExternalForm("out/" + image);
		} catch (Exception e) {
            LOG.trace("", e);
		}
        if (image.startsWith("C:")) {
            return makeFunction((String e) -> ResourceFXUtils.convertToURL(new File(e)).toString()).apply(image);
        }

        return SupplierEx.getIgnore(() -> ResourceFXUtils.toExternalForm("out/pdf/" + image));
    }
}