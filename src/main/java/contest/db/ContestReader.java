package contest.db;

import static utils.StringSigaUtils.intValue;

import java.io.File;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import pdfreader.PdfImage;
import pdfreader.PdfUtils;
import pdfreader.PrintImageLocations;
import utils.HasImage;
import utils.HasLogging;
import utils.StringSigaUtils;

public class ContestReader implements HasLogging {
    private static final String DISCURSIVA_PATTERN = "P *R *O *V *A *D *I *S *C *U *R *S *I *V *A *";
    private static final int OPTIONS_PER_QUESTION = 5;
    public static final String QUESTION_PATTERN = " *QUESTÃO +(\\d+)\\s*___+\\s+";
    public static final String TEXTS_PATTERN = ".+ para \\w* *[aà l]*s quest[õion]+es [de ]*(\\d+) [ae] (\\d+)\\.*\\s*";
    private static final String LINE_PATTERN = "^\\s*\\d+\\s*$";

    private static final String OPTION_PATTERN = "[\\. ]*[ \\(][A-E]\\).+";
    private static final String SUBJECT_PATTERN = "Questões de \\d+ a \\d+\\s*";
    private static final String SUBJECT_2_PATTERN = "(?i)(.+)[-–]*\\s*\\(*Quest.es .*\\d+ . \\d+\\)*\\s*";

    private static final ContestQuestionDAO CONTEST_DAO = new ContestQuestionDAO();

    private ContestQuestionAnswer answer = new ContestQuestionAnswer();

    private Contest contest;

    private ContestQuestion contestQuestion = new ContestQuestion();

    private final ObservableList<ContestQuestion> listQuestions = FXCollections.observableArrayList();
    private int option;
    private int pageNumber;
    private final List<QuestionPosition> questionPosition = new ArrayList<>();

    private final ObjectProperty<ReaderState> state = new SimpleObjectProperty<>(ReaderState.IGNORE);

    private String subject;
    private ContestText text = new ContestText();
    private final ObservableList<ContestText> texts = FXCollections.observableArrayList();

    public ContestReader() {
        state.addListener((ob, old, value) -> getLogger().info("{}->{} {}", old, value, getCurrentLine(7)));
    }

    public Contest getContest() {
        return contest;
    }

    public ObservableList<ContestText> getContestTexts() {
        return texts;
    }

    public ObservableList<ContestQuestion> getListQuestions() {
        return listQuestions;
    }

    public ReaderState getState() {
        return state.get();
    }

    public void saveAll() {
        if (!validate()) {
            return;
        }
        CONTEST_DAO.saveOrUpdate(getContest());
        CONTEST_DAO.saveOrUpdate(listQuestions);
        CONTEST_DAO.saveOrUpdate(listQuestions.stream().flatMap(e -> {
            e.getOptions().forEach(o -> o.setExercise(e));
            return e.getOptions().stream();
        }).collect(Collectors.toList()));

        List<ContestText> nonNullTexts = texts.stream().filter(e -> StringUtils.isNotBlank(e.getText()))
            .collect(Collectors.toList());
        CONTEST_DAO.saveOrUpdate(nonNullTexts);
    }

    public void setState(ReaderState state) {
        this.state.set(state);
    }

    public boolean validate() {
        if (listQuestions.stream().anyMatch(q -> q.getOptions().size() != OPTIONS_PER_QUESTION)) {
            List<Integer> invalid = listQuestions.stream().filter(e -> e.getOptions().size() != OPTIONS_PER_QUESTION)
                .map(ContestQuestion::getNumber).collect(Collectors.toList());
            getLogger().error("Invalid Questions {} {}/{}", invalid, invalid.size(), listQuestions.size());
            return false;
        }
        if (listQuestions.size() % 10 != 0) {
            getLogger().error("Invalid Questions Size {}", listQuestions.size());
            return false;
        }
        if (texts.isEmpty() || texts.stream().allMatch(e -> StringUtils.isBlank(e.getText()))) {
            getLogger().error("Text is Empty {}", texts);
            return false;
        }
        getLogger().info("Valid Questions ");
        return true;
    }

    private void addAnswer(String[] linhas, int i, String s) {
        answer.appendAnswer(s.trim() + " ");
        if (option == OPTIONS_PER_QUESTION) {
            if (i == linhas.length - 1) {
                addQuestion();
                return;
            }
            if (Stream.of(linhas).skip(i + 1).allMatch(str -> str.matches(LINE_PATTERN))) {
                addQuestion();
                setState(ReaderState.IGNORE);
                return;
            }
            String string = linhas[i + 1];
            if (string.matches(TEXTS_PATTERN)) {
                addQuestion();
                setState(ReaderState.TEXT);
                return;
            }
            if (string.matches(SUBJECT_2_PATTERN)) {
                addQuestion();
                setState(ReaderState.IGNORE);
                return;
            }
            if (string.matches(QUESTION_PATTERN) || string.startsWith("QUESTÃO")) {
                addQuestion();
                setState(ReaderState.QUESTION);
                return;
            }
            if (StringUtils.containsIgnoreCase(string, "Questões") && s.matches(".+\\.\\s*")) {
                addQuestion();
                setState(ReaderState.IGNORE);
                return;
            }
        }
    }

