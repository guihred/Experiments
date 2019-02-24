package gaming.ex21;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import utils.ResourceFXUtils;

public abstract class CatanResource extends Group {

    private final ObjectProperty<PlayerColor> player = new SimpleObjectProperty<>();
    private final Image image;
    protected final ImageView view;
    public CatanResource(String url) {
        image = new Image(ResourceFXUtils.toExternalForm(url));
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        view = new ImageView(image);
        view.setPreserveRatio(true);
        getChildren().add(view);
        setManaged(false);
        player.addListener((ob, old, newV) -> {
            WritableImage writableImage = new WritableImage(w, h);

            PixelChanger reader = new PixelChanger(image.getPixelReader());
            if (newV != null) {
                reader.put(Color.BLACK, newV.getColor());
                reader.put(Color.GRAY, newV.getColor().darker());
            }
            writableImage.getPixelWriter().setPixels(0, 0, w, h, reader, 0, 0);
            view.setImage(writableImage);
        });
    }

    public Bounds getImage() {
        return view.getBoundsInParent();
    }

    public PlayerColor getPlayer() {
        return player.get();
    }

    public ObjectProperty<PlayerColor> playerProperty() {
        return player;
    }
    public void setPlayer(PlayerColor color) {
        player.set(color);
    }

}
