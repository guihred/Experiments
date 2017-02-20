/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Note
 */
public final class FXCollectionsExamples {

	private FXCollectionsExamples() {
	}

	public static void main(String[] args) {
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener(new MyListenerExamples());
        System.out.println("Calling addAll(\"Zero\", \"One\", \"Two\", \"Three\"): ");
        strings.addAll("Zero", "One", "Two", "Three");
        System.out.println("Calling FXCollections.sort(strings): ");
        FXCollections.sort(strings);
        System.out.println("Calling set(1, \"Three_1\"): ");
        strings.set(1, "Three_1");
        System.out.println("Calling setAll(\"One_1\", \"Three_1\", \"Two_1\", \"Zero_1\"): ");
        strings.setAll("One_1", "Three_1", "Two_1", "Zero_1");
        System.out.println("Calling removeAll(\"One_1\", \"Two_1\", \"Zero_1\"): ");
        strings.removeAll("One_1", "Two_1", "Zero_1");
    }

}

