/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxpro.ch06;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import utils.HasLogging;

/**
 *
 * @author Note
 */
public final class FXCollectionsMapExamples {

	private static final Logger LOGGER = HasLogging.log();

	private FXCollectionsMapExamples() {
	}

    public static void main(String[] args) {
        ObservableMap<String, Integer> map = FXCollections.observableHashMap();
        map.addListener(new MyListenerMapExamples());
		LOGGER.info("Calling put(\"First\", 1): ");
        map.put("First", 1);
		LOGGER.info("Calling put(\"First\", 100): ");
        map.put("First", 100);
        Map<String, Integer> anotherMap = new HashMap<>();
        anotherMap.put("Second", 2);
        anotherMap.put("Third", 3);
		LOGGER.info("Calling putAll(anotherMap): ");
        map.putAll(anotherMap);
        final Iterator<Map.Entry<String, Integer>> entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            final Map.Entry<String, Integer> next = entryIterator.next();
			if ("Second".equals(next.getKey())) {
				LOGGER.info("Calling remove on entryIterator: ");
                entryIterator.remove();
            }
        }
        final Iterator<Integer> valueIterator = map.values().iterator();
        while (valueIterator.hasNext()) {
            final Integer next = valueIterator.next();
            if (next == 3) {
				LOGGER.info("Calling remove on valueIterator: ");
                valueIterator.remove();
            }
        }
    }

}
