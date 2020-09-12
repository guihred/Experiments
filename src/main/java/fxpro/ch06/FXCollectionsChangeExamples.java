/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch06;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.ex.HasLogging;

/**
 *
 * @author Note
 */
public final class FXCollectionsChangeExamples {

	private static final Logger LOGGER = HasLogging.log();

	private FXCollectionsChangeExamples() {
	}

    public static void main(String[] args) {
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener((Observable observable) -> LOGGER.trace("\tlist invalidated"));
        strings.addListener((ListChangeListener<String>) change -> LOGGER.trace("\tstrings = {}", change.getList()));
        LOGGER.trace("Calling add(\"First\"): ");
        strings.add("First");
        LOGGER.trace("Calling add(0, \"Zeroth\"): ");
        strings.add(0, "Zeroth");
        LOGGER.trace("Calling addAll(\"Second\", \"Third\"): ");
        strings.addAll("Second", "Third");
        LOGGER.trace("Calling set(1, \"New First\"): ");
        strings.set(1, "New First");
        List<String> list = Arrays.asList("Second_1", "Second_2");
        LOGGER.trace("Calling addAll(3, list): ");
        strings.addAll(3, list);
        LOGGER.trace("Calling remove(2, 4): ");
        strings.remove(2, 4);
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.contains("t")) {
                LOGGER.trace("Calling remove() on iterator: ");
                iterator.remove();
            }
        }
        LOGGER.trace("Calling removeAll(\"Third\", \"Fourth\"): ");
        strings.removeAll("Third", "Fourth");
    }
}
