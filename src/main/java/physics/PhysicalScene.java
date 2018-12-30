package physics;

import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author wayne
 */
public class PhysicalScene extends Scene {

    private static World world;

    private static final float GRAVITY = 9.86F;

    public PhysicalScene(Parent parent, double d, double d1) {
        super(parent, d, d1);
    }

    public Body createBody(BodyDef bd) {
        return world.createBody(bd);
    }

    public static World getWorld() {
        if (world == null) {
            world = new World(new Vec2(0, -GRAVITY));
        }
        return world;
    }

}
