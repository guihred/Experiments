package pdfreader;

import extract.web.HashVerifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import utils.*;
import utils.ex.ConsumerEx;

public final class BalabolkaApi {
    private static final String BALABOLKA_EXE = FileTreeWalker
            .getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "bal4web.exe").toFile().toString();

    private BalabolkaApi() {
    }

    public static void main(String[] args) throws IOException {
        // You should take the
        // subway.[sound:sapi5js-6e93fdd5-e02ac668-a24726b9-2fb6956d-720b8c84.mp3] Você
        // deveria pegar o metrô.
        CommonsFX.initializeFX();
        try (BufferedReader newBufferedReader = Files.newBufferedReader(
                new File("C:\\Users\\guigu\\Downloads\\Padrão.txt").toPath(), StandardCharsets.UTF_8)) {
            List<String> lines = newBufferedReader.lines().map(s -> {
                String[] split = s.split("\t");
                File audio = toAudio(split[0]);
                return Stream.of(split[0] + "[sound:" + audio.getName() + "]", split[1], "")
                        .collect(Collectors.joining("\t"));
            }).collect(Collectors.toList());
            Files.write(ResourceFXUtils.getOutFile("wav/Baralho.txt").toPath(), lines);
        }

    }

    public static File speak(String s) {
        return speak(s, out -> {
            CommonsFX.initializeFX();
            Media sound = new Media(out.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        });
    }

    public static File toAudio(String s) {
        return speak(s, out -> {
            // DOES NOTHING
        });
    }

    private static File speak(String s, ConsumerEx<File> run) {
        String text = s.replaceAll("<.+>", "");
        /*
         * cat readme.eng.txt | ./bal4web.exe -i -g f -l en-US -s Google -w hi.wav &&
         * cat hi.wav > /dev/dsp
         */
        String md5Hash = HashVerifier.getMD5Hash(s);
        CommonsFX.runInPlatformSync(() -> ImageFXUtils.setClipboardContent(text));
        File outFile = ResourceFXUtils.getOutFile("wav/" + md5Hash + ".wav");
        if (!outFile.exists()) {
            ConsoleUtils
                    .executeInConsoleInfo(String.format("%s -c -g f -l en-US -s baidu -w %s", BALABOLKA_EXE, outFile));
        }
        if (outFile.exists()) {
            ConsumerEx.accept(run, outFile);
        }
        return outFile;
    }
}
