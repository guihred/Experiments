package japstudy;

import japstudy.db.JapaneseLesson;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class JapaneseLessonDisplayer extends Stage {
	private ObservableList<JapaneseLesson> lessons;
	private IntegerProperty currentIndex = new SimpleIntegerProperty(1);
	private BooleanProperty tested = new SimpleBooleanProperty(false);
	private DoubleProperty score = new SimpleDoubleProperty(1);

	public JapaneseLessonDisplayer(ObservableList<JapaneseLesson> observableList) {
		Label english = new Label();
		english.setTextAlignment(TextAlignment.CENTER);
		Label japanese = new Label();
		japanese.setTextAlignment(TextAlignment.CENTER);
		Label romaji = new Label();
		romaji.setTextAlignment(TextAlignment.CENTER);
		lessons = observableList;
		currentIndex.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !lessons.isEmpty()) {
				english.setText(lessons.get(newValue.intValue()).getEnglish());
				romaji.setText(lessons.get(newValue.intValue()).getRomaji());
				japanese.setText(lessons.get(newValue.intValue()).getJapanese());
			}
		});
		japanese.visibleProperty().bind(tested);

		TextField answer = new TextField();
        Button next = new Button("_Next");
        next.setOnAction(e -> nextLesson(answer));

		setWidth(400);
		currentIndex.set(0);
		centerOnScreen();
		Label label = new Label(JapaneseLessonApplication.LESSON);
		Label label2 = new Label();
		label2.textProperty().bind(score.multiply(100).asString("%.02f%%"));
		HBox hBox = new HBox(label, new Label(" Score:　"), label2);
		Label japaneseLabel = new Label("Japanese");
		japaneseLabel.visibleProperty().bind(tested);
		Scene value = new Scene(new VBox(hBox, english, new Label("Romaji"), romaji, japaneseLabel, japanese,
				answer, next));
		setScene(value);
		value.setOnKeyPressed(e -> {
			KeyCode code = e.getCode();
			if (code == KeyCode.ENTER) {
				nextLesson(answer);
			}
		});

	}

	private void nextLesson(TextField answer) {
        if (!tested.get() && currentIndex.get() < lessons.size()) {
			tested.set(true);
			String japanese2 = lessons.get(currentIndex.get()).getJapanese();
			String text = answer.getText();
			double compare = CompareAnswers.compare(japanese2, text);
			score.set((score.get() + compare) / 2);
		} else {
            if (!lessons.isEmpty()) {
                currentIndex.set((currentIndex.get() + 1) % lessons.size());
            }
			tested.set(false);
			answer.setText("");
		}
	}

}