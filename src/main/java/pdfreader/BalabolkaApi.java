package pdfreader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import utils.CommonsFX;
import utils.ConsoleUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.ex.ConsumerEx;

public class BalabolkaApi {
    private static final String BALABOLKA_EXE = FileTreeWalker
            .getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "bal4web.exe").toFile().toString();

    public static void main(String[] args) {
        speak("It Worked");
    }

    public static File speak(String s) {
        return speak(s, out -> {
            CommonsFX.initializeFX();
            Media sound = new Media(out.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        });
    }

    public static File speak(String s, ConsumerEx<File> run) {
        String text = s.replaceAll("<.+>", "");
        /*
         * cat readme.eng.txt | ./bal4web.exe -i -g f -l en-US -s Google -w hi.wav &&
         * cat hi.wav > /dev/dsp
         */
        File outFile = ResourceFXUtils.getOutFile("wav/" + text.hashCode() + ".wav");
        if (!outFile.exists()) {
            ConsoleUtils.executeInConsoleInfo(
                    String.format("\"%s\" -i -g f -l en-US -iab -w \"%s\"", BALABOLKA_EXE, outFile),
                    new ByteArrayInputStream((text + "\r\n").getBytes(StandardCharsets.UTF_8)));
        }
        if (outFile.exists()) {
            ConsumerEx.accept(run, outFile);
        }
        return outFile;

    }

    public static File toAudio(String s) {
        return speak(s, out -> {
            // DOES NOTHING
        });
    }
}
