package contest.db;

import contest.db.PrintImageLocations.PDFImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import utils.HasLogging;

public final class ContestReader implements HasLogging {
    public static final String QUESTION_PATTERN = "QUESTÃO +(\\d+)\\s*___+\\s+";
    public static final String TEXT_PATTERN = "Texto \\d+\\s*";
    public static final String TEXTS_PATTERN = "Textos .+ para responder às questões de (\\d+) a (\\d+)\\.\\s*";
    private static ContestReader instance;

    private static final String LINE_PATTERN = "^\\d+\\s+$";

    private static final String OPTION_PATTERN = "\\([A-E]\\).+";
    private static final String SUBJECT_PATTERN = "Questões de \\d+ a \\d+\\s*";

    private ContestQuestionAnswer answer = new ContestQuestionAnswer();

    private Contest contest;

    private ContestQuestion contestQuestion = new ContestQuestion();

    private ObservableList<ContestQuestion> listQuestions = FXCollections.observableArrayList();

    private int option;
    private int pageNumber;
    private List<QuestionPosition> questionPosition = new ArrayList<>();
    private ReaderState state = ReaderState.STATE_IGNORE;

    private String subject;

    private ContestText text = new ContestText();
    private final ObservableList<ContestText> texts = FXCollections.observableArrayList();
    private ContestQuestionDAO contestQuestionDAO = new ContestQuestionDAO();

    public Contest getContest() {
        return contest;
    }
    public ObservableList<ContestText> getTexts() {
        return texts;
    }

    private void addNewText() {
        text.setContest(contest);
        getTexts().add(text);
        text = new ContestText(contest);
    }

    public static void saveAll() {
        instance.saveAllEntities();
    }

    public void saveAllEntities() {
        contestQuestionDAO.saveOrUpdate(getContest());
        contestQuestionDAO.saveOrUpdate(listQuestions);
        contestQuestionDAO.saveOrUpdate(
                listQuestions.stream().flatMap(e -> e.getOptions().stream()).collect(Collectors.toList()));
        List<ContestText> nonNullTexts = getTexts().stream().filter(e -> StringUtils.isNotBlank(e.getText()))
                .collect(Collectors.toList());
        getLogger().info("Text max size {}", nonNullTexts.stream().map(ContestText::getText).filter(Objects::nonNull)
                .mapToInt(String::length).max().orElse(0));
        contestQuestionDAO.saveOrUpdate(nonNullTexts);
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

    private void executeAppending(String[] linhas, int i, String s) {
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
                addAnswer(linhas, i, s);
                break;
            default:
                break;
        }
    }

    private void addAnswer(String[] linhas, int i, String s) {
        if (StringUtils.isNotBlank(s)) {
            answer.appendAnswer(s.trim() + " ");
            if (option == 5 && i == linhas.length - 1) {
                addQuestion();
            }
        }
    }

    private static boolean matchesQuestionPattern(String text1, List<TextPosition> textPositions) {
        return text1 != null && text1.matches(QUESTION_PATTERN + "|" + TEXTS_PATTERN) && !textPositions.isEmpty();
    }


    private static COSDocument parseAndGet(RandomAccessFile source) throws IOException {
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
            state = ReaderState.STATE_TEXT;
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
            if (option == 5 && state == ReaderState.STATE_OPTION) {
                addQuestion();
            }
            if (state == ReaderState.STATE_TEXT) {
                addNewText();
            }
            getLogger().trace(s);
            contestQuestion.setNumber(intValue(s));
            state = ReaderState.STATE_QUESTION;
            return;
        }
        if (s.matches(OPTION_PATTERN)) {
            if (state == ReaderState.STATE_OPTION) {
                contestQuestion.addOption(answer);

                answer = new ContestQuestionAnswer();
                answer.setExercise(contestQuestion);
            }

            answer.setNumber(option);
            option++;
            state = ReaderState.STATE_OPTION;

        }
        if (StringUtils.isBlank(s) && state == ReaderState.STATE_TEXT && !listQuestions.isEmpty()) {
            addNewText();
            state = ReaderState.STATE_IGNORE;
        }
        if (state == ReaderState.STATE_OPTION && StringUtils.isBlank(s)) {
            contestQuestion.addOption(answer);
            if (option == 5) {
                addQuestion();
            }

            state = ReaderState.STATE_IGNORE;
        }

