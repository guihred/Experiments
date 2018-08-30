package fxproexercises.ch01;

import static simplebuilder.CommonsFX.newCheckBox;

import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleLineBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleSliderBuilder;
import simplebuilder.SimpleTextBuilder;

public class AudioConfigLauncher extends Application {

    private static final String COLOR_AUDIO = "#131021";
    private AudioConfigModel acModel = new AudioConfigModel();

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
        Text textDb = new SimpleTextBuilder().x(18).y(69).textOrigin(VPos.TOP).fill(Color.web(COLOR_AUDIO))
				.font(Font.font("SansSerif", FontWeight.BOLD, 18)).build();
        ChoiceBox<String> genreChoiceBox = new ChoiceBox<>(AudioConfigModel.GENRES);
		genreChoiceBox.setLayoutX(204);
		genreChoiceBox.setLayoutY(154);
		genreChoiceBox.setPrefWidth(93);

		int x = 280;
		int y = 113;
        CheckBox mutingCheckBox = newCheckBox(x, y);
        Slider slider = new SimpleSliderBuilder().layoutX(135).layoutY(135).prefWidth(162)
                .min(AudioConfigModel.MIN_DECIBELS)
				.max(AudioConfigModel.MAX_DECIBELS).build();
        Text genreText = new SimpleTextBuilder().textOrigin(VPos.TOP).fill(Color.web(COLOR_AUDIO))
				.font(Font.font("SanSerif", FontWeight.BOLD, 18)).text("Genre").build();
        Line line3 = new SimpleLineBuilder().startX(9).startY(141).endX(309).endY(141)
                .stroke(Color.color(0.66, 0.67, 0.69)).build();
        Text mutingText = new SimpleTextBuilder().textOrigin(VPos.TOP).fill(Color.web(COLOR_AUDIO))
				.font(Font.font("SanSerif", FontWeight.BOLD, 18)).text("Muting").build();
        Line line2 = new SimpleLineBuilder().startX(9).startY(97).endX(309).endY(97)
                .stroke(Color.color(0.66, 0.67, 0.69)).build();
        Rectangle whiteRectangle = new SimpleRectangleBuilder().x(9).y(54).width(300).height(130).arcHeight(20)
				.arcWidth(20).fill(Color.WHITE).stroke(Color.color(0.66, 0.67, 0.69)).build();
        Text audioConfigText = new SimpleTextBuilder().textOrigin(VPos.TOP).fill(Color.WHITE)
				.font(Font.font("SansSerif", FontWeight.BOLD, 20)).text("Audio Configuration").build();
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("0xAEBBCC")), new Stop(1, Color.web("0x6D84A3")));
        Rectangle whitishRectangle = new Rectangle(320, 300, Color.rgb(199, 206, 213));
		whitishRectangle.setY(43);
        Rectangle gradientRectangle = new Rectangle(320, 45, linearGradient);

		Scene scene = new Scene(new Group(gradientRectangle, audioConfigText, whitishRectangle, whiteRectangle, textDb,
				slider, line2, mutingText, mutingCheckBox, line3, genreText, genreChoiceBox));
		textDb.textProperty().bind(acModel.selectedDBs.asString().concat(" dB"));
		slider.valueProperty().bindBidirectional(acModel.selectedDBs);
		slider.disableProperty().bind(acModel.muting);
		mutingCheckBox.selectedProperty().bindBidirectional(acModel.muting);
		acModel.setGenreSelectionModel(genreChoiceBox.getSelectionModel());
		acModel.getGenreSelectionModel().selectFirst();
		stage.setWidth(320);
		stage.setHeight(343);
		stage.setScene(scene);
		stage.setTitle("Audio Configuration");
		stage.show();
	}

}
