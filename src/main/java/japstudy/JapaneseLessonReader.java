package japstudy;

import japstudy.db.HibernateUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class JapaneseLessonReader implements HasLogging {
    private static final String JAP_REGEX = ".*([\u2E80-\u6FFF]+.*)$";
    private static final Logger LOGGER = HasLogging.log();
	private static LessonDAO lessonDAO = new LessonDAO();
	private JapaneseLessonReader() {
	}

	public static Long getCountExerciseByLesson(Integer lesson) {
		return lessonDAO.getCountExerciseByLesson(lesson);
	}

	public static ObservableList<JapaneseLesson> getLessons(String arquivo) throws IOException {
		InputStream resourceAsStream = new FileInputStream(ResourceFXUtils.toFile(arquivo));
		ObservableList<JapaneseLesson> listaExercises = FXCollections.observableArrayList();
		try (XWPFDocument document1 = new XWPFDocument(resourceAsStream)) {
			addJapaneseLessons(listaExercises, document1);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return listaExercises;
	}

	public static ObservableList<JapaneseLesson> getLessonsWait() {
		ObservableList<JapaneseLesson> lessons = FXCollections.observableArrayList();
		lessons.addAll(lessonDAO.list());
		return lessons;
	}

	public static LocalTime getMaxTimeLesson(Integer lesson, Integer exercise) {
		return lessonDAO.getMaxTimeLesson(lesson, exercise);
	}

	public static void main(String[] args) {
		try {
			ObservableList<JapaneseLesson> lessons = getLessons("jaftranscript.docx");
			lessons.forEach(JapaneseLessonReader::update);
			HibernateUtil.shutdown();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}
	public static void update(JapaneseLesson japaneseLesson) {
		lessonDAO.saveOrUpdate(japaneseLesson);
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

	private static int saveIfPossible(ObservableList<JapaneseLesson> listaExercises, JapaneseLesson japaneseLesson,
			int lesson, Integer exerciseNumber) {
		if (japaneseLesson != null) {
			lessonDAO.saveOrUpdate(japaneseLesson);
			listaExercises.add(japaneseLesson);
			if (japaneseLesson.getExercise() > exerciseNumber) {
				return lesson + 1;
			}
		}
		return lesson;
	}

}