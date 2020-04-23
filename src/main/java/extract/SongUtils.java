package extract;

import static utils.RunnableEx.ignore;
import static utils.RunnableEx.runIf;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import simplebuilder.SimpleSliderBuilder;
import utils.ConsoleUtils;
import utils.DateFormatUtils;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class SongUtils {

    private static final int SECONDS_IN_A_MINUTE = 60;


    private static final String FFMPEG = ResourceFXUtils.getUserFolder("Downloads").getAbsolutePath()
        + "\\ffmpeg-20180813-551a029-win64-static\\bin\\ffmpeg.exe";

    private SongUtils() {
    }

    public static Slider addSlider(VBox flow, MediaPlayer mediaPlayer2) {
        Slider slider = new SimpleSliderBuilder(0, 1, 0).blocks(100_000).build();
        Label label = new Label("00:00");

        bindSlider(mediaPlayer2, slider, label);

        flow.getChildren().add(label);
        flow.getChildren().add(slider);
        return slider;
    }

    public static void bindSlider(MediaPlayer mediaPlayer2, Slider slider, Label label) {
        label.textProperty()
            .bind(Bindings.createStringBinding(
                () -> mediaPlayer2.getTotalDuration() == null ? "00:00"
                    : SongUtils.formatFullDuration(mediaPlayer2.getTotalDuration().multiply(slider.getValue())),
                slider.valueProperty(), mediaPlayer2.totalDurationProperty()));
    }

    public static DoubleProperty convertToAudio(File mp4File) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -y -i \"");
        cmd.append(mp4File);
        cmd.append("\" \"");
        File obj = new File(mp4File.getParent(), mp4File.getName().replaceAll("\\..+", ".mp3"));
        if (obj.exists()) {
            RunnableEx.run(() -> Files.delete(obj.toPath()));
        }
        cmd.append(obj);
        cmd.append("\"");
        Map<String, String> responses = new HashMap<>();
        String key = "size=\\s*.+ time=(.+) bitrate=.+";

        responses.put(key, "$1");
        String key2 = "\\s*Duration: ([\\.:\\d]+),.+";
        responses.put(key2, "$1");
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        Map<String, ObservableList<String>> executeInConsoleAsync = ConsoleUtils.executeInConsoleAsync(cmd.toString(),
            responses);

        return ConsoleUtils.defineProgress(key2, key, executeInConsoleAsync, DateFormatUtils::convertTimeToMillis);
    }

    public static String formatDuration(Duration duration) {
        double millis = duration.toMillis();
        int seconds = (int) (millis / 1000) % SECONDS_IN_A_MINUTE;
        int minutes = (int) (millis / (1000 * SECONDS_IN_A_MINUTE));
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatFullDuration(Duration duration) {
        long millis = (long) duration.toMillis();
        long seconds = millis / 1000 % SECONDS_IN_A_MINUTE;
        long minutes = millis / (1000 * SECONDS_IN_A_MINUTE) % SECONDS_IN_A_MINUTE;
        long hours = millis / (1000 * SECONDS_IN_A_MINUTE) / SECONDS_IN_A_MINUTE;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis % 1000);
    }

    public static void seekAndUpdatePosition(Duration duration, Slider slider, MediaPlayer mediaPlayer) {
        if (mediaPlayer.getStatus() == Status.STOPPED) {
            mediaPlayer.pause();
        }
        mediaPlayer.seek(duration);
        if (mediaPlayer.getStatus() != Status.PLAYING) {
            updatePositionSlider(duration, slider, mediaPlayer);
        }
    }

    public static DoubleProperty splitAudio(File inFile, File outFile, Duration start, Duration end) {
        if (outFile.exists()) {
            ignore(() -> Files.delete(outFile.toPath()));
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -y -i \"");
        cmd.append(inFile);
        cmd.append("\" -ss ");
        cmd.append(formatFullDuration(start));
        cmd.append(" -r 1 -to ");
        cmd.append(formatFullDuration(end));
        cmd.append(" \"");

        cmd.append(outFile);
        cmd.append("\"");
        Map<String, String> responses = new HashMap<>();
        String duration = "\\s*Duration: ([^,]+),.+";
        String key = "size=\\s.+ time=(.+) bitrate=.+";
        responses.put(duration, "$1");
        responses.put(key, "$1");
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        Map<String, ObservableList<String>> executeInConsoleAsync = ConsoleUtils.executeInConsoleAsync(cmd.toString(),
            responses);
        return ConsoleUtils.defineProgress(duration, key, executeInConsoleAsync,
            s -> Math.abs(end.subtract(start).toMillis()), DateFormatUtils::convertTimeToMillis);
    }

    public static void stopAndDispose(MediaPlayer mediaPlayer) {
        runIf(mediaPlayer, t -> {
            if (t.getStatus() == Status.PLAYING) {
                t.stop();
            }
            t.dispose();
        });
    }

    public static void updateMediaPlayer(MediaPlayer mediaPlayer2, Slider currentSlider, double pos) {
        SongUtils.seekAndUpdatePosition(mediaPlayer2.getTotalDuration().multiply(pos), currentSlider, mediaPlayer2);
    }

    public static void updatePositionSlider(Duration currentTime, Slider positionSlider,
        final MediaPlayer mediaPlayer) {
        if (positionSlider.isValueChanging()) {
            return;
        }
        final Duration total = mediaPlayer.getTotalDuration();
        if (total == null || currentTime == null) {
            positionSlider.setValue(0);
        } else {
            positionSlider.setValue(currentTime.toMillis() / total.toMillis());
        }
    }

}
