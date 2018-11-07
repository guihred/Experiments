package fxpro.ch01;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleTextBuilder;
import utils.ClassReflectionUtils;

public class AudioConfigLauncher extends Application {

    private static final String AUDIO_TEXT_STYLE = "audio-text";
    private AudioConfigModel acModel = new AudioConfigModel();

    @Override
    public void start(Stage stage) {
        Text textDb = new SimpleTextBuilder().styleClass(AUDIO_TEXT_STYLE).build();
        ChoiceBox<String> genreChoiceBox = new ChoiceBox<>(AudioConfigModel.GENRES);

        CheckBox mutingCheckBox = new CheckBox();
        Slider slider = new SimpleSliderBuilder()
                .min(AudioConfigModel.MIN_DECIBELS)
                .max(AudioConfigModel.MAX_DECIBELS).build();
        Text genreText = new SimpleTextBuilder().styleClass(AUDIO_TEXT_STYLE).text("Genre").build();
        Text mutingText = new SimpleTextBuilder().styleClass(AUDIO_TEXT_STYLE).text("Muting").build();
        Text audioConfigText = new SimpleTextBuilder().styleClass(AUDIO_TEXT_STYLE)
                .text("Audio Configuration").build();

        BorderPane root = new BorderPane();
        StackPane value = new StackPane(audioConfigText);
        value.getStyleClass().add("title-text");
        root.setTop(value);
        root.setCenter(new StackPane(new VBox(new HBox(textDb, slider), new Separator(),
                new HBox(mutingText, mutingCheckBox), new Separator(), new HBox(genreText, genreChoiceBox))));

        Scene scene = new Scene(root);
        textDb.textProperty().bind(acModel.selectedDBs.asString().concat(" dB"));
        slider.valueProperty().bindBidirectional(acModel.selectedDBs);
        slider.disableProperty().bind(acModel.muting);
        mutingCheckBox.selectedProperty().bindBidirectional(acModel.muting);
        acModel.setGenreSelectionModel(genreChoiceBox.getSelectionModel());
        acModel.getGenreSelectionModel().selectFirst();
        stage.setWidth(335);
        stage.setHeight(343);
        stage.setScene(scene);
        stage.setTitle("Audio Configuration");
        stage.show();
        ClassReflectionUtils.displayCSSStyler(scene, "audio-config.css");

    }


    public static void main(String[] args) {
        Application.launch(args);
    }



}
