package graphs.app;

import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;

@FunctionalInterface
public interface Layout extends EventTarget {
	@Override
    default EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return null;
    }

	void execute();

    default String getName() {
		return getClass().getSimpleName().replace("Layout", "");
	}
}