    private void addNewText() {
        text.setContest(contest);
        texts.add(text);
        text = new ContestText(contest);
    }

    private void addQuestion() {
        if (!contestQuestion.getOptions().isEmpty() && contestQuestion.getOptions().size() < OPTIONS_PER_QUESTION) {
            contestQuestion.addOption(answer);
        }
        answer = new ContestQuestionAnswer();
        listQuestions.add(contestQuestion);
        contestQuestion = new ContestQuestion();
        contestQuestion.setContest(contest);
        contestQuestion.setSubject(subject);
        answer.setExercise(contestQuestion);
        option = 0;
    }

    private void executeAppending(String str, String[] linhas, int i) {
        String s = str;
        if (getState() == ReaderState.IGNORE) {
            contestQuestion.setContest(contest);
            contestQuestion.setSubject(subject);
            return;
        }

        if (StringUtils.isNotBlank(s)) {
            switch (getState()) {
                case TEXT:
                    text.appendText(s + "\n");
                    break;
                case QUESTION:
                    contestQuestion.appendExercise(s + "\n");
                    break;
                case OPTION:
                    addAnswer(linhas, i, s);
                    break;
                default:
                    break;
            }
        }

    }

    private int getIndicative(int i) {

        if (getState() == ReaderState.OPTION) {
            int a = contestQuestion != null && contestQuestion.getOptions() != null
                ? contestQuestion.getOptions().size() + 1
                : 1;
            return a;
        }
        if (getState() == ReaderState.QUESTION) {
            Integer number = contestQuestion.getNumber();
            int j = listQuestions.size() + 1;
            if (number != null && j != number) {
                getLogger().error("ERROR HERE--- Question {}!={}", number, j);
            }
            return j;
        }
        if (getState() == ReaderState.TEXT) {
            return texts.size();
        }
        return i;
    }

    private boolean isBetween() {
        return text.getMin() != null && text.getMin() <= listQuestions.size() + 1 && text.getMax() != null
            && text.getMax() >= listQuestions.size() + 1;
    }

    private void processQuestion(String[] linhas, int i) {
        String s = removeNotPrintable(linhas[i]);
        if (s.matches(SUBJECT_PATTERN) && i > 0) {
            subject = linhas[i - 1];
            return;
        }

        if (s.matches(TEXTS_PATTERN)) {
            setState(ReaderState.TEXT);
            String[] split = s.replaceAll(TEXTS_PATTERN, "$1,$2").split(",");
            IntSummaryStatistics stats = Stream.of(split).mapToInt(StringSigaUtils::intValue).summaryStatistics();
            text.setMin(stats.getMin());
            text.setMax(stats.getMax());
            return;
        }
        if (s.matches(SUBJECT_2_PATTERN)) {
            subject = linhas[i].split("[-–\\(]")[0].toUpperCase();
            return;
        }
        if (StringUtils.containsIgnoreCase(s, "Questões") && getState() == ReaderState.IGNORE
            && i < linhas.length - 1) {
            String[] split = linhas[i + 1].split("\\D+");
            if (split.length > 1 && Stream.of(split).allMatch(StringUtils::isNumeric)) {
                setState(ReaderState.TEXT);
                text.setMin(intValue(split[0]));
                text.setMax(intValue(split[1]));
            }
        }
        if (s.matches(LINE_PATTERN)) {
            return;
        }
        if (s.matches(QUESTION_PATTERN) || s.startsWith("QUESTÃO")) {
            if (getState() == ReaderState.TEXT) {
                addNewText();
            }
            contestQuestion.setNumber(intValue(s));
            setState(ReaderState.QUESTION);
            getLogger().info(s);
            return;
        }
        if (s.matches(OPTION_PATTERN)) {
            if (getState() == ReaderState.OPTION) {
                contestQuestion.addOption(answer);
                answer = new ContestQuestionAnswer();
                answer.setExercise(contestQuestion);
            }
            if (s.contains("(A)") && s.contains("(B)") && s.contains("(C)") && s.contains("(D)") && s.contains("(E)")) {
                if (contestQuestion.getOptions() != null && !contestQuestion.getOptions().isEmpty()) {
                    contestQuestion.getOptions().clear();
                }
                String[] split = s.split("(?=\\()");
                for (int j = 0; j < split.length; j++) {
                    String string = split[j];
                    answer = new ContestQuestionAnswer();
                    answer.setExercise(contestQuestion);
                    answer.appendAnswer(string);
                    answer.setNumber(j);
                    contestQuestion.addOption(answer);
                }
                addQuestion();
                setState(ReaderState.IGNORE);
                getLogger().info(s);
                return;
            }

            answer.setNumber(option);
            option++;
            setState(ReaderState.OPTION);

        }
        if (StringUtils.isBlank(s) && getState() == ReaderState.TEXT && !listQuestions.isEmpty()
            && StringUtils.isNotBlank(text.getText()) && !isBetween()) {
            addNewText();

            setState(ReaderState.IGNORE);
        }
        if (getState() == ReaderState.OPTION && StringUtils.isBlank(s)) {
            contestQuestion.addOption(answer);
            if (option == OPTIONS_PER_QUESTION) {
                addQuestion();
            }
            setState(ReaderState.IGNORE);
        }

        executeAppending(s, linhas, i);

        if (StringUtils.isNotBlank(s)) {
            int size = getIndicative(i);
            getLogger().info("{} {} - {}", getState(), size, s);
        }
    }

