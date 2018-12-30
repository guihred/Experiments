package japstudy;

import japstudy.db.HibernateUtil;
import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.CommonsFX;
import utils.SongUtils;

public class JapaneseLessonAudioSplitDisplay extends JapaneseLessonEditingDisplay {


    private int currentState;
    private Duration startTime;

	@Override
    public void setCurrent(JapaneseLesson selectedItem) {
        current.set(lessons.indexOf(selectedItem));
    }


    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Japanese Lesson Audio Split Display");

		TextField english = newText();
		TextField japanese = newText();
		TextField romaji = newText();
		TextField start = newText();
		TextField end = newText();
		Text lesson = new Text("Lesson");
        current.addListener((observable, oldValue, newValue) -> updateCurrentLesson(english, japanese, romaji, start,
                end, lesson, newValue));

        setListeners(english, japanese, romaji, start, end);
        Button previous = CommonsFX.newButton("P_revious", e -> previousLesson());
		previous.disableProperty().bind(current.isEqualTo(0));

        Button next = CommonsFX.newButton("_Next", e -> nextLesson());
		next.disableProperty().bind(current.isEqualTo(lessons.size() - 1));
        Button split = CommonsFX.newButton("Spli_t", e -> splitAudio());
        Button stop = CommonsFX.newButton("St_op", e -> {
            if (mediaPlayer.get().getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.get().pause();
            }
        });
        Button splay = CommonsFX.newButton("_Play", e -> playLesson());
        final int stageWidth = 600;
        primaryStage.setWidth(stageWidth);
		current.set(0);
		primaryStage.centerOnScreen();
		Text currentText = new Text();
		mediaPlayer.addListener((obj, oldM, newO) -> {
			newO.setStartTime(Duration.ZERO);
            currentText.textProperty().bind(Bindings.createStringBinding(
                    () -> SongUtils.formatFullDuration(newO.getCurrentTime()), newO.currentTimeProperty()));
		});
        Button save = CommonsFX.newButton("_Save and Close", e -> saveAndClose(primaryStage));
		Scene value = new Scene(
                new VBox(new HBox(lesson), english, new Text("Romaji"), romaji, new Text("Japanese"), japanese,
						new HBox(new VBox(new Text("Start"), start), currentText, new VBox(new Text("End"), end)),
                        new HBox(previous, splay, stop, split, next, save)));
		primaryStage.setScene(value);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
		value.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				nextLesson();
			}
		});

		mediaPlayer.set(new MediaPlayer(sound));
		primaryStage.show();
	}


    private void splitAudio() {
        Duration currentTime = mediaPlayer.get().getCurrentTime();

        if (startTime == null) {
            startTime = currentTime;
            return;
        }
        JapaneseLesson japaneseLesson = lessons.get(current.get());
        JapaneseAudio audio = JapaneseAudio.getAudio(japaneseLesson.getLesson());

        String type = currentState == 0 ? "ing" : "jap";
        Duration startTime2 = startTime;
        File newFile = new File(
                String.format("out\\%s%dx%d.mp3", type, japaneseLesson.getLesson(), japaneseLesson.getExercise()));
        new Thread(() -> SongUtils.splitAudio(audio.getFile(), newFile, startTime2, currentTime)).start();
        currentState = (currentState + 1) % 2;
        startTime = currentTime;
        if (currentState == 0) {
            nextLesson();
        }
    }
    
    

	public static void main(String[] args) {
		launch(args);
	}


}