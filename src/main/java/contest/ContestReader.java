package contest;

import static contest.db.ContestQuestion.QUESTION_PATTERN;
import static contest.db.ContestText.TEXTS_PATTERN;
import static utils.StringSigaUtils.intValue;
import static utils.StringSigaUtils.removeNotPrintable;

import contest.db.*;
import extract.PdfImage;
import extract.PdfUtils;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.text.TextPosition;
import utils.HasImage;
import utils.HasLogging;
import utils.StringSigaUtils;

public class ContestReader implements HasLogging {
    private static final String DISCURSIVA_PATTERN = " *P *R *O *V *A *D *I *S *C *U *R *S *I *V *A *";
    private static final int OPTIONS_PER_QUESTION = 5;
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

    private final SimpleObjectProperty<ReaderState> state = new SimpleObjectProperty<>(ReaderState.IGNORE);

    private String subject;
    private ContestText text = new ContestText();
    private final ObservableList<ContestText> texts = FXCollections.observableArrayList();

    public ContestReader() {
        state.addListener((ob, old, value) -> getLogger().info("{}->{} {}", old, value, HasLogging.getCurrentLine(7)));
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

    public void readFile(File file) {
        contest = new Contest(Organization.IADES);
        PdfUtils.runOnFile(2, file, this::getQuestionPositions, i -> pageNumber = i, this::tryReadQuestionFromLines,
            this::mapImages);
    }

    public void readQuadrixFile(File file) {
        contest = new Contest(Organization.QUADRIX);
        PdfUtils.runOnFile(2, file, this::getQuestionPositions, i -> pageNumber = i, this::tryReadQuestionFromLines,
            this::mapImages);
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
            .collect(Collectors.groupingBy(ContestText::getText)).entrySet().stream().map(e -> e.getValue().get(0))
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

    private void addAllOptions(String s) {
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
    }

    private void addAnswer(String[] linhas, int i, String s) {
        answer.appendAnswer(s.trim() + " ");
        if (option == OPTIONS_PER_QUESTION) {
            if (i == linhas.length - 1) {
                addQuestion();
                return;
            }
            if (Stream.of(linhas).skip(i + 1L).allMatch(str -> str.matches(LINE_PATTERN))) {
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
            if (isQuestionPattern(string)) {
                addQuestion();
                setState(ReaderState.QUESTION);
                return;
            }
            if (StringUtils.containsIgnoreCase(string, "Questões") && s.matches(".+\\.\\s*")) {
                addQuestion();
                setState(ReaderState.IGNORE);
            }
        }
    }

    private void addNewText() {
        text.setContest(contest);
        texts.add(text);
        text = new ContestText(contest);
    }

    private void addOptionIfStateOption() {
        if (getState() == ReaderState.OPTION) {
            contestQuestion.addOption(answer);
            answer = new ContestQuestionAnswer();
            answer.setExercise(contestQuestion);
        }
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

    private void addTextIfMatches(String[] linhas, int i, String s) {
        if (isTextPattern(linhas, i, s)) {
            String[] split = linhas[i + 1].split("\\D+");
            if (split.length > 1 && Stream.of(split).allMatch(StringUtils::isNumeric)) {
                setState(ReaderState.TEXT);
                text.setMin(intValue(split[0]));
                text.setMax(intValue(split[1]));
            }
        }
    }

    private void addTextIfNeeded() {
        if (getState() == ReaderState.TEXT) {
            addNewText();
        }
    }

    private void executeAppending(String str, String[] linhas, int i) {
        if (getState() == ReaderState.IGNORE) {
            contestQuestion.setContest(contest);
            contestQuestion.setSubject(subject);
            return;
        }

        if (StringUtils.isNotBlank(str)) {
            switch (getState()) {
                case TEXT:
                    text.appendText(str + "\n");
                    break;
                case QUESTION:
                    contestQuestion.appendExercise(str + "\n");
                    break;
                case OPTION:
                    addAnswer(linhas, i, str);
                    break;
                default:
                    break;
            }
        }

    }

    private int getIndicative(int i) {

        if (getState() == ReaderState.OPTION) {
            return contestQuestion != null && contestQuestion.getOptions() != null
                ? contestQuestion.getOptions().size() + 1
                : 1;
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

    private void getQuestionPositions(String text1, List<TextPosition> textPositions) {
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

    private void insertOptionIfNeeded(String s) {
        if (getState() == ReaderState.OPTION && StringUtils.isBlank(s)) {
            contestQuestion.addOption(answer);
            if (option == OPTIONS_PER_QUESTION) {
                addQuestion();
            }
            setState(ReaderState.IGNORE);
        }
    }

    private boolean isBetween() {
        return text.getMin() != null && text.getMin() <= listQuestions.size() + 1 && text.getMax() != null
            && text.getMax() >= listQuestions.size() + 1;
    }

    private boolean isTextPattern(String[] linhas, int i, String s) {
        return StringUtils.containsIgnoreCase(s, "Questões") && getState() == ReaderState.IGNORE
            && i < linhas.length - 1;
    }

    private boolean isTextToBeAdded(String s) {
        return StringUtils.isBlank(s) && getState() == ReaderState.TEXT && !listQuestions.isEmpty()
            && StringUtils.isNotBlank(text.getText()) && !isBetween();
    }

    private void logIndicative(int i, String s) {
        if (StringUtils.isNotBlank(s)) {
            int size = getIndicative(i);
            getLogger().info("{} {} - {}", getState(), size, s);
        }
    }

    private void mapImages(int currentPage, List<PdfImage> images) {
        List<HasImage> imageElements = Stream.concat(texts.stream(), listQuestions.stream())
            .collect(Collectors.toList());
        for (PdfImage pdfImage : images) {
            questionPosition.stream().filter(e -> e.getPage() == currentPage)
                .min(Comparator.comparing(position -> position.distance(pdfImage.getX(), pdfImage.getY())))
                .ifPresent(position -> imageElements.stream().filter(p -> p.matches(position.getLine())).forEach(p -> p
                    .appendImage(pdfImage.getFile().getParentFile().getName() + "/" + pdfImage.getFile().getName())));
        }
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

        addTextIfMatches(linhas, i, s);
        if (s.matches(LINE_PATTERN)) {
            return;
        }
        if (isQuestionPattern(s)) {
            addTextIfNeeded();
            contestQuestion.setNumber(intValue(s));
            setState(ReaderState.QUESTION);
            getLogger().info(s);
            return;
        }
        if (s.matches(OPTION_PATTERN)) {
            addOptionIfStateOption();
            if (containsAllOptions(s)) {
                addAllOptions(s);
                return;
            }

            answer.setNumber(option);
            option++;
            setState(ReaderState.OPTION);

        }
        if (isTextToBeAdded(s)) {
            addNewText();

            setState(ReaderState.IGNORE);
        }
        insertOptionIfNeeded(s);

        executeAppending(s, linhas, i);

        logIndicative(i, s);
    }

    private void tryReadQuestionFromLines(String[] lines) {

        try {
            setState(ReaderState.IGNORE);
            option = 0;
            text = new ContestText(contest);
            texts.add(text);
            for (int i = 0; i < lines.length && !lines[i].matches(DISCURSIVA_PATTERN); i++) {
                processQuestion(lines, i);
            }
        } catch (Exception e) {
            getLogger().error("", e);
        }
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

    private static boolean containsAllOptions(String s) {
        return s.contains("(A)") && s.contains("(B)") && s.contains("(C)") && s.contains("(D)") && s.contains("(E)");
    }

    private static boolean isQuestionPattern(String s) {
        return s.matches(QUESTION_PATTERN) || s.startsWith("QUESTÃO");
    }

    private static boolean matchesQuestionPattern(String text1, List<TextPosition> textPositions) {
        return text1 != null && text1.matches(QUESTION_PATTERN + "|" + TEXTS_PATTERN) && !textPositions.isEmpty();
    }

    enum ReaderState {
        IGNORE,
        OPTION,
        QUESTION,
        TEXT;
    }

}