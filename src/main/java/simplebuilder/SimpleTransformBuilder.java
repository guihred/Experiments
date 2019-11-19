package simplebuilder;

import javafx.scene.transform.Transform;

public class SimpleTransformBuilder<T extends Transform> implements SimpleBuilder<T> {
	protected T transform;

	protected SimpleTransformBuilder(T shape) {
		this.transform = shape;
	}

	@Override
	public T build() {
		return transform;
	}

}