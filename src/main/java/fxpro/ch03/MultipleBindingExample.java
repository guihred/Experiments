package fxpro.ch03;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.slf4j.Logger;
import utils.HasLogging;

public final class MultipleBindingExample {
    private static final Logger LOG = HasLogging.log(MultipleBindingExample.class);

	private MultipleBindingExample() {
	}

	public static void main(String[] args) {
        LOG.info("Constructing x with initial value of 2.0.");
        final DoubleProperty x = new SimpleDoubleProperty(null, "x", 2.0);
        LOG.info("Constructing y with initial value of 3.0.");
        final DoubleProperty y = new SimpleDoubleProperty(null, "y", 3.0);
        LOG.info("Creating binding area with dependencies x and y.");
		DoubleBinding area = Bindings.createDoubleBinding(() -> {
            LOG.info("computeValue() is called.");
			return x.get() * y.get();
		}, x, y);

        logBinding(area);
        logBinding(area);
        LOG.info("Setting x to 5");
        x.set(5);
        LOG.info("Setting y to 7");
        y.set(7);
        logBinding(area);
    }

    private static void logBinding(DoubleBinding area) {
        LOG.info("area.get() = {}", area.get());
    }
}
