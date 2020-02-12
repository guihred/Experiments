package japstudy;

import static utils.CommonsFX.onCloseWindow;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.BiConsumer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.CommonsFX;
import utils.DateFormatUtils;
import utils.HibernateUtil;
import utils.StringSigaUtils;

public class JapaneseLessonEditingDisplay extends Application {

    private static final long NANO_IN_A_MILLI_SECOND = 1000;

    @FXML
    private Button previous;

    @FXML
    private TextField english;
    @FXML
    private Text lesson;
    @FXML
    private Text text1;
    @FXML
    private TextField romaji;
    @FXML
    private TextField japanese;
    @FXML
    private TextField start;
    @FXML
    private Button next;
    @FXML
    private Button play;
    @FXML
    private Text currentText;
    @FXML
    private TextField end;
    protected ObservableList<JapaneseLesson> lessons = JapaneseLessonReader.getLessonsWait();
    protected SimpleIntegerProperty current = new SimpleIntegerProperty(1);
    protected Media sound = new Media(JapaneseAudio.AUDIO_1.getURL().toString());
    protected SimpleObjectProperty<MediaPlayer> mediaPlayer = new SimpleObjectProperty<>();
    private Stage stage;

    public SimpleIntegerProperty currentProperty() {
        return current;
    }

    public void initialize() {
        current.addListener((observable, oldValue, newValue) -> updateCurrentLesson(english, japanese, romaji, start,
            end, lesson, newValue));
        setListeners(english, japanese, romaji, start, end);
        previous.disableProperty().bind(current.isEqualTo(0));
        next.disableProperty().bind(current.isEqualTo(lessons.size() - 1));
        current.set(0);
        for (int i = 0; i < lessons.size(); i++) {
            if (lessons.get(i).getStart() == null) {
                current.set(i);
                break;
            }
        }
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
                    .ofNanoOfDay(millis * JapaneseLessonEditingDisplay.NANO_IN_A_MILLI_SECOND);
                return DateFormatUtils.format(ofNanoOfDay);
            }, newO.currentTimeProperty()));
        });
        mediaPlayer.set(new MediaPlayer(sound));
    }

    public void nextLesson() {
        JapaneseLesson japaneseLesson = lessons.get(current.get());
        JapaneseLessonReader.update(japaneseLesson);
        current.set((current.get() + 1) % lessons.size());
    }

    public void onActionNext() {
        nextLesson();
    }

    public void onActionPlay() {
        playLesson();
    }

    public void onActionPrevious() {
        previousLesson();
    }

    public void onActionSaveandClose() {
        saveAndClose();
    }

    public void setCurrent(JapaneseLesson selectedItem) {
        current.set(lessons.indexOf(selectedItem));
    }

    public void splitAudio() {
//        DOES NOTHING
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        CommonsFX.loadFXML(
            StringSigaUtils.splitMargeCamelCase(getClass().getSimpleName()),
            "JapaneseLessonEditingDisplay.fxml", this, stage);
        stage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                nextLesson();
            }
        });
        onCloseWindow(primaryStage, () -> HibernateUtil.shutdown());
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
            LocalTime start1 = japaneseLesson.getStart();
            Duration totalDuration = mediaPlayer.get().getTotalDuration();
            Duration startDuration = totalDuration.multiply(toMilli(start1) / totalDuration.toMillis());
            mediaPlayer.get().seek(startDuration);
            if (mediaPlayer.get().getStatus() != MediaPlayer.Status.PLAYING) {
                mediaPlayer.get().play();
            }
        }

    }

    protected void previousLesson() {
        JapaneseLesson japaneseLesson = lessons.get(current.get());
        JapaneseLessonReader.update(japaneseLesson);
        current.set((lessons.size() + current.get() - 1) % lessons.size());
    }

    protected void saveAndClose() {
        int index = current.get();
        JapaneseLesson japaneseLesson = lessons.get(index);
        JapaneseLessonReader.update(japaneseLesson);
        lessons.set(index, japaneseLesson);
        stage.close();
    }

    protected void setDateField(String newV, BiConsumer<JapaneseLesson, LocalTime> a) {
        if (newV != null) {
            LocalTime from = LocalTime.from(DateFormatUtils.parse(newV));
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
        if (japaneseLesson.getStart() == null || japaneseLesson.getEnd() == null
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
                LocalTime start1 = milliToLocalTime(offset);
                LocalTime end1 = milliToLocalTime(offset + nanoOfDay);
                japaneseLesson.setStart(start1);
                japaneseLesson.setEnd(end1);

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

    protected void updateCurrentLesson(TextField english1, TextField japanese1, TextField romaji1, TextField start1,
        TextField end1, Text lesson1, Number newValue) {
        if (newValue != null && !lessons.isEmpty()) {
            JapaneseLesson japaneseLesson = lessons.get(newValue.intValue() % lessons.size());
            lesson1.setText("" + japaneseLesson.getExercise());
            romaji1.setText(japaneseLesson.getRomaji());
            english1.setText(japaneseLesson.getEnglish());
            japanese1.setText(japaneseLesson.getJapanese());
            setStartEnd(japaneseLesson);
            start1.setText(DateFormatUtils.format(lessons.get(current.intValue()).getStart()));
            end1.setText(DateFormatUtils.format(lessons.get(current.intValue()).getEnd()));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    protected static Duration convertDuration(LocalTime start) {
        return Duration.valueOf(toMilli(start) + "ms");
    }

    protected static LocalTime milliToLocalTime(long offset) {
        return LocalTime.ofNanoOfDay(offset * NANO_IN_A_MILLI_SECOND);
    }

    protected static long toMilli(LocalTime maxTime) {
        return ChronoUnit.MILLIS.between(LocalTime.MIDNIGHT, maxTime);
    }
}
