package fxproexercises.ch01;

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
    public ObservableList genres = FXCollections.observableArrayList(
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
    public SingleSelectionModel genreSelectionModel;

    /**
     * Adds a change listener to the selection model of the ChoiceBox, and
     * contains code that executes when the selection in the ChoiceBox changes.
     */
    public void addListenerToGenreSelectionModel() {
        genreSelectionModel.selectedIndexProperty().addListener((ov, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    selectedDBs.setValue(80);
                    break;
                case 1:
                    selectedDBs.setValue(100);
                    break;
                case 2:
                    selectedDBs.setValue(150);
                    break;
                case 3:
                    selectedDBs.setValue(140);
                    break;
                case 4:
                    selectedDBs.setValue(120);
                    break;
                case 5:
                    selectedDBs.setValue(130);
            }
        });
    }
}
