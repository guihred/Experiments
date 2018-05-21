package contest.db;
import static contest.db.ContestReader.ReaderState.STATE_IGNORE;
import static contest.db.ContestReader.ReaderState.STATE_OPTION;
import static contest.db.ContestReader.ReaderState.STATE_QUESTION;
import static contest.db.ContestReader.ReaderState.STATE_TEXT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
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
    private static final String TEXT_PATTERN = "Texto \\d+\\s*";
    private static final String OPTION_PATTERN = "\\([A-E]\\).+";
    public static final String QUESTION_PATTERN = "QUESTÃO +(\\d+)\\s*___+\\s+";


    enum ReaderState {
        STATE_IGNORE,
        STATE_OPTION,
        STATE_QUESTION,
        STATE_TEXT;
    }

    private static final String SUBJECT_PATTERN = "Questões de \\d+ a \\d+\\s*";
    public static final String TEXTS_PATTERN = "Textos .+ para responder às questões de (\\d+) a (\\d+)\\.\\s*";

	public static ContestReader INSTANCE;

    public static ObservableList<ContestQuestion> getContestQuestions(File file, Runnable... r) {
        if (INSTANCE == null) {
            INSTANCE = new ContestReader();
        }
        new Thread(() -> {
            INSTANCE.readFile(file);
            Stream.of(r).forEach(Runnable::run);
        }).start();
        return INSTANCE.listQuestions;
    }

    public static ObservableList<ContestText> getContestTexts(File file) {
        if (INSTANCE == null) {
            INSTANCE = new ContestReader();
        }
        if (INSTANCE.texts.isEmpty()) {
            // new Thread(() -> INSTANCE.readFile(file)).start();
        }

        return INSTANCE.texts;
    }
    private ContestQuestionAnswer answer = new ContestQuestionAnswer();
	Contest contest;
    private ContestQuestion contestQuestion = new ContestQuestion();
	private ObservableList<ContestQuestion> listQuestions = FXCollections.observableArrayList();

    private int option = 0;

    // private Map<String, Map.Entry<Float, Float>> questionPosition = new
    // HashMap<>();
    private List<QuestionPosition> questionPosition = new ArrayList<>();
    private ReaderState state = STATE_IGNORE;

    private String subject;
    private ContestText text = new ContestText();

	final ObservableList<ContestText> texts = FXCollections.observableArrayList();

    int pageNumber;

    private void addNewText() {
        text.setContest(contest);
        texts.add(text);
        text = new ContestText(contest);
    }

    private void addQuestion() {
        answer = new ContestQuestionAnswer();
        answer.setExercise(contestQuestion);
        listQuestions.add(contestQuestion);
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
        if (StringUtils.isBlank(s) && state == STATE_TEXT && !listQuestions.isEmpty()) {
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
                    if (s.matches(TEXT_PATTERN)) {
                        addNewText();
                    }
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

    private void readFile(File file) {
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc);) {
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text, textPositions);
                    if (text != null && text.matches(QUESTION_PATTERN + "|" + TEXTS_PATTERN)
                            && !textPositions.isEmpty()) {
                        TextPosition textPosition = textPositions.get(0);
                        float x = textPosition.getXDirAdj();
                        float y = textPosition.getYDirAdj();
                        QuestionPosition qp = new QuestionPosition();
                        qp.line = text;
                        qp.x = x;
                        qp.y = y;
                        qp.page = pageNumber;

                        System.out.println(qp.line + " at (" + qp.x + "," + qp.y + ") page " + pageNumber);
                        questionPosition.add(qp);

                    }
                }

            };
            int numberOfPages = pdDoc.getNumberOfPages();
            contest = new Contest(Organization.IADES);
            PrintImageLocations printImageLocations = new PrintImageLocations();
            for (int i = 2; i < numberOfPages; i++) {
                PDPage page = pdDoc.getPage(i - 1);
                pageNumber = i;
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                List<PDFImage> images = printImageLocations.processPage(page, i);
                String parsedText = pdfStripper.getText(pdDoc);
                String[] lines = parsedText.split("\r\n");
                tryReadQuestionFromLines(lines);
                List<HasImage> collect = Stream.concat(texts.stream(), listQuestions.stream())
                        .collect(Collectors.toList());
                final int j = i;
                for (PDFImage pdfImage : images) {
                    questionPosition.stream().filter(e -> e.page == j)
                            .min(Comparator.comparing((QuestionPosition e) -> {
                                float a = pdfImage.x - e.x;
                                float b = pdfImage.y - e.y;
                                return a * a + b * b;
                            })).ifPresent(orElse -> {
                                for (HasImage pdfImage2 : collect) {
                                    if (pdfImage2.matches(orElse.line)) {
                                        pdfImage2.appendImage(pdfImage.file.getName());
                                    }
                                }
                            });
                }

                // concat.forEach(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContestQuestion tryReadQuestionFromLines(String[] lines) {

        try {
            state = STATE_IGNORE;
            option = 0;
            text = new ContestText(contest);
            answer.setExercise(contestQuestion);
            for (int i = 0; i < lines.length; i++) {
                processQuestion(lines, i);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return null;
    }

}