package physics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import simplebuilder.SimpleCircleBuilder;

/**
 *
 * @author wayne
 */
public class Ball extends BasePhysicalObject {
    
    private float centerX;
    private float centerY;
    public Ball(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        build();
    }

    @Override
    public Node create() {
		return new SimpleCircleBuilder()
				.radius(10)
				.stroke(Color.RED)
				.layoutX(Physics.toPixelX(centerX))
				.layoutY(Physics.toPixelY(centerY))
				.build();
    }

    @Override
    public Body createBody() {
        
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(centerX,centerY);
        
        CircleShape cs = new CircleShape();
		cs.m_radius = 1.0F;
        
        FixtureDef fd = new FixtureDef();
        fd.shape = cs;
        fd.density = 9F / 10;
        fd.friction = 3F / 10;
		fd.restitution = 0.5F;
        
        Body b = PhysicalScene.world.createBody(bd); 
        
        b.createFixture(fd);        
        
        return b;
    }

  
    
}
