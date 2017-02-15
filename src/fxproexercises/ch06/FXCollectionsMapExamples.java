/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 *
 * @author Note
 */
public final class FXCollectionsMapExamples {

	private FXCollectionsMapExamples() {
	}

    public static void main(String[] args) {
        ObservableMap<String, Integer> map = FXCollections.observableHashMap();
        map.addListener(new MyListenerMapExamples());
        System.out.println("Calling put(\"First\", 1): ");
        map.put("First", 1);
        System.out.println("Calling put(\"First\", 100): ");
        map.put("First", 100);
        Map<String, Integer> anotherMap = new HashMap<>();
        anotherMap.put("Second", 2);
        anotherMap.put("Third", 3);
        System.out.println("Calling putAll(anotherMap): ");
        map.putAll(anotherMap);
        final Iterator<Map.Entry<String, Integer>> entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            final Map.Entry<String, Integer> next = entryIterator.next();
			if ("Second".equals(next.getKey())) {
                System.out.println("Calling remove on entryIterator: ");
                entryIterator.remove();
            }
        }
        final Iterator<Integer> valueIterator = map.values().iterator();
        while (valueIterator.hasNext()) {
            final Integer next = valueIterator.next();
            if (next == 3) {
                System.out.println("Calling remove on valueIterator: ");
                valueIterator.remove();
            }
        }
    }

}
