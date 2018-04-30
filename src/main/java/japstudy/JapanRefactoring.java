package japstudy;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class JapanRefactoring {
    private static final String LESSON_REGEX = "INSERT INTO JAPANESE_LESSON\\(english,japanese,romaji,exercise,lesson\\) VALUES\\('([^\n]+)','([^\n]+)','([^\n]+)',(\\d+),(\\d+)\\);";

    private static final String TXT_FILE = "C:\\Users\\guilherme.hmedeiros\\Documents\\Dev\\mobileApps\\AndroidTest\\app\\src\\main\\assets\\create_database2.sql";

    private static int chapter = 1;
    private static int oldChapter = 0;
    private static int lesson = 0;


    public static void main(String[] args) throws IOException {
        PrintStream print = new PrintStream(TXT_FILE.substring(0, TXT_FILE.length() - 4) + "2.sql");

        print.println("DELETE FROM JAPANESE_LESSON;");
        Files.lines(new File(TXT_FILE).toPath(), StandardCharsets.UTF_8).forEach(t -> {
            if (!t.matches(LESSON_REGEX)) {
                return;
            }
            String[] split = t.replaceAll(LESSON_REGEX, "$1@$2@$3@$4@$5").split("@");
            int old = Integer.parseInt(split[4]);
            if (lesson >= 50) {
                chapter++;
                oldChapter = old;
                lesson = 0;
            }

            print.println(String.format(
                    "INSERT INTO JAPANESE_LESSON(english,japanese,romaji,exercise,lesson) VALUES('%s','%s','%s',%d,%d);",
                    split[0], split[1], split[2], ++lesson, chapter));

        });
        print.close();
    }

}
