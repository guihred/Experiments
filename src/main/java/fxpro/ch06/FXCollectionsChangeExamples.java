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
import utils.HasLogging;

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
		strings.addListener((Observable observable) -> LOGGER.info("\tlist invalidated"));
		strings.addListener((ListChangeListener<String>) change -> LOGGER.info("\tstrings = {}", change.getList()));
		LOGGER.info("Calling add(\"First\"): ");
        strings.add("First");
		LOGGER.info("Calling add(0, \"Zeroth\"): ");
        strings.add(0, "Zeroth");
		LOGGER.info("Calling addAll(\"Second\", \"Third\"): ");
        strings.addAll("Second", "Third");
		LOGGER.info("Calling set(1, \"New First\"): ");
        strings.set(1, "New First");
        final List<String> list = Arrays.asList("Second_1", "Second_2");
		LOGGER.info("Calling addAll(3, list): ");
        strings.addAll(3, list);
		LOGGER.info("Calling remove(2, 4): ");
        strings.remove(2, 4);
        final Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            final String next = iterator.next();
            if (next.contains("t")) {
				LOGGER.info("Calling remove() on iterator: ");
                iterator.remove();
            }
        }
		LOGGER.info("Calling removeAll(\"Third\", \"Fourth\"): ");
        strings.removeAll("Third", "Fourth");
    }
}