    private void readFile(File file) {
        PdfUtils.extractImages(file);

        try (RandomAccessFile source = new RandomAccessFile(file, "r");
            COSDocument cosDoc = PdfUtils.parseAndGet(source);
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
            for (int i = 2; i <= numberOfPages; i++) {
                PDPage page = pdDoc.getPage(i - 1);
                pageNumber = i;
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                List<PdfImage> images = printImageLocations.processPage(page, i);
                String parsedText = extractErrors(pdfStripper.getText(pdDoc));
                String[] lines = parsedText.split("\r\n");
                tryReadQuestionFromLines(lines);
                List<HasImage> imageElements = Stream.concat(texts.stream(), listQuestions.stream())
                    .collect(Collectors.toList());
                final int j = i;
                for (PdfImage pdfImage : images) {
                    questionPosition.stream().filter(e -> e.getPage() == j)
                        .min(Comparator.comparing(position -> position.distance(pdfImage.getX(), pdfImage.getY())))
                        .ifPresent(position -> imageElements.stream().filter(p -> p.matches(position.getLine()))
                            .forEach(p -> p.appendImage(
                                pdfImage.getFile().getParentFile().getName() + "/" + pdfImage.getFile().getName())));
                }
            }
        } catch (Throwable e) {
            getLogger().error("", e);
        }
    }

    private ContestQuestion tryReadQuestionFromLines(String[] lines) {

        try {
            setState(ReaderState.IGNORE);
            option = 0;
            text = new ContestText(contest);
            texts.add(text);
//            answer.setExercise(contestQuestion);
            for (int i = 0; i < lines.length; i++) {
                processQuestion(lines, i);
                if (lines[i].matches(DISCURSIVA_PATTERN)) {
                    break;
                }
            }
        } catch (Exception e) {
            getLogger().error("", e);
        }
        return null;
    }

    public static ObservableList<ContestReader> getAllContests() {
        List<Contest> listContests = CONTEST_DAO.listContests();
        List<ContestText> listTexts = CONTEST_DAO.listTexts();
        Map<Contest, List<ContestText>> textsByContest = listTexts.stream()
            .collect(Collectors.groupingBy(ContestText::getContest));
        return listContests.stream().map(c -> {
            ContestReader contestReader = new ContestReader();
            contestReader.contest = c;
            contestReader.listQuestions.setAll(CONTEST_DAO.list(c));
            contestReader.texts.setAll(textsByContest.getOrDefault(c, Collections.emptyList()));
            return contestReader;
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static ContestReader getContestQuestions(File file) {
        ContestReader instance = new ContestReader();
        new Thread(() -> instance.readFile(file)).start();
        return instance;
    }

    @SafeVarargs
    public static ContestReader getContestQuestions(File file, Consumer<ContestReader>... r) {
        ContestReader instance = new ContestReader();
//        new Thread(() -> {
        HasLogging.log().info("READING {}", file);
        instance.readFile(file);
        Stream.of(r).forEach(e -> e.accept(instance));
//        }).start();
        return instance;
    }

    public static ContestReader getContestQuestions(File file, Runnable... r) {
        ContestReader instance = new ContestReader();
        new Thread(() -> {
            instance.readFile(file);
            Stream.of(r).forEach(Runnable::run);
        }).start();
        return instance;
    }

    private static String extractErrors(String s) {
        if (s.codePoints().mapToObj(UnicodeBlock::of).distinct().collect(Collectors.toList())
            .contains(UnicodeBlock.MATHEMATICAL_OPERATORS)) {
            return s.replaceAll("[\u2200-\u22FF]", "?");
        }
        return s;
    }

    private static boolean matchesQuestionPattern(String text1, List<TextPosition> textPositions) {
        return text1 != null && text1.matches(QUESTION_PATTERN + "|" + TEXTS_PATTERN) && !textPositions.isEmpty();
    }

    private static String removeNotPrintable(String s) {
        String fixEncoding = StringSigaUtils.fixEncoding(s.replaceAll("\t", " "), StandardCharsets.UTF_8,
            Charset.forName("CESU-8"));
        String replace = fixEncoding.replaceAll("[\u0000-\u0010]", "?");
        if (!replace.equals(s)) {
            return replace;
        }
        return replace;
    }

    enum ReaderState {
        IGNORE,
        OPTION,
        QUESTION,
        TEXT;
    }

}