/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch03;

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
import simplebuilder.HasLogging;

public final class SimplePropertyExample {
    private static Logger log = HasLogging.log();


	private static IntegerProperty intProperty;

    private SimplePropertyExample() {
	}

    private static void addAndRemoveChangeListener() {
        log.info("\n");
        final ChangeListener<Number> changeListener = (observableValue, oldValue, newValue) -> log
                .info("The observableValue has changed: oldValue = {}, newValue = {}", oldValue, newValue);
        intProperty.addListener(changeListener);
        log.info("Added change listener.");
        log.info("Calling intProperty.set(5120).");
        intProperty.set(5120);
        intProperty.removeListener(changeListener);
        log.info("Removed change listener.");
        log.info("Calling intProperty.set(6144).");
        intProperty.set(6144);
    }

    private static void addAndRemoveInvalidationListener() {
        log.info("\n");
        final InvalidationListener invalidationListener
                = (Observable observable) -> log.info("The observable has been invalidated: {}.", observable);
        intProperty.addListener(invalidationListener);
        log.info("Added invalidation listener.");
        log.info("Calling intProperty.set(2048).");
        intProperty.set(2048);
        log.info("Calling intProperty.setValue(3072).");
        intProperty.setValue(Integer.valueOf(3072));
        intProperty.removeListener(invalidationListener);
        log.info("Removed invalidation listener.");
        log.info("Calling intProperty.set(4096).");
        intProperty.set(4096);
    }

    private static void bindAndUnbindOnePropertyToAnother() {
        log.info("\n");
        IntegerProperty otherProperty = new SimpleIntegerProperty(0);
        logOtherProperty(otherProperty);
        log.info("Binding otherProperty to intProperty.");
        otherProperty.bind(intProperty);
        logOtherProperty(otherProperty);
        log.info("Calling intProperty.set(7168).");
        intProperty.set(7168);
        logOtherProperty(otherProperty);
        log.info("Unbinding otherProperty from intProperty.");
        otherProperty.unbind();
        logOtherProperty(otherProperty);
        log.info("Calling intProperty.set(8192).");
        intProperty.set(8192);
        logOtherProperty(otherProperty);
    }

    private static void logOtherProperty(IntegerProperty otherProperty) {
        log.info("otherProperty.get() = {}", otherProperty.get());
    }

    private static void createProperty() {
        log.info("\n");
        intProperty = new SimpleIntegerProperty(1024);
        log.info("intProperty = {}", intProperty);
        log.info("intProperty.get() = {}", intProperty.get());
        int intValue = intProperty.getValue().intValue();
        log.info("intProperty.getValue() = {}", intValue);
    }

    public static void main(String[] args) {
        createProperty();
        addAndRemoveInvalidationListener();
        addAndRemoveChangeListener();
        bindAndUnbindOnePropertyToAnother();
    }
}
