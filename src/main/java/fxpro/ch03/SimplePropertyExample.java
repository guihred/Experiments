/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch03;

import java.util.Random;
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
import utils.ex.HasLogging;

public final class SimplePropertyExample {
	private static final String SETTING_PROPERTY = "Calling intProperty.set({}).";


	private static final Logger LOG = HasLogging.log();


	private static IntegerProperty intProperty;

    private static Random random = new Random();

	private SimplePropertyExample() {
	}

	public static void main(String[] args) {
		createProperty();
		addAndRemoveInvalidationListener();
		addAndRemoveChangeListener();
		bindAndUnbindOnePropertyToAnother();
	}

	private static void addAndRemoveChangeListener() {
		int nextInt = random.nextInt(1000);
        LOG.trace("\n");
        ChangeListener<Number> changeListener = (observableValue, oldValue, newValue) -> LOG
            .trace("The observableValue has changed: oldValue = {}, newValue = {}", oldValue, newValue);
		intProperty.addListener(changeListener);
        LOG.trace("Added change listener.");
        LOG.trace(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		intProperty.removeListener(changeListener);
        LOG.trace("Removed change listener.");
		nextInt = random.nextInt(1000);
        LOG.trace(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
	}

	private static void addAndRemoveInvalidationListener() {
		int nextInt = random.nextInt(1000);
        LOG.trace("\n");
        InvalidationListener invalidationListener
            = (Observable observable) -> LOG.trace("The observable has been invalidated: {}.", observable);
		intProperty.addListener(invalidationListener);
        LOG.trace("Added invalidation listener.");
        LOG.trace(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		nextInt = random.nextInt(1000);
        LOG.trace("Calling intProperty.setValue({}).", nextInt);
		intProperty.setValue(Integer.valueOf(nextInt));
		intProperty.removeListener(invalidationListener);
        LOG.trace("Removed invalidation listener.");
		nextInt = random.nextInt(1000);
        LOG.trace(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
	}

	private static void bindAndUnbindOnePropertyToAnother() {
        LOG.trace("\n");
		int nextInt = random.nextInt(1000);
		IntegerProperty otherProperty = new SimpleIntegerProperty(0);
		logOtherProperty(otherProperty);
        LOG.trace("Binding otherProperty to intProperty.");
		otherProperty.bind(intProperty);
		logOtherProperty(otherProperty);
        LOG.trace(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		nextInt = random.nextInt(1000);
		logOtherProperty(otherProperty);
        LOG.trace("Unbinding otherProperty from intProperty.");
		otherProperty.unbind();
		logOtherProperty(otherProperty);
        LOG.trace(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		logOtherProperty(otherProperty);
	}

	private static void createProperty() {
        LOG.trace("\n");
		int nextInt = random.nextInt(1000);
		intProperty = new SimpleIntegerProperty(nextInt);
        LOG.trace("intProperty = {}", intProperty);
        LOG.trace("intProperty.get() = {}", intProperty.get());
		int intValue = intProperty.getValue().intValue();
        LOG.trace("intProperty.getValue() = {}", intValue);
	}

	private static void logOtherProperty(IntegerProperty otherProperty) {
        LOG.trace("otherProperty.get() = {}", otherProperty.get());
	}
}
