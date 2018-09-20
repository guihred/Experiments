package physics;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.slf4j.Logger;
import utils.HasLogging;

/**
 *
 * @author wyoung
 */
public class Ramp extends BasePhysicalObject {

    private static final Logger LOGGER = HasLogging.log(Ramp.class);
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private float angle;
	private float fudgeX = 10.0F;
	private float fudgeY = 10.0F;

    public Ramp(float startX, float startY, float endX, float endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        angle = (float)Math.atan((this.endY - this.startY)/(this.endX - this.startX));
		fudgeX = (float) Math.cos(angle) * Physics.toPixelWidth(1.0F)
				+ (float) Math.sin(angle) * Physics.toPixelHeight(1.0F);
		fudgeY = (float) Math.sin(angle) * Physics.toPixelHeight(1.0F);
        LOGGER.info("ramp angle={}fudgeX={};fudgeY={}", Math.toDegrees(angle), fudgeX, fudgeY);
        build();
    }

    @Override
    public Node create() {
        Line rect = new Line(Physics.toPixelX(startX)+fudgeX,Physics.toPixelY(startY)-fudgeY,Physics.toPixelWidth(endX)-fudgeX,Physics.toPixelY(endY)+fudgeY);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.BLACK);
		rect.setStrokeWidth(Physics.toPixelHeight(1.0F));
        return rect;
    }

    @Override
    public Body createBody() {
        // ground
        FixtureDef fd = new FixtureDef();
        PolygonShape sd = new PolygonShape();

		sd.setAsBox(Math.abs(startX - endX) * 0.5F, 1);
        fd.shape = sd;
		fd.density = 1.0F;
		fd.friction = 0.7F;
        
        BodyDef bd = new BodyDef();
        bd.position = new Vec2((startX+endX)/2,(startY+endY)/2);        
        bd.angle = angle;
        Body bb = PhysicalScene.world.createBody(bd);
        bb.createFixture(fd);       
        
        
        return bb;
    }
}
