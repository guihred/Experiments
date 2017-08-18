package japstudy;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;

import japstudy.db.HibernateUtil;
import japstudy.db.JapaneseLesson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JapaneseLessonEditingDisplay extends Application {
	private IntegerProperty current = new SimpleIntegerProperty(1);
	private ObservableList<JapaneseLesson> lessons = getLessons();
	private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
			.appendLiteral('h').appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral('m')
			.appendValue(SECOND_OF_MINUTE, 2).appendLiteral('s').optionalStart().appendValue(MILLI_OF_SECOND, 3)
			.appendLiteral("ms").toFormatter();
	private Media sound = new Media(JapaneseAudio.AUDIO_1.getURL().toString());
	private MediaPlayer mediaPlayer = new MediaPlayer(sound);


	@Override
	public void start(Stage primaryStage) {

		TextField english = newText();
		TextField japanese = newText();
		TextField romaji = newText();
		TextField start = newText();
		TextField end = newText();
		Text lesson = new Text("Lesson");
		current.addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				JapaneseLesson japaneseLesson = lessons.get(newValue.intValue());
				lesson.setText("" + japaneseLesson.getExercise());
				english.setText(japaneseLesson.getEnglish());
				romaji.setText(japaneseLesson.getRomaji());
				japanese.setText(japaneseLesson.getJapanese());
				setStartEnd(japaneseLesson);
				start.setText(TIME_FORMAT.format(lessons.get(current.intValue()).getStart()));
				end.setText(TIME_FORMAT.format(lessons.get(current.intValue()).getEnd()));
			}
		});

		japanese.textProperty().addListener((o, old, newV) -> {
			if (newV != null) {
				JapaneseLesson japaneseLesson = lessons.get(current.intValue());
				japaneseLesson.setJapanese(newV);
			}
		});
		english.textProperty().addListener((o, old, newV) -> {
			if (newV != null) {
				JapaneseLesson japaneseLesson = lessons.get(current.intValue());
				japaneseLesson.setEnglish(newV);
			}
		});
		romaji.textProperty().addListener((o, old, newV) -> {
			if (newV != null) {
				JapaneseLesson japaneseLesson = lessons.get(current.intValue());
				japaneseLesson.setRomaji(newV);
			}
		});
		start.textProperty().addListener((o, old, newV) -> {
			if (newV != null) {
				LocalTime from = LocalTime.from(TIME_FORMAT.parse(newV));
				JapaneseLesson japaneseLesson = lessons.get(current.intValue());
				japaneseLesson.setStart(from);
			}
		});
		end.textProperty().addListener((o, old, newV) -> {
			if (newV != null) {

				LocalTime from = LocalTime.from(TIME_FORMAT.parse(newV));
				JapaneseLesson japaneseLesson = lessons.get(current.intValue());
				japaneseLesson.setEnd(from);
			}
		});
		mediaPlayer.currentTimeProperty().addListener((ob, old, newV) -> {
			Duration endDuration = convertDuration(lessons.get(current.get()).getEnd());
			if (newV.greaterThanOrEqualTo(endDuration)) {
				mediaPlayer.pause();
			}
			// Duration startDuration =
			// convertDuration(lessons.get(current.get()).getStart());
			// if (newV.lessThanOrEqualTo(startDuration)) {
			// mediaPlayer.seek(startDuration);
			// }
			// mediaPlayer.s

		});
		System.out.println(sound.getMetadata());
		Button previous = new Button("Previous");
		previous.setOnAction(e -> previousLesson());
		previous.disableProperty().bind(current.isEqualTo(0));
		Button next = new Button("Next");
		next.setOnAction(e -> nextLesson());
		next.disableProperty().bind(current.isEqualTo(lessons.size() - 1));
		Button play = new Button("Play");
		play.setOnAction(e -> playLesson());
		primaryStage.setWidth(600);
		current.set(0);
		for (int i = 0; i < lessons.size(); i++) {
			if (lessons.get(i).getStart() == null) {
				current.set(i);
				break;
			}
		}

		primaryStage.centerOnScreen();
		HBox hBox = new HBox(lesson);
		Text japaneseText = new Text("Japanese");
		Text romajiText = new Text("Romaji");
		Text current = new Text();
		mediaPlayer.setStartTime(Duration.ZERO);

		StringBinding stringBinding = Bindings.createStringBinding(() -> {
			Duration currentTimeProperty = mediaPlayer.getCurrentTime();
			double millis = currentTimeProperty.toMillis();
			LocalTime ofNanoOfDay = LocalTime.ofNanoOfDay((long) millis * 1000000);
			return TIME_FORMAT.format(ofNanoOfDay);
		}, mediaPlayer.currentTimeProperty());

		current.textProperty().bind(stringBinding);
		Scene value = new Scene(
				new VBox(hBox, english, romajiText, romaji, japaneseText, japanese,
						new HBox(new VBox(new Text("Start"), start), current, new VBox(new Text("End"), end)),
						new HBox(previous, play, next)));
		primaryStage.setScene(value);
		value.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				nextLesson();
			}
		});

		mediaPlayer.play();
		Platform.runLater(() -> {

			mediaPlayer.pause();
		});

		primaryStage.show();
		primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
	}

	private TextField newText() {
		return new TextField();
	}

	private ObservableList<JapaneseLesson> getLessons() {
		return JapaneseLessonReader.getLessons();
	}

	private void nextLesson() {
		JapaneseLesson japaneseLesson = lessons.get(current.get());
		JapaneseLessonReader.update(japaneseLesson);
		current.set((current.get() + 1) % lessons.size());
	}

	private void previousLesson() {
		JapaneseLesson japaneseLesson = lessons.get(current.get());
		JapaneseLessonReader.update(japaneseLesson);
		current.set((lessons.size() + current.get() - 1) % lessons.size());
	}

	private void playLesson() {
		JapaneseLesson japaneseLesson = lessons.get(current.get());
		JapaneseAudio audio = JapaneseAudio.getAudio(japaneseLesson.getLesson());
		if (audio != null) {
			if (!sound.getSource().equals(audio.getURL().toString())) {
				System.out.println("What?" + audio.getURL().toString() + "!=" + sound.getSource());
			}
			setStartEnd(japaneseLesson);
			LocalTime start = japaneseLesson.getStart();
			Duration totalDuration = mediaPlayer.getTotalDuration();
			Duration startDuration = totalDuration.multiply(toMilli(start) / totalDuration.toMillis());
			System.out.println("current:" + mediaPlayer.getCurrentTime());
			System.out.println(" start:" + mediaPlayer.getStartTime());
			System.out.println(" stop:" + mediaPlayer.getStopTime());
			System.out.println(" seek:" + startDuration);
			// sound.get
			mediaPlayer.seek(startDuration);
			// mediaPlayer.onS
			if (mediaPlayer.getStatus() != Status.PLAYING) {
				mediaPlayer.play();
			}
		}

	}

	private Duration convertDuration(LocalTime start) {
		Duration startDuration = Duration
				.valueOf(toMilli(start) + "ms");
		return startDuration;
	}

	private void setStartEnd(JapaneseLesson japaneseLesson) {
		if (japaneseLesson.getStart() == null || japaneseLesson.getEnd() == null
				|| japaneseLesson.getEnd().equals(LocalTime.MIDNIGHT)
				|| japaneseLesson.getStart().equals(LocalTime.MIDNIGHT)) {
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

	private long toMilli(LocalTime maxTime) {
		return ChronoUnit.MILLIS.between(LocalTime.MIDNIGHT, maxTime);
	}

	private LocalTime milliToLocalTime(long offset) {
		return LocalTime.ofNanoOfDay(offset * 1000000);
	}

	public static void main(String[] args) {
		launch(args);
	}

}