package japstudy;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import japstudy.db.HibernateUtil;
import japstudy.db.JapaneseLesson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;

public class JapaneseLessonAudioSplitDisplay extends Application implements HasLogging {
    private static final String FFMPEG = "C:\\Users\\guilherme.hmedeiros\\Downloads\\ffmpeg-20180813-551a029-win64-static\\bin\\ffmpeg.exe";
    private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder().appendValue(HOUR_OF_DAY, 2)
            .appendLiteral('h').appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral('m')
            .appendValue(SECOND_OF_MINUTE, 2).appendLiteral('s').optionalStart().appendValue(MILLI_OF_SECOND, 3)
            .appendLiteral("ms").toFormatter();
    private static final DateTimeFormatter TIME_OF_SECONDS_FORMAT = new DateTimeFormatterBuilder()
            .appendValue(MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2).optionalStart().appendLiteral('.')
            .appendValue(MILLI_OF_SECOND, 3)
            .toFormatter();

    private IntegerProperty current = new SimpleIntegerProperty(1);
    private ObservableList<JapaneseLesson> lessons = getLessons();
	private Media sound = new Media(JapaneseAudio.AUDIO_1.getURL().toString());
	private ObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<>();
    private int currentState;
    private LocalTime startTime;

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
                romaji.setText(japaneseLesson.getRomaji());
				english.setText(japaneseLesson.getEnglish());
				japanese.setText(japaneseLesson.getJapanese());
				setStartEnd(japaneseLesson);
				start.setText(TIME_FORMAT.format(lessons.get(current.intValue()).getStart()));
				end.setText(TIME_FORMAT.format(lessons.get(current.intValue()).getEnd()));
			}
		});

		japanese.textProperty().addListener((o, old, newV) -> setTextField(newV, JapaneseLesson::setJapanese));
		english.textProperty().addListener((o, old, newV) -> setTextField(newV, JapaneseLesson::setEnglish));
		romaji.textProperty().addListener((o, old, newV) -> setTextField(newV, JapaneseLesson::setRomaji));
		start.textProperty().addListener((o, old, newV) -> setDateField(newV, JapaneseLesson::setStart));
		end.textProperty().addListener((o, old, newV) -> setDateField(newV, JapaneseLesson::setEnd));
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
            new Thread(() -> splitAudio(audio.getFile(),
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
            if (mediaPlayer.get().getStatus() == Status.PLAYING) {
                mediaPlayer.get().pause();
            }
        });
        Button splay = new Button("_Play");
        splay.setOnAction(e -> playLesson());
        primaryStage.setWidth(600);
		current.set(0);

		primaryStage.centerOnScreen();
		HBox hBox = new HBox(lesson);
		Text japaneseText = new Text("Japanese");
		Text romajiText = new Text("Romaji");
		Text currentText = new Text();
		mediaPlayer.addListener((obj, oldM, newO) -> {

			newO.currentTimeProperty().addListener((ob, old, newV) -> {

			});
			newO.setStartTime(Duration.ZERO);

			StringBinding stringBinding = Bindings.createStringBinding(() -> {
				Duration currentTimeProperty = newO.getCurrentTime();
                LocalTime ofNanoOfDay = convertDurationToLocalTime(currentTimeProperty);
				return TIME_FORMAT.format(ofNanoOfDay);
			}, newO.currentTimeProperty());

			currentText.textProperty().bind(stringBinding);
		});
		Scene value = new Scene(
				new VBox(hBox, english, romajiText, romaji, japaneseText, japanese,
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

    public IntegerProperty currentProperty() {
        return current;
    }
    
    
    
    private void splitAudio(String mp3File, String mp4File, LocalTime start, LocalTime end) {
        try {

            StringBuilder cmd = new StringBuilder();
            cmd.append(FFMPEG + " -i ");
            cmd.append(ResourceFXUtils.toFile(mp3File));
            cmd.append(" -ss ");
            cmd.append(TIME_OF_SECONDS_FORMAT.format(start));
            cmd.append(" -r 1 -to ");
            cmd.append(TIME_OF_SECONDS_FORMAT.format(end));
            cmd.append(" ");
            cmd.append(mp4File);
            // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
            System.out.println(cmd);
            Process p = Runtime.getRuntime().exec(cmd.toString());
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("Success")) {
                    System.out.println(line);
                    System.out.println("file " + mp4File + " successfull");
                }
            }
            p.waitFor();
            in.close();
        } catch (Exception e) {
            getLogger().error("", e);
        }

    }

    private void saveAndClose(Stage primaryStage) {
        int index = current.get();
        JapaneseLesson japaneseLesson = lessons.get(index);
        JapaneseLessonReader.update(japaneseLesson);
        lessons.set(index, japaneseLesson);

        primaryStage.close();
    }

	private void setTextField(String newV, BiConsumer<JapaneseLesson, String> a) {
		if (newV != null) {
			JapaneseLesson japaneseLesson = lessons.get(current.intValue());
			a.accept(japaneseLesson, newV);
		}
	}

	private void setDateField(String newV, BiConsumer<JapaneseLesson, LocalTime> a) {
		if (newV != null) {
			LocalTime from = LocalTime.from(TIME_FORMAT.parse(newV));
			JapaneseLesson japaneseLesson = lessons.get(current.intValue());
			a.accept(japaneseLesson, from);
		}
	}

	/*	
			*/

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
			if (mediaPlayer.get().getStatus() != Status.PLAYING) {
				mediaPlayer.get().play();
			}
		}

	}

	private Duration convertDuration(LocalTime start) {
		return Duration
				.valueOf(toMilli(start) + "ms");
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

    public void setCurrent(JapaneseLesson selectedItem) {
        current.set(lessons.indexOf(selectedItem));
    }

}