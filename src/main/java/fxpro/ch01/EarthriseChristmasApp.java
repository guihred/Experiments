package fxpro.ch01;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.SimpleTextBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;

public class EarthriseChristmasApp extends Application {
    private static final double WRAPPING = 400;

    @Override
    public void start(Stage stage) {
        ExtractUtils.insertProxyConfig();
        String message = "Earthrise at Christmas: [Forty] years ago this Christmas, a turbulent world looked to"
            + " the heavens for a unique view of our home planet. This photo of Earthrise over the lunar horizon"
            + " was taken by the Apollo 8 crew in December 1968, showing Earth for the first time as it appears"
            + " from deep space. Astronauts Frank Borman, Jim Lovell and William Anders had become the first"
            + " humans to leave Earth orbit, entering lunar orbit on Christmas Eve. In a historic live broadcast"
            + " that night, the crew took turns reading from the Book of Genesis, closing with a holiday wish from"
            + " Commander Borman: \"We close with good night, good luck, a Merry Christmas, and God bless all of"
            + " you -- all of you on the good Earth.\"";
        Text textRef = new SimpleTextBuilder().text(message).layoutY(100).wrappingWidth(WRAPPING).build();

        TranslateTransition transTransition = new TranslateTransition(Duration.seconds(60), textRef);
        transTransition.setToY(-WRAPPING * 2);
        transTransition.setInterpolator(Interpolator.LINEAR);
        transTransition.setCycleCount(Animation.INDEFINITE);

        final ImageView image = new ImageView(new Image("http://projavafx.com/images/earthrise.jpg"));
        image.setPreserveRatio(true);
        final Group group = new Group(textRef);
        group.setManaged(false);
        group.setLayoutY(180);
        Rectangle value = new Rectangle(WRAPPING + 10, 100);
        group.setClip(value);
        Scene scene = new Scene(new StackPane(image, group));
        image.fitWidthProperty().bind(scene.widthProperty());
        group.setLayoutX(image.getBoundsInParent().getMinX() + image.getBoundsInParent().getWidth() / 2 - WRAPPING / 2);
        group.layoutXProperty()
            .bind(Bindings.createDoubleBinding(
                () -> image.getBoundsInParent().getMinX() + image.getBoundsInParent().getWidth() / 2 - WRAPPING / 2,
                image.boundsInParentProperty()));
        stage.setScene(scene);
        stage.setTitle("Earthrise Christmas");
        stage.show();
        transTransition.play();
        CommonsFX.addCSS(scene, "earthRiseChristmas.css");
    }

    public static void main(String[] args) {

        Application.launch(args);
    }
}
