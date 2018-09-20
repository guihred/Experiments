/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch06;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.HasLogging;

/**
 *
 * @author Note
 */
public final class FXCollectionsExamples {

	private FXCollectionsExamples() {
	}

	public static void main(String[] args) {
        Logger log = HasLogging.log();
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener(new MyListenerExamples());
        log.info("Calling addAll(\"Zero\", \"One\", \"Two\", \"Three\"): ");
        strings.addAll("Zero", "One", "Two", "Three");
        log.info("Calling FXCollections.sort(strings): ");
        FXCollections.sort(strings);
        log.info("Calling set(1, \"Three_1\"): ");
        strings.set(1, "Three_1");
        log.info("Calling setAll(\"One_1\", \"Three_1\", \"Two_1\", \"Zero_1\"): ");
        strings.setAll("One_1", "Three_1", "Two_1", "Zero_1");
        log.info("Calling removeAll(\"One_1\", \"Two_1\", \"Zero_1\"): ");
        strings.removeAll("One_1", "Two_1", "Zero_1");
    }

}

