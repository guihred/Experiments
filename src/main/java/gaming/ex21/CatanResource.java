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
    public CatanResource(final String url) {
        image = new Image(ResourceFXUtils.toExternalForm(url));
        view = new ImageView(image);
        view.setPreserveRatio(true);
        getChildren().add(view);
        setManaged(false);
		player.addListener(
				(ob, old, newV) -> view.setImage(convertImage(image, newV != null ? newV.getColor() : Color.BLACK)));
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

    public void setPlayer(final PlayerColor color) {
        player.set(color);
    }
    public static WritableImage convertImage(final Image image,final Color color) {
		int w = (int) image.getWidth();
		int h = (int) image.getHeight();
		WritableImage writableImage = new WritableImage(w, h);
		PixelChanger reader = new PixelChanger(image.getPixelReader());
		reader.put(Color.BLACK, color);
		reader.put(Color.GRAY, color.darker());
		writableImage.getPixelWriter().setPixels(0, 0, w, h, reader, 0, 0);
		return writableImage;
	}

}
