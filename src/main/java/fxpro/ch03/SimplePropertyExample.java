/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch03;

/**
 *
 * @author Note
 */
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import utils.HasLogging;

public final class SimplePropertyExample {
	private static final Logger LOG = HasLogging.log();


	private static IntegerProperty intProperty;

    private SimplePropertyExample() {
	}

    public static void main(String[] args) {
        createProperty();
        addAndRemoveInvalidationListener();
        addAndRemoveChangeListener();
        bindAndUnbindOnePropertyToAnother();
    }

    private static void addAndRemoveChangeListener() {
		LOG.info("\n");
		final ChangeListener<Number> changeListener = (observableValue, oldValue, newValue) -> LOG
                .info("The observableValue has changed: oldValue = {}, newValue = {}", oldValue, newValue);
        intProperty.addListener(changeListener);
		LOG.info("Added change listener.");
		LOG.info("Calling intProperty.set(5120).");
        intProperty.set(5120);
        intProperty.removeListener(changeListener);
		LOG.info("Removed change listener.");
		LOG.info("Calling intProperty.set(6144).");
        intProperty.set(6144);
    }

    private static void addAndRemoveInvalidationListener() {
		LOG.info("\n");
        final InvalidationListener invalidationListener
				= (Observable observable) -> LOG.info("The observable has been invalidated: {}.", observable);
        intProperty.addListener(invalidationListener);
		LOG.info("Added invalidation listener.");
		LOG.info("Calling intProperty.set(2048).");
        intProperty.set(2048);
		LOG.info("Calling intProperty.setValue(3072).");
        intProperty.setValue(Integer.valueOf(3072));
        intProperty.removeListener(invalidationListener);
		LOG.info("Removed invalidation listener.");
		LOG.info("Calling intProperty.set(4096).");
        intProperty.set(4096);
    }

    private static void bindAndUnbindOnePropertyToAnother() {
		LOG.info("\n");
        IntegerProperty otherProperty = new SimpleIntegerProperty(0);
        logOtherProperty(otherProperty);
		LOG.info("Binding otherProperty to intProperty.");
        otherProperty.bind(intProperty);
        logOtherProperty(otherProperty);
		LOG.info("Calling intProperty.set(7168).");
        intProperty.set(7168);
        logOtherProperty(otherProperty);
		LOG.info("Unbinding otherProperty from intProperty.");
        otherProperty.unbind();
        logOtherProperty(otherProperty);
		LOG.info("Calling intProperty.set(8192).");
        intProperty.set(8192);
        logOtherProperty(otherProperty);
    }

    private static void createProperty() {
		LOG.info("\n");
        intProperty = new SimpleIntegerProperty(1024);
		LOG.info("intProperty = {}", intProperty);
		LOG.info("intProperty.get() = {}", intProperty.get());
        int intValue = intProperty.getValue().intValue();
		LOG.info("intProperty.getValue() = {}", intValue);
    }

    private static void logOtherProperty(IntegerProperty otherProperty) {
		LOG.info("otherProperty.get() = {}", otherProperty.get());
    }
}
