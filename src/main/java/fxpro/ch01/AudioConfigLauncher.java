package fxpro.ch01;

import static utils.CommonsFX.newCheckBox;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.SimpleLineBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleTextBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.HasLogging;

public class AudioConfigLauncher extends Application {

    private static final String AUDIO_TEXT_STYLE = "audio-text";
    private static final Logger LOG = HasLogging.log();
    private AudioConfigModel acModel = new AudioConfigModel();

	@Override
	public void start(Stage stage) {
        Text textDb = new SimpleTextBuilder().y(65)
                .styleClass(AUDIO_TEXT_STYLE).build();
        ChoiceBox<String> genreChoiceBox = new ChoiceBox<>(AudioConfigModel.GENRES);
		genreChoiceBox.setLayoutX(204);
		genreChoiceBox.setLayoutY(154);
		genreChoiceBox.setPrefWidth(93);

		CheckBox mutingCheckBox = newCheckBox(280, 113);
        Slider slider = new SimpleSliderBuilder().layoutY(69).prefWidth(162)
                .min(AudioConfigModel.MIN_DECIBELS)
				.max(AudioConfigModel.MAX_DECIBELS).build();
        Text genreText = new SimpleTextBuilder()
                .styleClass(AUDIO_TEXT_STYLE)
                .layoutY(144).text("Genre").build();
        Line line3 = new SimpleLineBuilder().startX(9).startY(141).endX(309).endY(141)
                .build();
        Text mutingText = new SimpleTextBuilder()
                .styleClass(AUDIO_TEXT_STYLE)
                .layoutY(105).text("Muting").build();
        Line line2 = new SimpleLineBuilder().startX(9).startY(97).endX(309).endY(97)
                .build();
        Rectangle whiteRectangle = new SimpleRectangleBuilder().x(9).y(54).width(300).height(130)
                .styleClass("round-square").build();
        Text audioConfigText = new SimpleTextBuilder().fill(Color.WHITE)
                .styleClass(AUDIO_TEXT_STYLE)
                .layoutY(10).text("Audio Configuration")
                .build();
        Rectangle whitishRectangle = new Rectangle(320, 300);
        whitishRectangle.getStyleClass().add("whitish-rectangle");
		whitishRectangle.setY(43);
        Rectangle gradientRectangle = new Rectangle(320, 45);
        gradientRectangle.getStyleClass().add("gradient-rectangle");

        BorderPane root = new BorderPane();
        root.setTop(new StackPane(audioConfigText));
        root.setCenter(new StackPane(new VBox(new HBox(textDb, slider), new HBox(mutingText, mutingCheckBox),
                new HBox(genreText, genreChoiceBox))));

        //        Group root = new Group(gradientRectangle, audioConfigText, whitishRectangle, whiteRectangle, textDb, slider,
        //                line2, mutingText, mutingCheckBox, line3, genreText, genreChoiceBox);
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
        displayCSSStyler(scene, "src/main/resources/audio-config.css");
        ClassReflectionUtils.displayStyleClass(scene.getRoot());
    }

    private void displayCSSStyler(Scene scene, String pathname) {
        Stage stage2 = new Stage();
        File file = new File(pathname);
        TextArea textArea = new TextArea(getText(file));
        if (file.exists()) {
            try {
                scene.getStylesheets().add(file.toURI().toURL().toString());
            } catch (MalformedURLException e2) {
                LOG.error("", e2);
            }
        }
        stage2.setScene(new Scene(new VBox(textArea, CommonsFX.newButton("_Save", e -> {
            try (PrintStream fileOutputStream = new PrintStream(file);) {
                fileOutputStream.print(textArea.getText());
                fileOutputStream.flush();
                scene.getStylesheets().clear();
                scene.getStylesheets().add(file.toURI().toURL().toString());
                textArea.requestFocus();
            } catch (Exception e1) {
                LOG.error("", e1);
            }

        }))));
        stage2.show();
    }

    private String getText(File file) {
        try {
            return Files.toString(file, StandardCharsets.UTF_8);
        } catch (IOException e2) {
            LOG.error("", e2);
        }
        return "";
    }

    public static void main(String[] args) {
		Application.launch(args);
	}


}
