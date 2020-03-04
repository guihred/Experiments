package contest;

import static contest.db.ContestQuestion.QUESTION_PATTERN;
import static contest.db.ContestText.TEXTS_PATTERN;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static utils.StringSigaUtils.intValue;
import static utils.StringSigaUtils.removeNotPrintable;

import contest.db.*;
import extract.PdfImage;
import extract.PdfUtils;
import java.io.File;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.text.TextPosition;
import utils.HasImage;
import utils.StringSigaUtils;

public class ContestReader extends ContestDTO {

    public void readFile(File file, Organization organization) {
        setContest(new Contest(organization));
        PdfUtils.runOnFile(organization == Organization.QUADRIX ? 1 : 2, file, this::getQuestionPositions,
            i -> pageNumber = i, this::tryReadQuestionFromLines, this::mapImages);
    }

    public void saveAll() {
        if (!validate()) {
            return;
        }
        ContestHelper.saveAll(contest, listQuestions, texts);
    }


    public boolean validate() {
        if (questionType == QuestionType.OPTIONS
            && listQuestions.stream().anyMatch(q -> q.getOptions().size() != OPTIONS_PER_QUESTION)) {
            List<Integer> invalid = listQuestions.stream().filter(e -> e.getOptions().size() != OPTIONS_PER_QUESTION)
                .map(ContestQuestion::getNumber).collect(Collectors.toList());
            getLogger().error("Invalid Questions {} {}/{}", invalid, invalid.size(), listQuestions.size());
            return false;
        }
        if (StringUtils.isBlank(contest.getJob()) || StringUtils.isBlank(contest.getName())) {
            getLogger().error("Invalid Name or Job");
            return false;
        }
        if (listQuestions.size() % 10 != 0) {
            if (StringUtils.isNotBlank(contestQuestion.getExercise())
                && listQuestions.stream().noneMatch(e -> e == contestQuestion)) {
                listQuestions.add(contestQuestion);
            }
            if (listQuestions.size() % 10 != 0) {
                getLogger().error("Invalid Questions Size {}", listQuestions.size());
                return false;
            }
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
        setState(ContestDTO.ReaderState.IGNORE);
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
                setState(ContestDTO.ReaderState.IGNORE);
                return;
            }
            String string = linhas[i + 1];
            if (string.matches(TEXTS_PATTERN)) {
                addQuestion();
                setState(ContestDTO.ReaderState.TEXT);
                return;
            }
            if (string.matches(SUBJECT_2_PATTERN)) {
                addQuestion();
                setState(ContestDTO.ReaderState.IGNORE);
                return;
            }
            if (isQuestionPattern(string)) {
                addQuestion();
                setState(ContestDTO.ReaderState.QUESTION);
                return;
            }
            if (StringUtils.containsIgnoreCase(string, "Questões") && s.matches(".+\\.\\s*")) {
                addQuestion();
                setState(ContestDTO.ReaderState.IGNORE);
            }
        }
    }

    private void addNewText() {
        text.setContest(getContest());
        if (StringUtils.isNotBlank(text.getText())) {
            texts.add(text);
            if (text.getMin() == null) {
                text.setMin(Math.max(1, listQuestions.size() + 1));
            }
            if (text.getMax() == null) {
                text.setMax(Math.max(2, listQuestions.size() + 2));
            }
        }

        text = new ContestText(getContest());
    }

    private void addOptionIfStateOption() {
        if (getState() == ContestDTO.ReaderState.OPTION) {
            contestQuestion.addOption(answer);
            answer = new ContestQuestionAnswer();
            answer.setExercise(contestQuestion);
        }
    }

    private void addQuestion() {
        if (questionType == QuestionType.OPTIONS && !contestQuestion.getOptions().isEmpty()
            && contestQuestion.getOptions().size() < OPTIONS_PER_QUESTION) {
            contestQuestion.addOption(answer);
        }
        if (questionType == QuestionType.OPTIONS) {
            answer = new ContestQuestionAnswer();
        }
        if (containsIgnoreCase(contestQuestion.getExercise(), "texto") && isBetween()
            && contestQuestion.getNumber() != null) {
            text.setMax(contestQuestion.getNumber());
        }
        if (StringUtils.isBlank(contestQuestion.getSubject())) {
            contestQuestion.setSubject(subject);
        }
        contestQuestion.setType(questionType);
        listQuestions.add(contestQuestion);
        contestQuestion = new ContestQuestion();
        contestQuestion.setContest(getContest());
        contestQuestion.setSubject(subject);
        contestQuestion.setType(questionType);
        if (questionType == QuestionType.OPTIONS) {
            answer.setExercise(contestQuestion);
            option = 0;
        }
    }

    private void addQuestionIfDifferentNumber(String s) {
        if (intValue(s.split("\\D+")[0]) != listQuestions.size() + 1) {
            addQuestion();
        }
    }

    private void addTextIfEnded(String s) {
        if (isTextToBeAdded(s)) {
            addNewText();
            setState(ContestDTO.ReaderState.IGNORE);
        }
    }

    private void addTextIfMatches(String[] linhas, int i, String s) {
        if (isTextPattern(linhas, i, s)) {
            String[] split = linhas[i + 1].split("\\D+");
            if (split.length > 1 && Stream.of(split).allMatch(StringUtils::isNumeric)) {
                setState(ContestDTO.ReaderState.TEXT);
                text.setMin(intValue(split[0]));
                text.setMax(intValue(split[1]));
            }
        }
    }

    private void addTextIfNeeded() {
        if (getState() == ContestDTO.ReaderState.TEXT) {
            addNewText();
        }
    }

    private void addTextIfQuestion() {
        if (getState() == ContestDTO.ReaderState.QUESTION) {
            addQuestion();
            addNewText();
        }
    }

    private void changeTypeOfQuestions(String s) {
        if (getState() == ContestDTO.ReaderState.IGNORE && s.matches(".*CERTO.+ERRADO.*")
            && getContest().getOrganization() == Organization.QUADRIX) {
            questionType = QuestionType.TRUE_FALSE;
        }
    }

    private void executeAppending(String str, String[] linhas, int i) {
        if (getState() == ContestDTO.ReaderState.IGNORE) {
            contestQuestion.setContest(getContest());
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

        if (getState() == ContestDTO.ReaderState.OPTION) {
            return contestQuestion != null && contestQuestion.getOptions() != null
                ? contestQuestion.getOptions().size() + 1
                : 1;
        }
        if (getState() == ContestDTO.ReaderState.QUESTION) {
            Integer number = contestQuestion.getNumber();
            int j = listQuestions.size() + 1;
            if (number != null && j != number) {
                getLogger().error("ERROR HERE--- Question {}!={}", number, j);
            }
            return j;
        }
        if (getState() == ContestDTO.ReaderState.TEXT) {
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
        if (getState() == ContestDTO.ReaderState.OPTION && StringUtils.isBlank(s)) {
            contestQuestion.addOption(answer);
            if (option == OPTIONS_PER_QUESTION) {
                addQuestion();
            }
            setState(ContestDTO.ReaderState.IGNORE);
        }
    }

    private boolean isBetween() {
        return text.getMin() != null && text.getMin() <= listQuestions.size() + 1 && text.getMax() != null
            && text.getMax() >= listQuestions.size() + 1;
    }

    private boolean isEndOfQuestion(String s) {
        return questionType == QuestionType.TRUE_FALSE && s.matches("^\\s*_____+\\s*$");
    }

    private boolean isSubject(String s) {
        return s.matches(CONHECIMENTO) && getState() == ContestDTO.ReaderState.IGNORE;
    }

    private boolean isTextMultiLine() {
        return StringUtils.isNotBlank(text.getText()) && text.getText().split("\n").length > 1;
    }

    private boolean isTextPattern(String[] linhas, int i, String s) {
        return containsIgnoreCase(s, "Questões") && getState() == ContestDTO.ReaderState.IGNORE
            && i < linhas.length - 1;
    }

    private boolean isTextToBeAdded(String s) {
        return StringUtils.isBlank(s) && getState() == ContestDTO.ReaderState.TEXT && !listQuestions.isEmpty()
            && isTextMultiLine() && !isBetween();
    }

    private boolean isTrueFalseQuestion(String s) {
        return questionType == QuestionType.TRUE_FALSE
            && (s.matches(listQuestions.size() + 1 + " \\S+.+") || s.matches(listQuestions.size() + 2 + " \\S+.+"));
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
            getLogger().info("SUBJECT={}", subject);
            return;
        }
        if (isSubject(s)) {
            subject = s.trim();
            getLogger().info("SUBJECT={}", subject);
            setState(ContestDTO.ReaderState.TEXT);
            return;
        }
        changeTypeOfQuestions(s);
        if (hasTexto(s)) {
            setState(ContestDTO.ReaderState.TEXT);
            String[] split = s.split("\\D+");
            IntSummaryStatistics stats = Stream.of(split).filter(StringUtils::isNotBlank)
                .mapToInt(StringSigaUtils::intValue).summaryStatistics();
            text.setMin(stats.getMin());
            text.setMax(stats.getMax());
            return;
        }
        if (s.matches(SUBJECT_2_PATTERN)) {
            subject = linhas[i].split("[-–\\(]")[0].toUpperCase();
            getLogger().info("SUBJECT={}", subject);
            return;
        }

        addTextIfMatches(linhas, i, s);
        if (s.matches(LINE_PATTERN)) {
            return;
        }
        if (isEndOfQuestion(s)) {
            addTextIfQuestion();
            setState(ContestDTO.ReaderState.TEXT);
            return;
        }
        verifyTrueFalseQuestion(s);
        if (isQuestionPattern(s)) {
            addTextIfNeeded();
            contestQuestion.setNumber(intValue(s));
            setState(ContestDTO.ReaderState.QUESTION);
            getLogger().info(s);
            return;
        }
        if (containsAllOptions(s)) {
            addOptionIfStateOption();
            addAllOptions(s);
            return;
        }
        verifyOptionPattern(s);
        addTextIfEnded(s);
        insertOptionIfNeeded(s);
        logIndicative(i, s);
        executeAppending(s, linhas, i);

    }

    private void tryReadQuestionFromLines(String[] lines) {
        try {
            setState(ContestDTO.ReaderState.IGNORE);
            option = 0;
            text = new ContestText(getContest());
            texts.add(text);
            for (int i = 0; i < lines.length && !removeNotPrintable(lines[i]).matches(DISCURSIVA_PATTERN); i++) {
                processQuestion(lines, i);
            }

        } catch (Exception e) {
            getLogger().error("", e);
        }
    }

    private void verifyOptionPattern(String s) {
        if (s.matches(OPTION_PATTERN)) {
            addOptionIfStateOption();
            answer.setNumber(option);
            option++;
            setState(ContestDTO.ReaderState.OPTION);
        }
    }

    private void verifyTrueFalseQuestion(String s) {
        if (isTrueFalseQuestion(s)) {
            addQuestionIfDifferentNumber(s);
            contestQuestion.setNumber(intValue(s.split("\\D+")[0]));
            setState(ContestDTO.ReaderState.QUESTION);
        }
    }

    private static boolean containsAllOptions(String s) {
        return s.contains("(A)") && s.contains("(B)") && s.contains("(C)") && s.contains("(D)") && s.contains("(E)");
    }

    private static boolean hasTexto(String s) {
        return s.matches(TEXTS_PATTERN) || s.startsWith("Texto");
    }

    private static boolean isQuestionPattern(String s) {
        return s.matches(QUESTION_PATTERN) || s.startsWith(QUESTAO);
    }

    private static boolean matchesQuestionPattern(String text1, List<TextPosition> textPositions) {
        return text1 != null && text1.matches(QUESTION_PATTERN + "|" + TEXTS_PATTERN) && !textPositions.isEmpty();
    }

}