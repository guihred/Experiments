package gaming.ex21;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import utils.ResourceFXUtils;

public abstract class CatanResource extends Group {

    public static final String CATAN = "catan/";
    public static final int RADIUS = 70;
    private final ObjectProperty<PlayerColor> player = new SimpleObjectProperty<>();
    private final Image image;
    protected final ImageView view;

    public CatanResource(final String url) {
        image = new Image(ResourceFXUtils.toExternalForm(CATAN + url));
        view = new ImageView(image);
        view.setPreserveRatio(true);
        getChildren().add(view);
        setManaged(false);
        player.addListener(
            (obj, old, newV) -> view.setImage(convertImage(image, newV != null ? newV.getColor() : Color.BLACK)));
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

	public final void setPlayer(final PlayerColor color) {
        player.set(color);
    }

    public static WritableImage convertImage(final Image image, final Color color) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        WritableImage writableImage = new WritableImage(w, h);
        PixelChanger reader = new PixelChanger(image.getPixelReader());
        reader.put(Color.BLACK, color);
        reader.put(Color.GRAY, color.darker());
        writableImage.getPixelWriter().setPixels(0, 0, w, h, reader, 0, 0);
        return writableImage;
    }

    public static ImageView newImage(Image url, double width) {
        ImageView newImage = new ImageView(url);
        newImage.setPreserveRatio(true);
        newImage.setFitWidth(width);
        return newImage;
    }

    public static ImageView newImage(String url) {
        ImageView view = new ImageView(ResourceFXUtils.toExternalForm(CATAN + url));
        view.setPreserveRatio(true);
        return view;
    }

    public static WritableImage newImage(String url, Color color) {
        return CatanResource.convertImage(new Image(ResourceFXUtils.toExternalForm(CATAN + url)), color);
    }

    public static ImageView newImage(String url, Color color, int width) {
        ImageView newImage = new ImageView(newImage(url, color));
        newImage.setFitWidth(width);
        newImage.setPreserveRatio(true);
        return newImage;
    }

    public static ImageView newImage(String url, double width) {
        ImageView newImage = new ImageView(ResourceFXUtils.toExternalForm(CATAN + url));
        newImage.setPreserveRatio(true);
        newImage.setFitWidth(width);
        return newImage;
    }

    public static ImageView newImage(String url, double width, int height) {
        ImageView view = new ImageView(ResourceFXUtils.toExternalForm(CATAN + url));
        view.setPreserveRatio(true);
        view.setFitWidth(width);
        view.setFitHeight(height);
        return view;
    }

    public static ImagePattern newPattern(String terrain) {
        return new ImagePattern(new Image(ResourceFXUtils.toExternalForm(CATAN + terrain)));
    }
}
