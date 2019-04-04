package utils;

import java.util.stream.Stream;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public final class ImageTableCell extends TableCell<HasImage, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        if (item == getItem()) {
            return;
        }
        super.updateItem(item, empty);
        if (item == null) {
            super.setText(null);
            super.setGraphic(null);
        } else {
            super.setGraphic(new VBox(Stream.of(item.split(";")).map(image -> {
                String imageUrl = ResourceFXUtils.toExternalForm("out/" + image);
                ImageView imageView = new ImageView(imageUrl);
                imageView.fitWidthProperty().bind(super.widthProperty());
                imageView.setPreserveRatio(true);
                return imageView;
            }).toArray(ImageView[]::new)));
        }
    }
}