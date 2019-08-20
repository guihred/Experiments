package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
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
import org.slf4j.Logger;
import simplebuilder.SimpleSliderBuilder;

public final class SongUtils {

    private static final int SECONDS_IN_A_MINUTE = 60;

    private static final Logger LOG = HasLogging.log();

	private static final DateTimeFormatter TIME_OF_SECONDS_FORMAT = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(':')
			.appendValue(ChronoField.SECOND_OF_MINUTE, 2).appendLiteral('.')
			.appendValue(ChronoField.MILLI_OF_SECOND, 2)
			.toFormatter();

    private static final String FFMPEG = ResourceFXUtils.getUserFolder("Downloads").getAbsolutePath()
            + "\\ffmpeg-20180813-551a029-win64-static\\bin\\ffmpeg.exe";
    private SongUtils() {
    }

    public static Slider addSlider(VBox flow, MediaPlayer mediaPlayer2) {
        Slider slider = new SimpleSliderBuilder(0, 1, 0).blocks(100_000).build();
        Label label = new Label("00:00");

        label.textProperty()
            .bind(Bindings.createStringBinding(
                () -> mediaPlayer2.getTotalDuration() == null ? "00:00"
                    : SongUtils.formatFullDuration(mediaPlayer2.getTotalDuration().multiply(slider.getValue())),
                slider.valueProperty(), mediaPlayer2.totalDurationProperty()));

        flow.getChildren().add(label);
        flow.getChildren().add(slider);
        return slider;
    }

    public static DoubleProperty convertToAudio(File mp4File) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -i \"");
        cmd.append(mp4File);
        cmd.append("\" \"");
        File obj = new File(mp4File.getParent(), mp4File.getName().replaceAll("\\..+", ".mp3"));
        if (obj.exists()) {
            try {
                Files.delete(obj.toPath());
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
        cmd.append(obj);
        cmd.append("\"");
        Map<String, String> responses = new HashMap<>();
        String key = "size=\\s*.+ time=(.+) bitrate=.+";

        responses.put(key, "$1");
        String key2 = "\\s*Duration: ([\\.:\\d]+),.+";
        responses.put(key2, "$1");
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        Map<String, ObservableList<String>> executeInConsoleAsync = ConsoleUtils
                .executeInConsoleAsync(cmd.toString(), responses);
        
        return ConsoleUtils.defineProgress(key2, key, executeInConsoleAsync, SongUtils::convertTimeToMillis);
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
            try {
                Files.delete(outFile.toPath());
            } catch (IOException e) {
                LOG.error("ERRO AO DELETAR", e);
            }
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -i \"");
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
        Map<String, ObservableList<String>> executeInConsoleAsync = ConsoleUtils
				.executeInConsoleAsync(cmd.toString(), responses);
        return ConsoleUtils.defineProgress(duration, key, executeInConsoleAsync,
                s -> Math.abs(end.subtract(start).toMillis()), SongUtils::convertTimeToMillis);
    }

    public static void updateCurrentSlider(MediaPlayer mediaPlayer2, Slider currentSlider) {
        if (!currentSlider.isValueChanging()) {
            Duration currentTime = mediaPlayer2.getCurrentTime();
            Duration totalDuration = mediaPlayer2.getTotalDuration();
            double value = currentTime.toMillis() / totalDuration.toMillis();
            currentSlider.setValue(value);
        }
    }

    public static void updateMediaPlayer(MediaPlayer mediaPlayer2, Slider currentSlider, boolean valueChanging) {
        if (!valueChanging) {
            double pos = currentSlider.getValue();
            final Duration seekTo = mediaPlayer2.getTotalDuration().multiply(pos);
            SongUtils.seekAndUpdatePosition(seekTo, currentSlider, mediaPlayer2);
        }
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

    private static long convertTimeToMillis(String text) {
		return ChronoUnit.MILLIS.between(LocalTime.MIN, TIME_OF_SECONDS_FORMAT.parse(text, LocalTime::from));
	}

}
