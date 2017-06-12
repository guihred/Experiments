package physics;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

/**
 *
 * @author wayne
 */
public class PhysicalScene extends Scene {
    
	protected static World world = new World(new Vec2(0, -9.86F));
    
    public PhysicalScene(Parent parent, double d, double d1, boolean bln) {
        super(parent, d, d1, bln);
    }

    public PhysicalScene(Parent parent, double d, double d1, Paint paint) {
        super(parent, d, d1, paint);
    }

    public PhysicalScene(Parent parent, Paint paint) {
        super(parent, paint);
    }

    public PhysicalScene(Parent parent, double d, double d1) {
        super(parent, d, d1);
    }

    public PhysicalScene(Parent parent) {
        super(parent);
    }
    
    
    
    public Body createBody(BodyDef bd) {
        return world.createBody(bd);
    }
    
    
}
