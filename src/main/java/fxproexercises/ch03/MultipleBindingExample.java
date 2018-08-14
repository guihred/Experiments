package fxproexercises.ch03;

import org.slf4j.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import simplebuilder.HasLogging;

public final class MultipleBindingExample {

	private MultipleBindingExample() {
	}

	public static void main(String[] args) {
        Logger log = HasLogging.log();
        log.info("Constructing x with initial value of 2.0.");
        final DoubleProperty x = new SimpleDoubleProperty(null, "x", 2.0);
        log.info("Constructing y with initial value of 3.0.");
        final DoubleProperty y = new SimpleDoubleProperty(null, "y", 3.0);
        log.info("Creating binding area with dependencies x and y.");
		DoubleBinding area = Bindings.createDoubleBinding(() -> {
            log.info("computeValue() is called.");
			return x.get() * y.get();
		}, x, y);

        logBinding(area);
        logBinding(area);
        log.info("Setting x to 5");
        x.set(5);
        log.info("Setting y to 7");
        y.set(7);
        logBinding(area);
    }

    private static void logBinding(DoubleBinding area) {
        Logger log = HasLogging.log();
        log.info("area.get() = {}", area.get());
    }
}
