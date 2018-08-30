package fxproexercises.ch01;

import election.experiment.CrawlerTask;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTextBuilder;

public class EarthriseChristmasApp extends Application {
    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        String message
                = "Earthrise at Christmas: "
                + "[Forty] years ago this Christmas, a turbulent world "
                + "looked to the heavens for a unique view of our home "
                + "planet. This photo of Earthrise over the lunar horizon "
                + "was taken by the Apollo 8 crew in December 1968, showing "
                + "Earth for the first time as it appears from deep space. "
                + "Astronauts Frank Borman, Jim Lovell and William Anders "
                + "had become the first humans to leave Earth orbit, "
                + "entering lunar orbit on Christmas Eve. In a historic live "
                + "broadcast that night, the crew took turns reading from "
                + "the Book of Genesis, closing with a holiday wish from "
                + "Commander Borman: \"We close with good night, good luck, "
                + "a Merry Christmas, and God bless all of you -- all of "
                + "you on the good Earth.\"";
        Text textRef = new SimpleTextBuilder()
        		.text(message)
        		.layoutY(100)
        		.textOrigin(VPos.TOP)
        		.textAlignment(TextAlignment.JUSTIFY)
        		.wrappingWidth(400)
        		.fill(Color.rgb(187, 195, 107))
        		.font(Font.font("SansSerif", FontWeight.BOLD, 24))
        		.build();
 
        
        TranslateTransition transTransition = new TranslateTransition(new Duration(75000), textRef);
        transTransition.setToY(-820);
        transTransition.setInterpolator(Interpolator.LINEAR);
		transTransition.setCycleCount(Animation.INDEFINITE);

        final ImageView image = new ImageView(new Image("http://projavafx.com/images/earthrise.jpg"));

        final Group group = new Group(textRef);
        group.setLayoutX(50);
        group.setLayoutY(180);
        group.setClip(new Rectangle(430, 85));

        Scene scene = new Scene(new Group(image, group));
        stage.setWidth(516);
        stage.setHeight(387);
        stage.setScene(scene);
        stage.setTitle("Hello Earthrise");
        stage.show();
        transTransition.play();
    }
}
