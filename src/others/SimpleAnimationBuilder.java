package others;

import javafx.animation.Animation;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

@SuppressWarnings("unchecked")
public class SimpleAnimationBuilder<T extends Animation, Z extends SimpleBuilder<T>> 
		implements SimpleBuilder<T> {

	protected T animation;

	protected SimpleAnimationBuilder(T shape) {
		this.animation = shape;
	}

	@Override
	public T build() {
		return animation;
	}

	public Z autoReverse(boolean d) {
		animation.setAutoReverse(d);
		return (Z) this;
	}

	public Z cycleCount(int value) {
		animation.setCycleCount(value);
		return (Z) this;
	}

	public Z delay(Duration value) {
		animation.setDelay(value);
		return (Z) this;
	}

	public Z onFinished(EventHandler<ActionEvent> value) {
		animation.setOnFinished(value);
		return (Z) this;
	}

	public Z rate(double value) {
		animation.setRate(value);
		return (Z) this;
	}

}