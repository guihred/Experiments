/*
 * CubeFaceComparator.java
 *
 * A Comparator for putting the cube faces in the correct Z-order as it rotates.
 *
 * Developed by James L. Weaver (jim.weaver#javafxpert.com) to demonstrate the
 * use of 3D features in the JavaFX 2.0 API
 */
package fxpro.earth;

import java.util.Comparator;
import javafx.scene.Node;

/**
 *
 * @author Jim Weaver
 */
public class CubeFaceComparator implements Comparator<Node> {
	@Override
	public int compare(Node cubeFaceB, Node cubeFaceA) {
		CubeFace faceA = (CubeFace) cubeFaceA;
		CubeFace faceB = (CubeFace) cubeFaceB;
		return faceA.getZPos().compareTo(faceB.getZPos());
	}
}
