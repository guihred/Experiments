package japstudy;

import extract.SongUtils;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;

public class JapaneseLessonAudioSplitDisplay extends JapaneseLessonEditingDisplay {

    private Duration startTime;
    private int currentState;
    @FXML
    private Button split;
    @Override
    public void splitAudio() {
        Duration currentTime = mediaPlayer.get().getCurrentTime();
        if (startTime == null) {
            startTime = currentTime;
            return;
        }
        JapaneseLesson japaneseLesson = lessons.get(current.get());
        JapaneseAudio audio = JapaneseAudio.getAudio(japaneseLesson.getLesson());

        String type = currentState == 0 ? "ing" : "jap";
        String format =
                String.format("mp3/%s%dx%d.mp3", type, japaneseLesson.getLesson(), japaneseLesson.getExercise());
        File newFile = ResourceFXUtils.getOutFile(format);
        RunnableEx.runNewThread(() -> SongUtils.splitAudio(audio.getFile(), newFile, startTime, currentTime));
        startTime = currentTime;
        currentState = (currentState + 1) % 2;
        if (currentState == 0) {
            nextLesson();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        split.setVisible(true);
    }

    public static void main(String[] args) {
        launch(args);
    }

}