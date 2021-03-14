package fxpro.ch01;

import com.google.common.collect.ImmutableMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.control.SingleSelectionModel;

public class AudioConfigModel {

    /**
     * The minimum audio volume in decibels
     */
	public static final double MIN_DECIBELS = 0.0;
    /**
     * The maximum audio volume in decibels
     */
	public static final double MAX_DECIBELS = 160.0;
    /**
     * List of some musical genres
     */
	public static final ObservableMap<String,Integer> GENRES_MAP = FXCollections.observableMap(
            ImmutableMap.<String, Integer>builder()
                    .put("Chamber", 80)
                    .put("Cowbell", 100)
                    .put("Metal", 150)
                    .put("Polka", 140)
                    .put("Rock", 120)
                    .build());
    /**
     * The selected audio volume in decibels
     */
	public final IntegerProperty selectedDBs = new SimpleIntegerProperty(0);
    /**
     * Indicates whether audio is muted
     */
	public final BooleanProperty muting = new SimpleBooleanProperty(false);
    /**
     * A reference to the selection model used by the Slider
     */
	private SingleSelectionModel<String> genreSelectionModel;

    public SingleSelectionModel<String> getGenreSelectionModel() {
		return genreSelectionModel;
	}

	public void setGenreSelectionModel(SingleSelectionModel<String> genreSelectionModel) {
		this.genreSelectionModel = genreSelectionModel;
		addListenerToGenreSelectionModel();
	}

	/**
     * Adds a change listener to the selection model of the ChoiceBox, and
     * contains code that executes when the selection in the ChoiceBox changes.
     */
    private void addListenerToGenreSelectionModel() {
        genreSelectionModel.selectedItemProperty().addListener((ov, oldValue, newValue) -> selectedDBs
                .setValue(GENRES_MAP.getOrDefault(newValue, selectedDBs.getValue())));
    }
}
