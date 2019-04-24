package utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import org.blinkenlights.jid3.ID3Tag;
import org.blinkenlights.jid3.MP3File;
import org.blinkenlights.jid3.v2.APICID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Frame;
import org.blinkenlights.jid3.v2.ID3V2Tag;
import org.blinkenlights.jid3.v2.ID3V2_3_0Tag;
import org.slf4j.Logger;

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
        String key = "size=\\s.+ time=(.+) bitrate=.+";
        responses.put(key, "$1");
        String key2 = "\\s*Duration: ([^,]+), .+";
        responses.put(key2, "$1");
        // ffmpeg.exe -i mix-gameOfThrone.mp3 -r 1 -t 164 teste.mp3
        Map<String, ObservableList<String>> executeInConsoleAsync = ConsoleUtils
                .executeInConsoleAsync(cmd.toString(), responses);
        
        return ConsoleUtils.defineProgress(key2, key, executeInConsoleAsync, SongUtils::convertTimeToMillis);
    }


    public static Image extractEmbeddedImage(File mp3) {
        MP3File mp31 = new MP3File(mp3);
        try {
            for (ID3Tag tag : mp31.getTags()) {

                if (tag instanceof ID3V2_3_0Tag) {
                    ID3V2_3_0Tag tag2 = (ID3V2_3_0Tag) tag;

                    if (tag2.getAPICFrames() != null && tag2.getAPICFrames().length > 0) {
                        // Simply take the first image that is available.
                        APICID3V2Frame frame = tag2.getAPICFrames()[0];
                        return new Image(new ByteArrayInputStream(frame.getPictureData()));
                    }
                }
            }
            ID3V2Tag id3v2Tag = mp31.getID3V2Tag();
            ID3V2Frame[] singleFrames = id3v2Tag.getSingleFrames();
            String singleFramesStr = Arrays.toString(singleFrames);
            LOG.trace("SingleFrames={}", singleFramesStr);
        } catch (Exception e) {
            LOG.trace("", e);
        }
        return null;
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

    public static DoubleProperty splitAudio(File mp3File, File mp4File, Duration start, Duration end) {
        if (mp4File.exists()) {
            try {
                Files.delete(mp4File.toPath());
            } catch (IOException e) {
                LOG.error("ERRO AO DELETAR", e);
            }
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append(FFMPEG);
        cmd.append(" -i \"");
        cmd.append(mp3File);
        cmd.append("\" -ss ");
        cmd.append(formatFullDuration(start));
        cmd.append(" -r 1 -to ");
        cmd.append(formatFullDuration(end));
        cmd.append(" \"");

        cmd.append(mp4File);
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
