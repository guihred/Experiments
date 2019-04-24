package physics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;

/**
 *
 * @author wyoung
 */
public class Ground extends BasePhysicalObject {
    
    public Ground() {
        build();
    }
    
    @Override
    public Node create() {
        Rectangle rect = new Rectangle(0, Physics.toPixelY(1), Physics.toPixelX(100),
            Physics.toPixelY(0) - Physics.toPixelY(1));
        rect.setFill(Color.BLACK);
        return rect;
    }

    @Override
    public Body createBody() {
                // ground
        FixtureDef fd = new FixtureDef();
        PolygonShape sd = new PolygonShape();
        
        final float hx = 200.0F;
        sd.setAsBox(hx, 11.0F);
        fd.shape = sd;

        BodyDef bd = new BodyDef();
		bd.position = new Vec2(0.0F, -10.0F);
        Body bb = PhysicalScene.getWorld().createBody(bd);
        bb.createFixture(fd);
        
        return bb;
    }
    
    
}
