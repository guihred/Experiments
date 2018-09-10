package japstudy;

import japstudy.db.HibernateUtil;
import japstudy.db.JapaneseLesson;
import java.util.Random;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class JapaneseLessonDisplay extends Application {
	private IntegerProperty current = new SimpleIntegerProperty(1);
	private ObservableList<JapaneseLesson> lessons = getLessons();
	private DoubleProperty score = new SimpleDoubleProperty(1);
	private BooleanProperty tested = new SimpleBooleanProperty(false);
	private CheckBox showRomaji = new CheckBox();
	@Override
	public void start(Stage primaryStage) {
		Text english = newText();
		Text japanese = new Text();
		japanese.setTextAlignment(TextAlignment.CENTER);
		Text romaji = newText();
		Text lesson = new Text("Lesson");
		current.addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				lesson.setText("Lesson: " + lessons.get(newValue.intValue()).getLesson());
				english.setText(lessons.get(newValue.intValue()).getEnglish());
				romaji.setText(lessons.get(newValue.intValue()).getRomaji());
				japanese.setText(lessons.get(newValue.intValue()).getJapanese());

			}
		});
		japanese.visibleProperty().bind(tested);
		TextField answer = new TextField();
		Button next = new Button("Next");
		next.setOnAction(e -> nextLesson(answer));
		primaryStage.setWidth(600);
        if (!lessons.isEmpty()) {
            current.set(new Random().nextInt(lessons.size()));
        }
		primaryStage.centerOnScreen();
		Text scoreText = new Text();
		scoreText.textProperty().bind(score.multiply(100).asString(" Score: %.02f%%"));
		HBox hBox = new HBox(lesson, scoreText, showRomaji);
		Text japaneseText = new Text("Japanese");
		japaneseText.visibleProperty().bind(tested);
		Text romajiText = new Text("Romaji");
		romajiText.visibleProperty().bind(showRomaji.selectedProperty());
		romaji.visibleProperty().bind(showRomaji.selectedProperty());
		Scene value = new Scene(new VBox(hBox, english, romajiText, romaji, japaneseText, japanese, answer,
				next));
		primaryStage.setScene(value);
		value.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				nextLesson(answer);
			}
		});
		primaryStage.show();
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
	}

	private Text newText() {
		Text romaji = new Text();
		romaji.setTextAlignment(TextAlignment.CENTER);
		romaji.setWrappingWidth(500);
		return romaji;
	}

	private ObservableList<JapaneseLesson> getLessons() {
		return JapaneseLessonReader.getLessons();
	}

	private void nextLesson(TextField answer) {
		if (!tested.get()) {
			tested.set(true);
            String text = answer.getText();
			String japanese2 = lessons.get(current.get()).getJapanese();
			double compare = CompareAnswers.compare(japanese2, text);
			score.set((score.get() + compare) / 2);
		} else {
			current.set((current.get() + 1) % lessons.size());
			tested.set(false);
			answer.setText("");
			showRomaji.setSelected(false);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}