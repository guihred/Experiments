package physics;

import javafx.scene.Node;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author wayne
 */
public abstract class BasePhysicalObject {

	protected Node node;
	protected Body body;
    public static final int WIDTH = 600;
    public static final int HEIGHT = 600;

	public abstract Node create();

	public abstract Body createBody();

    public final void build() {
		node = create();
		body = createBody();
		node.setUserData(body);
	}

    public static float toPixelWidth(float worldWidth) {
        return BasePhysicalObject.WIDTH * worldWidth / 100.0F;
    }

    public static float toPixelHeight(float worldHeight) {
        return BasePhysicalObject.HEIGHT * worldHeight / 100.0F;
    }

    public static int toPixelY(float worldY) {
        float y = BasePhysicalObject.HEIGHT - 1.0F * BasePhysicalObject.HEIGHT * worldY / 100.0F;
        return (int) y;
    }

    /*
     * JavaFX Coordinates: (0,0) --> (WIDTH,HEIGHT) in pixels World Coordinates:
     * (0,100) --> (100, 0) in meters
     */
    public static int toPixelX(float worldX) {
        float x = BasePhysicalObject.WIDTH * worldX / 100.0F;
        return (int) x;
    }

}
