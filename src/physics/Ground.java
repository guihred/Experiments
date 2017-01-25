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
public class Ground extends PhysicalObject {
    
    public Ground() {
        build();
    }
    
    @Override
    public Node create() {
        Rectangle rect = new Rectangle(0,Physics.toPixelY(1),Physics.toPixelX(100),Physics.toPixelY(0)-Physics.toPixelY(1));
        rect.setFill(Color.BLACK);
        return rect;
    }

    @Override
    public Body createBody() {
                // ground
        FixtureDef fd = new FixtureDef();
        PolygonShape sd = new PolygonShape();
        
        sd.setAsBox(200.0f, 11.0f);
        fd.shape = sd;

        BodyDef bd = new BodyDef();
        bd.position = new Vec2(0.0f,-10.0f);
        Body bb = PhysicalScene.world.createBody(bd);
        bb.createFixture(fd);
        
        return bb;
    }
    
    
}
