package utils;

import java.util.stream.Stream;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public final class ImageTableCell<T> extends TableCell<T, String> {

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
        String imageUrl = image.startsWith("http") ? image : ResourceFXUtils.toExternalForm("out/" + image);
        ImageView imageView = new ImageView(imageUrl);
        imageView.fitWidthProperty().bind(super.widthProperty());
        imageView.setPreserveRatio(true);
        return imageView;
    }
}