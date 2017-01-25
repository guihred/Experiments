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
public class Wall extends PhysicalObject {
    
    private float worldX;
    private float worldY;
    private float worldWidth;
    private float worldHeight;
    
    public Wall(float worldX, float worldY, float width, float height) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.worldWidth = width;
        this.worldHeight = height;
        build();
    }

    @Override
    public Node create() {
        Rectangle rect = new Rectangle( Physics.toPixelX(worldX) /* left */,
                                        Physics.toPixelHeight(worldY) /* right */,
                                        Physics.toPixelWidth(worldWidth) /* width */,
                                        Physics.toPixelHeight(worldHeight) /* height */);
        
        System.out.println(rect.getBoundsInLocal());
        rect.setFill(Color.BLACK);
        return rect;
    }

    @Override
    public Body createBody() {
                   // ground
        FixtureDef fd = new FixtureDef();
        PolygonShape sd = new PolygonShape();
        
        sd.setAsBox(worldWidth,worldHeight);
        fd.shape = sd;
        fd.density = 1.0f;
        fd.friction = 0.3f;        
        

        BodyDef bd = new BodyDef();
        bd.position = new Vec2(worldX, worldY);
        Body bb = PhysicalScene.world.createBody(bd);
        bb.createFixture(fd);
        
        return bb;
    }
    
}