        executeAppending(linhas, i, s);

        if (StringUtils.isNotBlank(s) && state != ReaderState.STATE_IGNORE) {
            getLogger().trace(s);
        }
    }

    private void readFile(File file) {
        try (RandomAccessFile source = new RandomAccessFile(file, "r");
                COSDocument cosDoc = parseAndGet(source);
                PDDocument pdDoc = new PDDocument(cosDoc)) {
            PDFTextStripper pdfStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text1, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text1, textPositions);
                    if (matchesQuestionPattern(text1, textPositions)) {
                        TextPosition textPosition = textPositions.get(0);
                        float x = textPosition.getXDirAdj();
                        float y = textPosition.getYDirAdj();
                        QuestionPosition qp = new QuestionPosition();
                        qp.setLine(text1);
                        qp.setX(x);
                        qp.setY(y);
                        qp.setPage(pageNumber);
                        getLogger().trace("{} at ({},{}) page {}", qp.getLine(), qp.getX(), qp.getY(), pageNumber);
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
                List<HasImage> imageElements = Stream.concat(getTexts().stream(), listQuestions.stream())
                        .collect(Collectors.toList());
                final int j = i;
                for (PDFImage pdfImage : images) {
                    questionPosition.stream().filter(e -> e.getPage() == j)
                            .min(Comparator.comparing((QuestionPosition e) -> {
                                float a = pdfImage.x - e.getX();
                                float b = pdfImage.y - e.getY();
                                return a * a + b * b;
                            })).ifPresent(orElse -> {
                                for (HasImage pdfImage2 : imageElements) {
                                    if (pdfImage2.matches(orElse.getLine())) {
                                        pdfImage2.appendImage(pdfImage.file.getName());
                                    }
                                }
                            });
                }

                // concat.forEach(action)
            }
        } catch (Exception e) {
            getLogger().error("", e);
        }
    }

    private ContestQuestion tryReadQuestionFromLines(String[] lines) {

        try {
            state = ReaderState.STATE_IGNORE;
            option = 0;
            text = new ContestText(contest);
            answer.setExercise(contestQuestion);
            for (int i = 0; i < lines.length; i++) {
                processQuestion(lines, i);
            }
        } catch (Exception e) {
            getLogger().error("", e);
        }
        return null;
    }

    private Integer intValue(String v) {
        try {
            return Integer.valueOf(v.replaceAll("\\D", ""));
        } catch (NumberFormatException e) {
            getLogger().trace("", e);
            return null;
        }

    }

    public static ObservableList<ContestQuestion> getContestQuestions(File file, Runnable... r) {
        if (instance == null) {
            instance = new ContestReader();
        }
        new Thread(() -> {
            instance.readFile(file);
            Stream.of(r).forEach(Runnable::run);
        }).start();
        return instance.listQuestions;
    }

    public static ObservableList<ContestQuestion> getContestQuestions() {
        return FXCollections.observableArrayList(getInstance().contestQuestionDAO.list());
    }

    public static ObservableList<ContestText> getContestTexts() {
        if (instance == null) {
            instance = new ContestReader();
        }
        if (instance.getTexts().isEmpty()) {
            // new Thread(() -> INSTANCE.readFile(file)).start();
        }

        return instance.getTexts();
    }

    public static ContestReader getInstance() {
        return instance;
    }

    enum ReaderState {
        STATE_IGNORE,
        STATE_OPTION,
        STATE_QUESTION,
        STATE_TEXT;
    }

}