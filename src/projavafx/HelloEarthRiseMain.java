package projavafx;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
public class HelloEarthRiseMain extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		String message = "Earthrise at Christmas: " + "[Forty] years ago this Christmas, a turbulent world "
				+ "looked to the heavens for a unique view of our home " + "planet. This photo of Earthrise over the lunar horizon "
				+ "was taken by the Apollo 8 crew in December 1968, showing " + "Earth for the first time as it appears from deep space. "
				+ "Astronauts Frank Borman, Jim Lovell and William Anders " + "had become the first humans to leave Earth orbit, "
				+ "entering lunar orbit on Christmas Eve. In a historic live " + "broadcast that night, the crew took turns reading from "
				+ "the Book of Genesis, closing with a holiday wish from " + "Commander Borman: \"We close with good night, good luck, "
				+ "a Merry Christmas, and God bless all of you -- all of " + "you on the good Earth.\"";

		// Reference to the Text
		Text textRef = new Text();
		textRef.setLayoutY(100);
		textRef.setTextOrigin(VPos.TOP);
		textRef.setTextAlignment(TextAlignment.JUSTIFY);
		textRef.setWrappingWidth(400);
		textRef.setText(message);
		textRef.setFill(Color.rgb(187, 195, 107));
		textRef.setFont(Font.font("SansSerif", FontWeight.BOLD, 24));
		Rectangle rect = new Rectangle();
		rect.setWidth(430);
		rect.setHeight(85);

		// Provides the animated scrolling behavior for the text
		TranslateTransition transTransition = new TranslateTransition();
		transTransition.setDuration(new Duration(75000));
		transTransition.setNode(textRef);
		transTransition.setToY(-820);
		transTransition.setInterpolator(Interpolator.LINEAR);
		transTransition.setCycleCount(Animation.INDEFINITE);
		stage.setTitle("Hello Earthrise");
		stage.show();
		// Start the text animation
		transTransition.play();

	}


}
