/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch03;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import utils.HasLogging;

public final class SimplePropertyBindExample {

	private static final Logger LOG = HasLogging.log();
	private SimplePropertyBindExample() {
	}
    public static void main(String[] args) {
		LOG.info("Constructing two StringProperty objects.");
        StringProperty prop1 = new SimpleStringProperty("");
        StringProperty prop2 = new SimpleStringProperty("");
		LOG.info("Calling bindBidirectional.");
        prop2.bindBidirectional(prop1);
		LOG.info("prop1.isBound() = {}", prop1.isBound());
		LOG.info("prop2.isBound() = {}", prop2.isBound());
		LOG.info("Calling prop1.set(\"prop1 says: Hi!\")");
        prop1.set("prop1 says: Hi!");
		LOG.info("prop2.get() returned:");
		LOG.info(prop2.get());
		LOG.info("Calling prop2.set(prop2.get() + \"\\nprop2 says: Bye!\")");
        prop2.set(prop2.get() + "\nprop2 says: Bye!");
		LOG.info("prop1.get() returned:{}", prop1.get());
    }
}
