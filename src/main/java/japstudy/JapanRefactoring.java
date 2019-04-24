package japstudy;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class JapanRefactoring {
    private static final Logger LOG = HasLogging.log();

    private static final String LESSON_REGEX = "INSERT INTO JAPANESE_LESSON"
        + "\\(english,japanese,romaji,exercise,lesson\\) VALUES"
        + "\\('([^\n]+)','([^\n]+)','([^\n]+)',(\\d+),(\\d+)\\);";

    public static final String TXT_FILE = ResourceFXUtils.toFullPath("create_database.sql");

    private static int chapter = 1;
    private static int lesson;

    public static void main(String[] args) {
        refactorJapaneseFile(TXT_FILE, renameFile(TXT_FILE));
    }

    public static void refactorJapaneseFile(String inputfile, String outputFile) {
        try (PrintStream print = new PrintStream(outputFile, StandardCharsets.UTF_8.displayName());
            Stream<String> lines = Files.lines(new File(inputfile).toPath(), StandardCharsets.UTF_8)) {
            print.println("DELETE FROM JAPANESE_LESSON;");
            lines.forEach(t -> {
                if (!t.matches(LESSON_REGEX)) {
                    return;
                }
                String[] split = t.replaceAll(LESSON_REGEX, "$1@$2@$3@$4@$5").split("@");
                if (lesson >= 50) {
                    chapter++;
                    lesson = 0;
                }

                print.println(String.format(
                    "INSERT INTO JAPANESE_LESSON"
                        + "(english,japanese,romaji,exercise,lesson) VALUES('%s','%s','%s',%d,%d);",
                    split[0], split[1], split[2], ++lesson, chapter));

            });
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static String renameFile(String txtFile) {
        return txtFile.substring(0, txtFile.length() - 4) + "3.sql";
    }

}
