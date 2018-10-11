package audio.mp3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class SongUtils {

    private static final DateTimeFormatter TIME_OF_SECONDS_FORMAT = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalStart().appendLiteral('.')
            .appendValue(ChronoField.MILLI_OF_SECOND, 3).toFormatter();

    private static final String FFMPEG = ResourceFXUtils.getUserFolder("Downloads").getAbsolutePath()
            + "\\ffmpeg-20180813-551a029-win64-static\\bin\\ffmpeg.exe";
    private SongUtils() {
    }

    public static void convertToAudio(File mp4File) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -i \"");
        cmd.append(mp4File);
        cmd.append("\" \"");
        cmd.append(new File(mp4File.getParent(), mp4File.getName().replaceAll("\\..+", ".mp3")));
        cmd.append("\"");
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        ResourceFXUtils.executeInConsole(cmd.toString());
    }


    public static String formatDuration(Duration duration) {
        double millis = duration.toMillis();
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / (1000 * 60));
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String formatDurationMillis(Duration duration) {
        long millis = (long) duration.toMillis();
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60);
        return String.format("%02d:%02d.%03d", minutes, seconds, millis % 1000);
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

    public static void splitAudio(File mp3File, File mp4File, Duration start, Duration end) {
        if (mp4File.exists()) {
            try {
                Files.delete(mp4File.toPath());
            } catch (IOException e) {
                HasLogging.log().error("ERRO AO DELETAR", e);
            }
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -i \"");
        cmd.append(mp3File);
        cmd.append("\" -ss ");
        cmd.append(formatDurationMillis(start));
        cmd.append(" -r 1 -to ");
        cmd.append(formatDurationMillis(end));
        cmd.append(" \"");

        cmd.append(mp4File);
        cmd.append("\"");
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        ResourceFXUtils.executeInConsole(cmd.toString());
    }

    public static void splitAudio(File mp3File, String mp4File, LocalTime start, LocalTime end) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -i ");
        cmd.append(mp3File);
        cmd.append(" -ss ");
        cmd.append(TIME_OF_SECONDS_FORMAT.format(start));
        cmd.append(" -r 1 -to ");
        cmd.append(TIME_OF_SECONDS_FORMAT.format(end));
        cmd.append(" ");
        cmd.append(mp4File);
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        ResourceFXUtils.executeInConsole(cmd.toString());
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
