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
import utils.HasLogging;

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
		LOG.info("\n");
		final ChangeListener<Number> changeListener = (observableValue, oldValue, newValue) -> LOG
				.info("The observableValue has changed: oldValue = {}, newValue = {}", oldValue, newValue);
		intProperty.addListener(changeListener);
		LOG.info("Added change listener.");
		LOG.info(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		intProperty.removeListener(changeListener);
		LOG.info("Removed change listener.");
		nextInt = random.nextInt(1000);
		LOG.info(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
	}

	private static void addAndRemoveInvalidationListener() {
		int nextInt = random.nextInt(1000);
		LOG.info("\n");
		final InvalidationListener invalidationListener
		= (Observable observable) -> LOG.info("The observable has been invalidated: {}.", observable);
		intProperty.addListener(invalidationListener);
		LOG.info("Added invalidation listener.");
		LOG.info(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		nextInt = random.nextInt(1000);
		LOG.info("Calling intProperty.setValue({}).", nextInt);
		intProperty.setValue(Integer.valueOf(nextInt));
		intProperty.removeListener(invalidationListener);
		LOG.info("Removed invalidation listener.");
		nextInt = random.nextInt(1000);
		LOG.info(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
	}

	private static void bindAndUnbindOnePropertyToAnother() {
		LOG.info("\n");
		int nextInt = random.nextInt(1000);
		IntegerProperty otherProperty = new SimpleIntegerProperty(0);
		logOtherProperty(otherProperty);
		LOG.info("Binding otherProperty to intProperty.");
		otherProperty.bind(intProperty);
		logOtherProperty(otherProperty);
		LOG.info(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		nextInt = random.nextInt(1000);
		logOtherProperty(otherProperty);
		LOG.info("Unbinding otherProperty from intProperty.");
		otherProperty.unbind();
		logOtherProperty(otherProperty);
		LOG.info(SETTING_PROPERTY, nextInt);
		intProperty.set(nextInt);
		logOtherProperty(otherProperty);
	}

	private static void createProperty() {
		LOG.info("\n");
		int nextInt = random.nextInt(1000);
		intProperty = new SimpleIntegerProperty(nextInt);
		LOG.info("intProperty = {}", intProperty);
		LOG.info("intProperty.get() = {}", intProperty.get());
		int intValue = intProperty.getValue().intValue();
		LOG.info("intProperty.getValue() = {}", intValue);
	}

	private static void logOtherProperty(IntegerProperty otherProperty) {
		LOG.info("otherProperty.get() = {}", otherProperty.get());
	}
}
