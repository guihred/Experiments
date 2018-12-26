package fxsamples;

import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;

public class ChangingTextFonts extends Application {
	private static final Logger LOG = HasLogging.log();
	private static final String INTRO_TEXT = "JavaFX 8: Intro. by Example";

    @Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Changing Text Fonts");
        String families = Font.getFamilies().stream().collect(Collectors.joining("\n\t", "\n\t", ""));
        LOG.info("Font families: {}", families);
        String fontNames = Font.getFontNames().stream().collect(Collectors.joining("\n\t", "\n\t", ""));
        LOG.info("Font names: {}", fontNames);
		Group root = new Group();
		Scene scene = new Scene(root, 580, 250, Color.WHITE);
		// Serif with drop shadow
        Text text2 = new Text(50, 50, INTRO_TEXT);
		Font serif = Font.font("Serif", 30);
		text2.setFont(serif);
		text2.setFill(Color.RED);
		DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setColor(Color.rgb(50, 50, 50, .5));
		text2.setEffect(dropShadow);
		root.getChildren().add(text2);
		// SanSerif
        Text text3 = new Text(50, 100, INTRO_TEXT);
		Font sanSerif = Font.font("SanSerif", 30);
		text3.setFont(sanSerif);
		text3.setFill(Color.BLUE);
		root.getChildren().add(text3);
		// Dialog
        Text text4 = new Text(50, 150, INTRO_TEXT);
		Font dialogFont = Font.font("Dialog", 30);
		text4.setFont(dialogFont);
		text4.setFill(Color.rgb(0, 255, 0));
		root.getChildren().add(text4);
		// Monospaced
        Text text5 = new Text(50, 200, INTRO_TEXT);
		Font monoFont = Font.font("Monospaced", 30);
		text5.setFont(monoFont);
		text5.setFill(Color.BLACK);
		root.getChildren().add(text5);
		// Reflection
		Reflection refl = new Reflection();
        refl.setFraction(4. / 5);
		refl.setTopOffset(5);
        text5.setEffect(refl);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
