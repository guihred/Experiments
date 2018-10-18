/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch06;

import java.util.Arrays;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.HasLogging;

/**
 *
 * @author Note
 */
public final class FXCollectionsMethodsExamples {

	private static final Logger LOGGER = HasLogging.log();

	private FXCollectionsMethodsExamples() {
	}

    public static void main(String[] args) {
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener(new MyListenerMethodsExamples());
		LOGGER.info("Calling addAll(\"Zero\", \"One\", \"Two\", \"Three\"): ");
        strings.addAll("Zero", "One", "Two", "Three");
		LOGGER.info("Calling copy: ");
        FXCollections.copy(strings, Arrays.asList("Four", "Five"));
		LOGGER.info("Calling replaceAll: ");
        FXCollections.replaceAll(strings, "Two", "Two_1");
		LOGGER.info("Calling reverse: ");
        FXCollections.reverse(strings);
		LOGGER.info("Calling rotate(strings, 2: ");
        FXCollections.rotate(strings, 2);
		LOGGER.info("Calling shuffle(strings): ");
        FXCollections.shuffle(strings);
		LOGGER.info("Calling shuffle(strings, new Random(0L)): ");
        FXCollections.shuffle(strings, new Random(0L));
		LOGGER.info("Calling sort(strings): ");
        FXCollections.sort(strings);
		LOGGER.info("Calling sort(strings, c) with custom comparator: ");
        FXCollections.sort(strings, (String lhs, String rhs) -> rhs.compareTo(lhs));
		LOGGER.info("Calling fill(strings, \"Ten\"): ");
        FXCollections.fill(strings, "Ten");
    }

}
