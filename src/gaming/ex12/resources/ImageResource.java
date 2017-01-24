package gaming.ex12.resources;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
public enum ImageResource {
	LEOPARD("cat2.png", 512, 256, 0.5, 8, 4),
	BIRD("bird.png", 184, 184, 1, 9, 5),
	DOG("dog.png", 184, 100, 1, 9, 3),
	TREE("tree.png", 1200, 1200, 0.25),
	GROUND("ground.png", 118, 75, 1, 10, 5),
	TILES("tiles.png", 72, 72, 1, 156, 13),
	JUNGLE("jungle.png", 900, 506, 1);
	private final String filename;
	private final int width;
	private final int height;
	private double scale = 1;
	private int count = 1;
	private int columns = 1;
	private Image image;

	private ImageResource(String file, int width, int height, double scale) {
		filename = file;
		this.width = width;
		this.height = height;
		this.scale = scale;

	}

	private ImageResource(String file, int width, int height, double scale, int count, int columns) {
		filename = file;
		this.height = height;
		this.width = width;
		this.scale = scale;
		this.count = count;
		this.columns = columns;
	}

	public String getFilename() {
		return filename;
	}

	public int getCount() {
		return count;
	}

	public int getColumns() {
		return columns;
	}

	public Image asImage() {
		if (image == null) {
			image = new Image(getClass().getResourceAsStream(filename));
		}
		return image;
	}

	public ImageView asImageView() {

		ImageView imageView = new ImageView(asImage());
		imageView.setScaleX(scale);
		imageView.setScaleY(scale);
		imageView.setViewport(new Rectangle2D(0, 0, width * scale, height * scale));
		return imageView;
	}

	public int getWidth() {
		return width;
	}

	public double getScaledWidth() {
		return width * scale;
	}

	public double getScaledHeight() {
		return height * scale;
	}
	public int getHeight() {
		return height;
	}
}
