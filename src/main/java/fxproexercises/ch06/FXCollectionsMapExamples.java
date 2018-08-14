/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import simplebuilder.HasLogging;

/**
 *
 * @author Note
 */
public final class FXCollectionsMapExamples {

	private FXCollectionsMapExamples() {
	}

    public static void main(String[] args) {
        Logger log = HasLogging.log();

        ObservableMap<String, Integer> map = FXCollections.observableHashMap();
        map.addListener(new MyListenerMapExamples());
        log.info("Calling put(\"First\", 1): ");
        map.put("First", 1);
        log.info("Calling put(\"First\", 100): ");
        map.put("First", 100);
        Map<String, Integer> anotherMap = new HashMap<>();
        anotherMap.put("Second", 2);
        anotherMap.put("Third", 3);
        log.info("Calling putAll(anotherMap): ");
        map.putAll(anotherMap);
        final Iterator<Map.Entry<String, Integer>> entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            final Map.Entry<String, Integer> next = entryIterator.next();
			if ("Second".equals(next.getKey())) {
                log.info("Calling remove on entryIterator: ");
                entryIterator.remove();
            }
        }
        final Iterator<Integer> valueIterator = map.values().iterator();
        while (valueIterator.hasNext()) {
            final Integer next = valueIterator.next();
            if (next == 3) {
                log.info("Calling remove on valueIterator: ");
                valueIterator.remove();
            }
        }
    }

}
