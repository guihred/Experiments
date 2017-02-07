package simplebuilder;

import javafx.event.EventHandler;
import javafx.scene.transform.Transform;
import javafx.scene.transform.TransformChangedEvent;

@SuppressWarnings("unchecked")
public class SimpleTransformBuilder<T extends Transform, Z extends SimpleBuilder<T>> implements SimpleBuilder<T> {
	protected T transform;

	protected SimpleTransformBuilder(T shape) {
		this.transform = shape;
	}

	@Override
	public T build() {
		return transform;
	}

	public Z onTransformChange(EventHandler<? super TransformChangedEvent> value) {
		transform.setOnTransformChanged(value);
		return (Z) this;
	}
}