package japstudy;

import japstudy.db.HibernateUtil;
import japstudy.db.JapaneseLesson;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import simplebuilder.HasLogging;
import simplebuilder.ResourceFXUtils;

public enum JapaneseAudio {
    AUDIO_1(1, "jaf01lesson122216.mp3"),
    AUDIO_2(2, "jaf02lesson122216.mp3"),
    AUDIO_3(3, "jaf03lesson122216"),
    AUDIO_4(4, "jaf04lesson122216"),
    AUDIO_5(5, "jaf05lesson032814"),
    AUDIO_6(6, "jaf06lesson122216"),
    AUDIO_7(7, "jaf07lesson122216"),
    AUDIO_8(8, "jaf08lesson122216"),
    AUDIO_9(9, "jaf09lesson122216"),
    AUDIO_10(10, "jaf10lesson122216"),
    AUDIO_11(11, "jaf11lesson122216"),
    AUDIO_12(12, "jaf12lesson122216"),
    AUDIO_13(13, "jaf13lesson122216"),
    AUDIO_14(14, "jaf14lesson122216"),
    AUDIO_15(15, "jaf15lesson122216"),
    AUDIO_16(16, "jaf16lesson122216"),
    AUDIO_17(17, "jaf17lesson122216");
    private static final String OUTPUT_FILE = "C:\\Users\\guilherme.hmedeiros\\Documents\\Dev\\mobileApps\\AndroidTest\\app\\src\\main\\assets\\create_database.sql";
    private final String file;
    private final int lesson;

    JapaneseAudio(int lesson, String file) {
        this.lesson = lesson;
        this.file = file;

    }

    public String getFile() {
        return file;
    }

    public static JapaneseAudio getAudio(int lesson) {
        return Stream.of(values()).filter(e -> e.lesson == lesson).findFirst().orElse(null);
    }

    public URL getURL() {
        return ResourceFXUtils.toURL("jap/" + file);
    }

    public int getLesson() {
        return lesson;
    }

    public static void main(String[] args) {
        ObservableList<JapaneseLesson> lessons = JapaneseLessonReader.getLessonsWait();
        /*
            CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT 'en_US') 
            INSERT INTO "android_metadata" VALUES ('en_US')
            
            CREATE TABLE "JAPANESE_LESSON" (
                    english TEXT,
                    japanese TEXT,
                    romaji TEXT,
                    exercise INT,
                    lesson INT,
                    PRIMARY KEY (exercise,lesson)) 
         */
        File file2 = new File(OUTPUT_FILE);

        try (PrintStream out = new PrintStream(file2, StandardCharsets.UTF_8.displayName())) {

            for (JapaneseLesson lesson : lessons) {

                String format = String.format(
                        "INSERT INTO JAPANESE_LESSON(english,japanese,romaji,exercise,lesson) VALUES('%s','%s','%s',%d,%d);",
                        extracted(lesson.getEnglish()), extracted(lesson.getJapanese()), extracted(lesson.getRomaji()),
                        lesson.getPk().getExercise(), lesson.getPk().getLesson());
                out.println(format);
            }
        } catch (Exception e) {
            HasLogging.log().error("", e);
        }
        HibernateUtil.shutdown();
    }

    private static String extracted(String string) {
        return Objects.toString(string, "").replaceAll("'", "''").replaceAll(";", ",");
    }

}
