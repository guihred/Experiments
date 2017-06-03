package japstudy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JapaneseLessonReader {
	private static final Logger LOGGER = LoggerFactory.getLogger(JapaneseLessonReader.class);

	private JapaneseLessonReader() {
	}

	public static void main(String[] args) {
		File file = new File("jaftranscript051517phone.pdf");
		System.out.println(file.exists());
		try {
			ObservableList<JapaneseLesson> lessons = getLessons("jaftranscript.docx");
			// ObservableList<JapaneseLesson> medicamentosSNGPCPDF;
			// medicamentosSNGPCPDF = getLessonsPDF(file);
			lessons.forEach(System.out::println);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	public static ObservableList<JapaneseLesson> getLessons(String arquivo) throws IOException {
		InputStream resourceAsStream = new FileInputStream(new File(arquivo));
		XWPFDocument document1 = new XWPFDocument(resourceAsStream);
		JapaneseLesson japaneseLesson = null;
		List<IBodyElement> bodyElements = document1.getBodyElements();
		ObservableList<JapaneseLesson> listaExercises = FXCollections.observableArrayList();
		int lesson = 1;
		for (IBodyElement e : bodyElements) {
			if (e.getElementType() == BodyElementType.PARAGRAPH) {
				XWPFParagraph a = (XWPFParagraph) e;
				String text = a.getText();
				if (text.isEmpty()) {
					continue;
				} else if (text.matches("\\d+\\..+")) {
					Integer exerciseNumber = Integer.valueOf(text.replaceAll("^(\\d+)\\..+", "$1"));
					if (japaneseLesson != null) {
						listaExercises.add(japaneseLesson);
						if (japaneseLesson.getExercise() > exerciseNumber) {
							lesson++;
						}
					}
					japaneseLesson = new JapaneseLesson();
					japaneseLesson.setLesson(lesson);
					japaneseLesson.setExercise(exerciseNumber);
					japaneseLesson.addEnglish(text.replaceAll("^\\d+\\.", "").trim());
					if (text.matches(".*[\u2E80-\u6FFF]+.*")) {
						japaneseLesson.addJapanese(text.replaceAll(".*([\u2E80-\u6FFF]+.*)$", "$1"));
					}
				} else if (text.matches(".*[\u2E80-\u6FFF]+.*")) {
					japaneseLesson.addJapanese(text.trim());
				} else if (japaneseLesson != null) {
					japaneseLesson.addRomaji(text.trim());

				}
			}
		}
		return listaExercises;
	}

}