/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.Arrays;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Note
 */
public final class FXCollectionsMethodsExamples {

	private FXCollectionsMethodsExamples() {
	}

    public static void main(String[] args) {
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener(new MyListenerMethodsExamples());
        System.out.println("Calling addAll(\"Zero\", \"One\", \"Two\", \"Three\"): ");
        strings.addAll("Zero", "One", "Two", "Three");
        System.out.println("Calling copy: ");
        FXCollections.copy(strings, Arrays.asList("Four", "Five"));
        System.out.println("Calling replaceAll: ");
        FXCollections.replaceAll(strings, "Two", "Two_1");
        System.out.println("Calling reverse: ");
        FXCollections.reverse(strings);
        System.out.println("Calling rotate(strings, 2: ");
        FXCollections.rotate(strings, 2);
        System.out.println("Calling shuffle(strings): ");
        FXCollections.shuffle(strings);
        System.out.println("Calling shuffle(strings, new Random(0L)): ");
        FXCollections.shuffle(strings, new Random(0L));
        System.out.println("Calling sort(strings): ");
        FXCollections.sort(strings);
        System.out.println("Calling sort(strings, c) with custom comparator: ");
        FXCollections.sort(strings, (String lhs, String rhs) -> rhs.compareTo(lhs));
        System.out.println("Calling fill(strings, \"Ten\"): ");
        FXCollections.fill(strings, "Ten");
    }

}
