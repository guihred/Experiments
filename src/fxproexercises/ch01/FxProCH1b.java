package fxproexercises.ch01;

import static others.CommonsFX.newCheckBox;

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
import others.SimpleLineBuilder;
import others.SimpleRectangleBuilder;
import others.SimpleSliderBuilder;
import others.SimpleTextBuilder;

public class FxProCH1b extends Application {

	AudioConfigModel acModel = new AudioConfigModel();
	Text textDb;
	Slider slider;
	CheckBox mutingCheckBox;
	ChoiceBox<String> genreChoiceBox;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		textDb = new SimpleTextBuilder().x(18).y(69).textOrigin(VPos.TOP).fill(Color.web("#131021"))
				.font(Font.font("SansSerif", FontWeight.BOLD, 18)).build();
		genreChoiceBox = new ChoiceBox<>(acModel.genres);
		genreChoiceBox.setLayoutX(204);
		genreChoiceBox.setLayoutY(154);
		genreChoiceBox.setPrefWidth(93);

		int x = 280;
		int y = 113;
		mutingCheckBox = newCheckBox(x, y);
		slider = new SimpleSliderBuilder().layoutX(135).layoutY(135).prefWidth(162).min(acModel.minDecibels)
		.max(acModel.maxDecibels).build();
		final Text genreText = new SimpleTextBuilder().textOrigin(VPos.TOP).fill(Color.web("#131021"))
				.font(Font.font("SanSerif", FontWeight.BOLD, 18)).text("Genre").build();
		final Line line3 = new SimpleLineBuilder().startX(9).startY(141).endX(309).endY(141).stroke(Color.color(0.66, 0.67, 0.69)).build();
		final Text mutingText = new SimpleTextBuilder().textOrigin(VPos.TOP).fill(Color.web("#131021"))
				.font(Font.font("SanSerif", FontWeight.BOLD, 18)).text("Muting").build();
		final Line line2 = new SimpleLineBuilder().startX(9).startY(97).endX(309).endY(97).stroke(Color.color(0.66, 0.67, 0.69)).build();
		final Rectangle whiteRectangle = new SimpleRectangleBuilder().x(9).y(54).width(300).height(130).arcHeight(20)
				.arcWidth(20).fill(Color.WHITE).stroke(Color.color(0.66, 0.67, 0.69)).build();
		final Text audioConfigText = new SimpleTextBuilder().textOrigin(VPos.TOP).fill(Color.WHITE)
				.font(Font.font("SansSerif", FontWeight.BOLD, 20)).text("Audio Configuration").build();
		final LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("0xAEBBCC")), new Stop(1, Color.web("0x6D84A3")));
		final Rectangle whitishRectangle = new Rectangle(320, 300, Color.rgb(199, 206, 213));
		whitishRectangle.setY(43);
		final Rectangle gradientRectangle = new Rectangle(320, 45, linearGradient);

		Scene scene = new Scene(new Group(gradientRectangle, audioConfigText, whitishRectangle, whiteRectangle, textDb,
				slider, line2, mutingText, mutingCheckBox, line3, genreText, genreChoiceBox));
		textDb.textProperty().bind(acModel.selectedDBs.asString().concat(" dB"));
		slider.valueProperty().bindBidirectional(acModel.selectedDBs);
		slider.disableProperty().bind(acModel.muting);
		mutingCheckBox.selectedProperty().bindBidirectional(acModel.muting);
		acModel.genreSelectionModel = genreChoiceBox.getSelectionModel();
		acModel.addListenerToGenreSelectionModel();
		acModel.genreSelectionModel.selectFirst();
		stage.setWidth(320);
		stage.setHeight(343);
		stage.setScene(scene);
		stage.setTitle("Audio Configuration");
		stage.show();
	}

}
