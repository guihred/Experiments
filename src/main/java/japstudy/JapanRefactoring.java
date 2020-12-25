package japstudy;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.HibernateUtil;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;

public class JapanRefactoring {
    private static final Logger LOG = HasLogging.log();

    private static final String LESSON_REGEX = "INSERT INTO JAPANESE_LESSON"
        + "\\(english,japanese,romaji,exercise,lesson\\) VALUES"
        + "\\('([^\n]+)','([^\n]+)','([^\n]+)',(\\d+),(\\d+)\\);";

    private static final String OUTPUT_FILE = "sql/create_database.sql";
    public static final String TXT_FILE = ResourceFXUtils.toFullPath(OUTPUT_FILE);

    private static int chapter = 1;
    private static int lesson;


    public static void createDatabaseFile() {
        ObservableList<JapaneseLesson> lessons = JapaneseLessonReader.getLessonsWait();
        /*
         * CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT 'en_US') INSERT INTO
         * "android_metadata" VALUES ('en_US')
         * 
         * CREATE TABLE "JAPANESE_LESSON" ( english TEXT, japanese TEXT, romaji TEXT,
         * exercise INT, lesson INT, PRIMARY KEY (exercise,lesson))
         */
        File file2 = ResourceFXUtils.getOutFile(OUTPUT_FILE);

        try (PrintStream out = new PrintStream(file2, StandardCharsets.UTF_8.displayName())) {

            for (JapaneseLesson lesson1 : lessons) {

                String format = String.format(
                    "INSERT INTO JAPANESE_LESSON(english,japanese,romaji,exercise,lesson) "
                        + "VALUES('%s','%s','%s',%d,%d);",
                    treatStr(lesson1.getEnglish()), treatStr(lesson1.getJapanese()), treatStr(lesson1.getRomaji()),
                    lesson1.getExercise(), lesson1.getLesson());
                out.println(format);
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
        HibernateUtil.shutdown();
    }

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
                String[] lessonParts = t.replaceAll(LESSON_REGEX, "$1@$2@$3@$4@$5").split("@");
                if (lesson >= 50) {
                    chapter++;
                    lesson = 0;
                }

                print.println(String.format(
                    "INSERT INTO JAPANESE_LESSON"
                        + "(english,japanese,romaji,exercise,lesson) VALUES('%s','%s','%s',%d,%d);",
                        lessonParts[0], lessonParts[1], lessonParts[2], ++lesson, chapter));

            });
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public static String renameFile(String txtFile) {
        return txtFile.substring(0, txtFile.length() - 4) + "3.sql";
    }

    private static String treatStr(String string) {
        return Objects.toString(string, "").replaceAll("'", "''").replaceAll(";", ",");
    }

}
