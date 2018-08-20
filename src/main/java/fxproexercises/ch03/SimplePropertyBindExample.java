/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch03;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public final class SimplePropertyBindExample {

	private SimplePropertyBindExample() {
	}
    public static void main(String[] args) {
        Logger log = HasLogging.log();
        log.info("Constructing two StringProperty objects.");
        StringProperty prop1 = new SimpleStringProperty("");
        StringProperty prop2 = new SimpleStringProperty("");
        log.info("Calling bindBidirectional.");
        prop2.bindBidirectional(prop1);
        log.info("prop1.isBound() = {}", prop1.isBound());
        log.info("prop2.isBound() = {}", prop2.isBound());
        log.info("Calling prop1.set(\"prop1 says: Hi!\")");
        prop1.set("prop1 says: Hi!");
        log.info("prop2.get() returned:");
        log.info(prop2.get());
        log.info("Calling prop2.set(prop2.get() + \"\\nprop2 says: Bye!\")");
        prop2.set(prop2.get() + "\nprop2 says: Bye!");
        log.info("prop1.get() returned:{}", prop1.get());
    }
}
