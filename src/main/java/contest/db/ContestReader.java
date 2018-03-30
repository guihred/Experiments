package contest.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ContestReader {
    private static final String LINE_PATTERN = "^\\d+\\s+$";
    private static final Logger LOGGER = LoggerFactory.getLogger(ContestReader.class);
    private static final String OPTION_PATTERN = "\\([A-E]\\).+";
    private static final String QUESTION_PATTERN = "QUESTÃO .+___+\\s+";
    private static final String SUBJECT_PATTERN = "Questões de \\d+ a \\d+\\s*";
    private static final String TEXTS_PATTERN = "Textos ";
    private static final String TEXT_PATTERN = "Texto \\d+";

    public static ObservableList<ContestQuestion> getContestQuestions(File file) {
        ContestReader contestReader = new ContestReader();
        new Thread(() -> contestReader.readFile(file)).start();
        return contestReader.listaMedicamentos;

    }
    ContestQuestionAnswer answer = new ContestQuestionAnswer();
    private Contest contest;
    ContestQuestion contestQuestion = new ContestQuestion();
    int state = 0;
    ObservableList<ContestQuestion> listaMedicamentos = FXCollections.observableArrayList();
    int option = 0;
    String subject;
    StringBuilder text = new StringBuilder();

    public void copyStream(Object numb, InputStream obj) throws IOException {
        InputStream createInputStream = obj;
        IOUtils.copy(createInputStream, new FileOutputStream(new File("teste" + numb)));
    }


    private void processQuestion(String[] linhas, int i) {
        String s = linhas[i];
        if (s.contains("P R O V A  O B J E T I V A")) {
            state = 1;
            return;
        }
        if (s.matches(SUBJECT_PATTERN)) {
            subject = linhas[i - 1];
            return;
        }

        if (s.startsWith(TEXTS_PATTERN)) {
            state = 2;
            return;
        }
        if (s.matches(LINE_PATTERN)) {
            return;
        }
        if (s.matches(QUESTION_PATTERN)) {
			if (option == 5 && state == 4) {
				addQuestion();
			}
            log(s);
            contestQuestion.setNumber(intValue(s));
            state = 3;
            return;
        }
        if (s.matches(OPTION_PATTERN)) {
            if (state == 4) {
                contestQuestion.addOption(answer);

                answer = new ContestQuestionAnswer();
                answer.setExercise(contestQuestion);
            }

            answer.setNumber(option);
            option++;
            state = 4;

        }
        if (StringUtils.isBlank(s) && state == 2 && !listaMedicamentos.isEmpty()) {
            state = 0;
        }
        if (state == 4 && StringUtils.isBlank(s)) {
            contestQuestion.addOption(answer);
            if (option == 5) {
                addQuestion();
            }

            state = 0;
        }

        switch (state) {
            case 0:
                contestQuestion.setContest(contest);
                contestQuestion.setSubject(subject);
                break;
            case 1:
                if (StringUtils.isNotBlank(s)) {
                    subject = s;
                    contestQuestion.setSubject(subject);
                    state = 0;
                }
                break;
            case 2:
                if (StringUtils.isNotBlank(s)) {
                    text.append(s + "\n");
                }
                break;
            case 3:
                if (StringUtils.isNotBlank(s)) {
                    contestQuestion.appendExercise(s + "\n");
                }
                break;
            case 4:
                if (StringUtils.isNotBlank(s)) {
                    answer.appendAnswer(s.trim() + " ");
                    if (option == 5 && i == linhas.length - 1) {
                        addQuestion();
                    }

                }
                break;
            default:
                break;
        }

        if (StringUtils.isNotBlank(s) && state != 0) {
            log(s);
        }
    }


	private void addQuestion() {
		answer = new ContestQuestionAnswer();
		answer.setExercise(contestQuestion);
		listaMedicamentos.add(contestQuestion);
		contestQuestion = new ContestQuestion();
		contestQuestion.setContest(contest);
		contestQuestion.setSubject(subject);
		option = 0;
	}

    Integer intValue(String v) {
        try {
            return Integer.valueOf(v.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return null;
        }

    }

    public boolean isPDF(File selectedFile) {
        return selectedFile.getName().endsWith(".pdf");
    }

    private void log(String s) {
        // System.out.println(s);
    }

    private COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }

    Map<String, Map.Entry<Float, Float>> questionPosition = new HashMap<>();
    private void readFile(File file) {
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc);) {
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text, textPositions);
                    if (text != null && text.matches(QUESTION_PATTERN + "|" + TEXT_PATTERN)
                            && !textPositions.isEmpty()) {
                        float x = textPositions.get(0).getX();
                        float y = textPositions.get(0).getY();
                        questionPosition.put(text.replaceAll("_", ""), new AbstractMap.SimpleEntry<>(x, y));
                    }
                }

            };
            int numberOfPages = pdDoc.getNumberOfPages();
            contest = new Contest(Organization.IADES);
            PrintImageLocations printImageLocations = new PrintImageLocations();
            for (int i = 2; i < numberOfPages; i++) {
                PDPage page = pdDoc.getPage(i);
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                printImageLocations.processPage(page);
                String parsedText = pdfStripper.getText(pdDoc);
                String[] linhas = parsedText.split("\r\n");
                tryReadSNGPCLine(linhas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContestQuestion tryReadSNGPCLine(String[] lines) {

        try {
            state = 0;
            option = 0;
            text = new StringBuilder();
            answer.setExercise(contestQuestion);
            for (int i = 0; i < lines.length; i++) {
                processQuestion(lines, i);

            }

        } catch (Exception e) {
            log("ERRO LINHA =");
            LOGGER.error("", e);
        }
        return null;
    }

}