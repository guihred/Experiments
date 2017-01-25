package physics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

/**
 *
 * @author wayne
 */
public class Ball extends PhysicalObject {
    
    private float centerX;
    private float centerY;
    public Ball(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        build();
    }

    @Override
    public Node create() {
        Circle c = new Circle();
        c.setRadius(10);
        c.setStroke(Color.RED);
        c.setLayoutX(Physics.toPixelX(centerX));
        c.setLayoutY(Physics.toPixelY(centerY));
        return c;
    }

    @Override
    public Body createBody() {
        
        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(centerX,centerY);
        
        CircleShape cs = new CircleShape();
        cs.m_radius = 1.0f;
        
        FixtureDef fd = new FixtureDef();
        fd.shape = cs;
        fd.density = 0.9f;
        fd.friction = 0.3f;        
        fd.restitution = 0.5f;
        
        Body b = PhysicalScene.world.createBody(bd); 
        
        b.createFixture(fd);        
        
        return b;
    }

  
    
}
