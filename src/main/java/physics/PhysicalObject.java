package physics;

import javafx.scene.Node;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author wayne
 */
public abstract class PhysicalObject {

	protected Node node;
	protected Body body;

	final void build() {
		node = create();
		body = createBody();
		node.setUserData(body);
	}

	public abstract Node create();

	public abstract Body createBody();

}
