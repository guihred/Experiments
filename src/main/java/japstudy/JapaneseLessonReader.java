package japstudy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import pdfreader.GoogleVoiceApi;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;

public final class JapaneseLessonReader {
    private static final String JAP_REGEX = ".*([\u2E80-\u6FFF]+.*)$";
    private static final Logger LOGGER = HasLogging.log();
    private static final LessonDAO LESSON_DAO = new LessonDAO();

    private JapaneseLessonReader() {
    }

    public static Long getCountExerciseByLesson(Integer lesson) {
        return LESSON_DAO.getCountExerciseByLesson(lesson);
    }

    public static ObservableList<JapaneseLesson> getLessons(String arquivo) {
        ObservableList<JapaneseLesson> listaExercises = FXCollections.observableArrayList();
        try (InputStream resourceAsStream = new FileInputStream(ResourceFXUtils.toFile(arquivo));
                XWPFDocument document1 = new XWPFDocument(resourceAsStream)) {
            addJapaneseLessons(listaExercises, document1);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return listaExercises;
    }

    public static ObservableList<JapaneseLesson> getLessonsWait() {
        ObservableList<JapaneseLesson> lessons = FXCollections.observableArrayList();
        lessons.addAll(LESSON_DAO.list());
        return lessons;
    }

    public static LocalTime getMaxTimeLesson(Integer lesson, Integer exercise) {
        return LESSON_DAO.getMaxTimeLesson(lesson, exercise);
    }

    public static void main(String[] args) {
        extractJapanese();
    }

    public static void update(JapaneseLesson japaneseLesson) {
        LESSON_DAO.saveOrUpdate(japaneseLesson);
    }

    private static void addJapanese(JapaneseLesson japaneseLesson, String text) {
        if (japaneseLesson != null) {
            japaneseLesson.addJapanese(text.trim());
        }
    }

    private static void addJapaneseLessons(ObservableList<JapaneseLesson> listaExercises, XWPFDocument document1) {
        JapaneseLesson japaneseLesson = null;
        List<IBodyElement> bodyElements = document1.getBodyElements();
        int lesson = 1;
        for (IBodyElement e : bodyElements) {
            if (e.getElementType() == BodyElementType.PARAGRAPH) {
                XWPFParagraph a = (XWPFParagraph) e;
                String text = a.getText();
                if (text.isEmpty()) {
                    continue;
                }
                if (text.matches("\\d+\\..+")) {
                    Integer exerciseNumber = Integer.valueOf(text.replaceAll("^(\\d+)\\..+", "$1"));
                    lesson = saveIfPossible(listaExercises, japaneseLesson, lesson, exerciseNumber);
                    japaneseLesson = new JapaneseLesson();
                    japaneseLesson.setLesson(lesson);
                    japaneseLesson.setExercise(exerciseNumber);
                    japaneseLesson.addEnglish(text.replaceAll("^\\d+\\.", "").trim());
                    if (text.matches(JAP_REGEX)) {
                        japaneseLesson.addJapanese(text.replaceAll(JAP_REGEX, "$1"));
                    }
                } else if (text.matches(JAP_REGEX)) {
                    addJapanese(japaneseLesson, text);
                } else if (japaneseLesson != null) {
                    japaneseLesson.addRomaji(text.trim());
                }
            }
        }
    }

    private static void extractJapanese() {
        try (BufferedWriter newBufferedWriter =
                Files.newBufferedWriter(ResourceFXUtils.getOutFile("mp3/japanese.txt").toPath())) {
            List<JapaneseLesson> list = LESSON_DAO.list();
            for (JapaneseLesson japaneseLesson : list) {
                RunnableEx.run(() -> {
                    String japanese = japaneseLesson.getJapanese();
                    File synthesize = GoogleVoiceApi.synthesize(japanese, "ja-JP");
                    newBufferedWriter.append(String.format("%s[source:%s]\t%s\t%s\t%n", japanese, synthesize.getName(),
                            japaneseLesson.getEnglish(), japaneseLesson.getRomaji()));
                });
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static int saveIfPossible(ObservableList<JapaneseLesson> listaExercises, JapaneseLesson japaneseLesson,
            int lesson, Integer exerciseNumber) {
        if (japaneseLesson != null) {
            LESSON_DAO.saveOrUpdate(japaneseLesson);
            listaExercises.add(japaneseLesson);
            if (japaneseLesson.getExercise() > exerciseNumber) {
                return lesson + 1;
            }
        }
        return lesson;
    }

}