package japstudy;

import audio.mp3.SongUtils;
import japstudy.db.HibernateUtil;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
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

public class JapaneseLessonAudioSplitDisplay extends JapaneseLessonEditingDisplay {
    public static final long NANO_IN_A_MILLI_SECOND = 1_000_000;

    private int currentState;
    private LocalTime startTime;

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
        Button previous = new Button("P_revious");
		previous.setOnAction(e -> previousLesson());
		previous.disableProperty().bind(current.isEqualTo(0));
        Button save = new Button("_Save and Close");
        save.setOnAction(e -> saveAndClose(primaryStage));

        Button next = new Button("_Next");
		next.setOnAction(e -> nextLesson());
		next.disableProperty().bind(current.isEqualTo(lessons.size() - 1));
        Button split = new Button("Spli_t");
        split.setOnAction(e -> {
            Duration currentDuration = mediaPlayer.get().getCurrentTime();
            LocalTime currentTime = convertDurationToLocalTime(currentDuration);

            if (startTime == null) {
                startTime = currentTime;
                return;
            }
            JapaneseLesson japaneseLesson = lessons.get(current.get());
            JapaneseAudio audio = JapaneseAudio.getAudio(japaneseLesson.getLesson());

            String type = currentState == 0 ? "ing" : "jap";
            LocalTime startTime2 = startTime;
            new Thread(() -> SongUtils.splitAudio(audio.getFile(),
                    String.format("out\\%s%dx%d.mp3", type, japaneseLesson.getLesson(), japaneseLesson.getExercise()),
                    startTime2, currentTime)).start();
            currentState = (currentState + 1) % 2;
            startTime = currentTime;
            if (currentState == 0) {
                nextLesson();
            }

        });
        Button stop = new Button("St_op");
        stop.setOnAction(e -> {
            if (mediaPlayer.get().getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.get().pause();
            }
        });
        Button splay = new Button("_Play");
        splay.setOnAction(e -> playLesson());
        primaryStage.setWidth(600);
		current.set(0);

		primaryStage.centerOnScreen();
		Text currentText = new Text();
		mediaPlayer.addListener((obj, oldM, newO) -> {

			newO.setStartTime(Duration.ZERO);

			StringBinding stringBinding = Bindings.createStringBinding(() -> {
				Duration currentTimeProperty = newO.getCurrentTime();
                LocalTime ofNanoOfDay = convertDurationToLocalTime(currentTimeProperty);
				return TIME_FORMAT.format(ofNanoOfDay);
			}, newO.currentTimeProperty());

			currentText.textProperty().bind(stringBinding);
		});
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
    
    
    private LocalTime convertDurationToLocalTime(Duration currentDuration) {
        int millis = (int) currentDuration.toMillis();
        LocalTime currentTime = LocalTime.of(millis / 1000 / 60 / 60, millis / 1000 / 60 % 60, millis / 1000 % 60);
        return currentTime.plus(millis % 1000, ChronoUnit.MILLIS);
    }

	public static void main(String[] args) {
		launch(args);
	}


}