package fxproexercises.ch01;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;

public class AudioConfigModel {

    /**
     * The minimum audio volume in decibels
     */
    public double minDecibels = 0.0;
    /**
     * The maximum audio volume in decibels
     */
    public double maxDecibels = 160.0;
    /**
     * The selected audio volume in decibels
     */
    public IntegerProperty selectedDBs = new SimpleIntegerProperty(0);
    /**
     * Indicates whether audio is muted
     */
    public BooleanProperty muting = new SimpleBooleanProperty(false);
    /**
     * List of some musical genres
     */
    public ObservableList<String> genres = FXCollections.observableArrayList(
            "Chamber",
            "Country",
            "Cowbell",
            "Metal",
            "Polka",
            "Rock"
    );
    /**
     * A reference to the selection model used by the Slider
     */
    public SingleSelectionModel<String> genreSelectionModel;

    /**
     * Adds a change listener to the selection model of the ChoiceBox, and
     * contains code that executes when the selection in the ChoiceBox changes.
     */
    public void addListenerToGenreSelectionModel() {
		Map<Integer, Integer> hashMap = new HashMap<>();
		hashMap.put(0, 80);
		hashMap.put(1, 100);
		hashMap.put(2, 150);
		hashMap.put(3, 140);
		hashMap.put(4, 120);
		hashMap.put(5, 130);
		genreSelectionModel.selectedIndexProperty().addListener((ov, oldValue, newValue) -> selectedDBs
				.setValue(hashMap.getOrDefault(newValue, selectedDBs.getValue())));
    }
}
