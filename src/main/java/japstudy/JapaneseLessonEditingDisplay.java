package japstudy;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.CommonsFX;
import utils.HasLogging;

public class JapaneseLessonEditingDisplay extends Application implements HasLogging {
    public static final long NANO_IN_A_MILLI_SECOND = 1_000_000;
    protected static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendLiteral('.')
            .optionalStart().appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();
    protected SimpleIntegerProperty current = new SimpleIntegerProperty(1);
    protected ObservableList<JapaneseLesson> lessons = getLessons();
    protected Media sound = new Media(JapaneseAudio.AUDIO_1.getURL().toString());
    protected SimpleObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<>();

    public SimpleIntegerProperty currentProperty() {
        return current;
    }
	public void setCurrent(JapaneseLesson selectedItem) {
        current.set(lessons.indexOf(selectedItem));
    }

    @Override
	public void start(Stage primaryStage) {

        primaryStage.setTitle("Japanese Lesson Editing Display");
        TextField japanese = newText();
		TextField english = newText();
		TextField start = newText();
		TextField end = newText();
        TextField romaji = newText();
		Text lesson = new Text("Lesson");
        current.addListener((observable, oldValue, newValue) -> updateCurrentLesson(english, japanese, romaji, start,
                end, lesson, newValue));

        setListeners(english, japanese, romaji, start, end);
        Button previous = CommonsFX.newButton("P_revious", e -> previousLesson());
		previous.disableProperty().bind(current.isEqualTo(0));


        Button next = CommonsFX.newButton("_Next", e -> nextLesson());
		next.disableProperty().bind(current.isEqualTo(lessons.size() - 1));

		primaryStage.setWidth(600);
		current.set(0);
		for (int i = 0; i < lessons.size(); i++) {
			if (lessons.get(i).getStart() == null) {
				current.set(i);
				break;
			}
		}

		primaryStage.centerOnScreen();
		Text currentText = new Text();
		mediaPlayer.addListener((obj, oldM, newO) -> {

			newO.currentTimeProperty().addListener((ob, old, newV) -> {
				if (newV.greaterThanOrEqualTo(convertDuration(lessons.get(current.get()).getEnd()))) {
					newO.pause();
				}
			});
			newO.setStartTime(Duration.ZERO);

			currentText.textProperty().bind(Bindings.createStringBinding(() -> {
				Duration currentTimeProperty = newO.getCurrentTime();
                long millis = (long) currentTimeProperty.toMillis();
                LocalTime ofNanoOfDay = LocalTime
                        .ofNanoOfDay(millis * JapaneseLessonAudioSplitDisplay.NANO_IN_A_MILLI_SECOND);
				return TIME_FORMAT.format(ofNanoOfDay);
			}, newO.currentTimeProperty()));
		});
        Button play = CommonsFX.newButton("_Play", e -> playLesson());
        Button save = CommonsFX.newButton("_Save and Close", e -> saveAndClose(primaryStage));
		Scene value = new Scene(
				new VBox(new HBox(lesson), english, new Text("Romaji"), romaji, new Text("Japanese"), japanese,
						new HBox(new VBox(new Text("Start"), start), currentText, new VBox(new Text("End"), end)),
                        new HBox(previous, play, next, save)));
		primaryStage.setScene(value);
		value.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				nextLesson();
			}
		});

		mediaPlayer.set(new MediaPlayer(sound));
		primaryStage.show();
	}

    protected Duration convertDuration(LocalTime start) {
		return Duration
				.valueOf(toMilli(start) + "ms");
	}

    protected TextField newText() {
		return new TextField();
	}

    protected void nextLesson() {
		JapaneseLesson japaneseLesson = lessons.get(current.get());
		JapaneseLessonReader.update(japaneseLesson);
		current.set((current.get() + 1) % lessons.size());
	}

    protected void playLesson() {
		JapaneseLesson japaneseLesson = lessons.get(current.get());
		JapaneseAudio audio = JapaneseAudio.getAudio(japaneseLesson.getLesson());
		if (audio != null) {
			if (!sound.getSource().equals(audio.getURL().toString())) {
				sound = new Media(audio.getURL().toString());
				mediaPlayer.set(new MediaPlayer(sound));
			}
			setStartEnd(japaneseLesson);
			LocalTime start = japaneseLesson.getStart();
			Duration totalDuration = mediaPlayer.get().getTotalDuration();
			Duration startDuration = totalDuration.multiply(toMilli(start) / totalDuration.toMillis());

            getLogger().info("current:{}", mediaPlayer.get().getCurrentTime());
            getLogger().info(" start:{}", mediaPlayer.get().getStartTime());
            getLogger().info(" stop:{}", mediaPlayer.get().getStopTime());
            getLogger().info(" seek: {}", startDuration);
			// sound.get
			mediaPlayer.get().seek(startDuration);
			// mediaPlayer.onS
            if (mediaPlayer.get().getStatus() != MediaPlayer.Status.PLAYING) {
				mediaPlayer.get().play();
			}
		}

	}

	/*	
			*/

    protected void previousLesson() {
		JapaneseLesson japaneseLesson = lessons.get(current.get());
		JapaneseLessonReader.update(japaneseLesson);
		current.set((lessons.size() + current.get() - 1) % lessons.size());
	}

    protected void saveAndClose(Stage primaryStage) {
        int index = current.get();
        JapaneseLesson japaneseLesson = lessons.get(index);
        JapaneseLessonReader.update(japaneseLesson);
        lessons.set(index, japaneseLesson);

        primaryStage.close();
    }

    protected void setDateField(String newV, BiConsumer<JapaneseLesson, LocalTime> a) {
		if (newV != null) {
			LocalTime from = LocalTime.from(TIME_FORMAT.parse(newV));
			JapaneseLesson japaneseLesson = lessons.get(current.intValue());
			a.accept(japaneseLesson, from);
		}
	}

    protected void setListeners(TextField english, TextField japanese, TextField romaji, TextField start,
            TextField end) {
        japanese.textProperty().addListener((o, old, newV) -> setTextField(newV, JapaneseLesson::setJapanese));
		english.textProperty().addListener((o, old, newV) -> setTextField(newV, JapaneseLesson::setEnglish));
		romaji.textProperty().addListener((o, old, newV) -> setTextField(newV, JapaneseLesson::setRomaji));
		start.textProperty().addListener((o, old, newV) -> setDateField(newV, JapaneseLesson::setStart));
		end.textProperty().addListener((o, old, newV) -> setDateField(newV, JapaneseLesson::setEnd));
    }

    protected void setStartEnd(JapaneseLesson japaneseLesson) {
		if (japaneseLesson.getStart() == null 
		        || japaneseLesson.getEnd() == null
                || LocalTime.MIDNIGHT.equals(japaneseLesson.getEnd())
                || LocalTime.MIDNIGHT.equals(japaneseLesson.getStart())) {
			double millis = sound.getDuration().toMillis();
			if (!Double.isNaN(millis)) {
				Long countExercises = JapaneseLessonReader.getCountExerciseByLesson(japaneseLesson.getLesson());
				LocalTime maxTime = JapaneseLessonReader.getMaxTimeLesson(japaneseLesson.getLesson(),
						japaneseLesson.getExercise());
				long offset = maxTime == null ? 0 : toMilli(maxTime);

				long l = countExercises - japaneseLesson.getExercise() - 1;
				long nanoOfDay = (long) (millis - offset) / (l <= 0 ? 1 : l);
				LocalTime start = milliToLocalTime(offset);
				LocalTime end = milliToLocalTime(offset + nanoOfDay);
				japaneseLesson.setStart(start);
				japaneseLesson.setEnd(end);

			} else {
				japaneseLesson.setStart(LocalTime.MIDNIGHT);
				japaneseLesson.setEnd(LocalTime.MIDNIGHT);
			}
		}
	}

    protected void setTextField(String newV, BiConsumer<JapaneseLesson, String> a) {
		if (newV != null) {
			JapaneseLesson japaneseLesson = lessons.get(current.intValue());
			a.accept(japaneseLesson, newV);
		}
	}

    protected void updateCurrentLesson(TextField english, TextField japanese, TextField romaji, TextField start,
            TextField end,
            Text lesson, Number newValue) {
        if (newValue != null && !lessons.isEmpty()) {
            JapaneseLesson japaneseLesson = lessons.get(newValue.intValue() % lessons.size());
        	lesson.setText("" + japaneseLesson.getExercise());
            romaji.setText(japaneseLesson.getRomaji());
        	english.setText(japaneseLesson.getEnglish());
        	japanese.setText(japaneseLesson.getJapanese());
        	setStartEnd(japaneseLesson);
        	start.setText(TIME_FORMAT.format(lessons.get(current.intValue()).getStart()));
        	end.setText(TIME_FORMAT.format(lessons.get(current.intValue()).getEnd()));
        }
    }

    public static void main(String[] args) {
		launch(args);
	}

    protected static ObservableList<JapaneseLesson> getLessons() {
        return JapaneseLessonReader.getLessonsWait();
	}

	protected static LocalTime milliToLocalTime(long offset) {
        return LocalTime.ofNanoOfDay(offset * JapaneseLessonAudioSplitDisplay.NANO_IN_A_MILLI_SECOND);
	}

    protected static long toMilli(LocalTime maxTime) {
		return ChronoUnit.MILLIS.between(LocalTime.MIDNIGHT, maxTime);
	}

}