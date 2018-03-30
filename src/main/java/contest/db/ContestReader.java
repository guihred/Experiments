package contest.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
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

    private static final int STATE_IGNORE = 0;
    private static final int STATE_OPTION = 4;
    private static final int STATE_QUESTION = 3;
    private static final int STATE_TEXT = 2;

    private static final String SUBJECT_PATTERN = "Questões de \\d+ a \\d+\\s*";
    private static final String TEXT_PATTERN = "Texto \\d+";
    private static final String TEXTS_PATTERN = "Textos .+ para responder às questões de (\\d+) a (\\d+)\\.\\s*";

    public static ObservableList<ContestQuestion> getContestQuestions(File file) {
        ContestReader contestReader = new ContestReader();
        new Thread(() -> contestReader.readFile(file)).start();
        return contestReader.listaMedicamentos;
    }

    private ContestQuestionAnswer answer = new ContestQuestionAnswer();
    private Contest contest;
    private ContestQuestion contestQuestion = new ContestQuestion();
    private ObservableList<ContestQuestion> listaMedicamentos = FXCollections.observableArrayList();
    private int option = 0;

    private Map<String, Map.Entry<Float, Float>> questionPosition = new HashMap<>();

    private int state = 0;
    private String subject;


    private ContestText text = new ContestText();
    private List<ContestText> texts = new ArrayList<>();

    private void addQuestion() {
        answer = new ContestQuestionAnswer();
        answer.setExercise(contestQuestion);
        listaMedicamentos.add(contestQuestion);
        contestQuestion = new ContestQuestion();
        contestQuestion.setContest(contest);
        contestQuestion.setSubject(subject);
        option = 0;
    }

    public void copyStream(Object numb, InputStream obj) throws IOException {
        InputStream createInputStream = obj;
        IOUtils.copy(createInputStream, new FileOutputStream(new File("teste" + numb)));
    }

    Integer intValue(String v) {
        try {
            return Integer.valueOf(v.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            return null;
        }

    }


    private void log(String s) {
        // System.out.println(s);
    }

    private COSDocument parseAndGet(RandomAccessFile source) throws IOException {
        PDFParser parser = new PDFParser(source);
        parser.parse();
        return parser.getDocument();
    }
    private void processQuestion(String[] linhas, int i) {
        String s = linhas[i];

        if (s.matches(SUBJECT_PATTERN) && i > 0) {
            subject = linhas[i - 1];
            return;
        }

        if (s.matches(TEXTS_PATTERN)) {
            state = STATE_TEXT;
            String[] split = s.replaceAll(TEXTS_PATTERN, "$1,$2").split(",");
            IntSummaryStatistics stats = Stream.of(split).mapToInt(this::intValue).summaryStatistics();
            text.setMin(stats.getMin());
            text.setMax(stats.getMax());

            return;
        }
        if (s.matches(LINE_PATTERN)) {
            return;
        }

        if (s.matches(QUESTION_PATTERN)) {
            if (option == 5 && state == STATE_OPTION) {
				addQuestion();
			}
            if (state == STATE_TEXT) {
                addNewText();
            }
            log(s);
            contestQuestion.setNumber(intValue(s));
            state = STATE_QUESTION;
            return;
        }
        if (s.matches(OPTION_PATTERN)) {
            if (state == STATE_OPTION) {
                contestQuestion.addOption(answer);

                answer = new ContestQuestionAnswer();
                answer.setExercise(contestQuestion);
            }

            answer.setNumber(option);
            option++;
            state = STATE_OPTION;

        }
        if (StringUtils.isBlank(s) && state == STATE_TEXT && !listaMedicamentos.isEmpty()) {
            addNewText();
            state = STATE_IGNORE;
        }
        if (state == STATE_OPTION && StringUtils.isBlank(s)) {
            contestQuestion.addOption(answer);
            if (option == 5) {
                addQuestion();
            }

            state = STATE_IGNORE;
        }

        switch (state) {
            case STATE_IGNORE:
                contestQuestion.setContest(contest);
                contestQuestion.setSubject(subject);
                break;

            case STATE_TEXT:
                if (StringUtils.isNotBlank(s)) {
                    text.appendText(s + "\n");
                }
                break;
            case STATE_QUESTION:
                if (StringUtils.isNotBlank(s)) {
                    contestQuestion.appendExercise(s + "\n");
                }
                break;
            case STATE_OPTION:
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

        if (StringUtils.isNotBlank(s) && state != STATE_IGNORE) {
            log(s);
        }
    }

    private void addNewText() {
        text.setContest(contest);
        texts.add(text);
        text = new ContestText(contest);
    }
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
                        questionPosition.put(text, new AbstractMap.SimpleEntry<>(x, y));
                    }
                }

            };
            int numberOfPages = pdDoc.getNumberOfPages();
            contest = new Contest(Organization.IADES);
            PrintImageLocations printImageLocations = new PrintImageLocations();
            for (int i = 2; i < numberOfPages; i++) {
                PDPage page = pdDoc.getPage(i - 1);
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
            text = new ContestText(contest);
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