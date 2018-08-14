/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.Arrays;
import java.util.Random;

import org.slf4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import simplebuilder.HasLogging;

/**
 *
 * @author Note
 */
public final class FXCollectionsMethodsExamples {

	private FXCollectionsMethodsExamples() {
	}

    public static void main(String[] args) {
        Logger log = HasLogging.log();
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener(new MyListenerMethodsExamples());
        log.info("Calling addAll(\"Zero\", \"One\", \"Two\", \"Three\"): ");
        strings.addAll("Zero", "One", "Two", "Three");
        log.info("Calling copy: ");
        FXCollections.copy(strings, Arrays.asList("Four", "Five"));
        log.info("Calling replaceAll: ");
        FXCollections.replaceAll(strings, "Two", "Two_1");
        log.info("Calling reverse: ");
        FXCollections.reverse(strings);
        log.info("Calling rotate(strings, 2: ");
        FXCollections.rotate(strings, 2);
        log.info("Calling shuffle(strings): ");
        FXCollections.shuffle(strings);
        log.info("Calling shuffle(strings, new Random(0L)): ");
        FXCollections.shuffle(strings, new Random(0L));
        log.info("Calling sort(strings): ");
        FXCollections.sort(strings);
        log.info("Calling sort(strings, c) with custom comparator: ");
        FXCollections.sort(strings, (String lhs, String rhs) -> rhs.compareTo(lhs));
        log.info("Calling fill(strings, \"Ten\"): ");
        FXCollections.fill(strings, "Ten");
    }

}
