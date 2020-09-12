package contest;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static utils.StringSigaUtils.putNumbers;
import static utils.ex.FunctionEx.mapIf;

import contest.db.Contest;
import contest.db.ContestQuestion;
import contest.db.ContestText;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

public final class ContestHelper {

    private static final ContestQuestionDAO CONTEST_DAO = new ContestQuestionDAO();

    private ContestHelper() {
    }

    public static void fixContests(Collection<ContestDTO> allContests2) {
        allContests2.stream().filter(e -> isBlank(formatDTO(e)))
            .forEach(e -> CONTEST_DAO.deleteContest(e.getContest()));
        allContests2.stream().map(e -> CONTEST_DAO.hasEqual(e.getContest()))
            .forEach(e -> e.forEach(CONTEST_DAO::deleteContest));
    }

    public static String formatDTO(ContestDTO item) {
        return mapIf(item, it -> Stream.of(it.getContest().getJob(), it.getContest().getName()).filter(Objects::nonNull)
            .collect(Collectors.joining("\n")));
    }

    public static ObservableList<ContestDTO> getAllContests() {
        Map<Contest, List<ContestText>> textsByContest = textsByContest();
        return CONTEST_DAO.listContests().stream().map(c -> {
            ContestDTO contestReader = new ContestDTO();
            contestReader.setContest(c);
            contestReader.getListQuestions().setAll(CONTEST_DAO.list(c));
            contestReader.getTexts().setAll(textsByContest.getOrDefault(c, Collections.emptyList()));
            return contestReader;
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static Stream<ContestText> getContestTexts(Collection<ContestText> contestTexts, int cur) {
        return contestTexts.stream().filter(t -> isBetween(t, cur));
    }

    public static String getText(ContestDTO contestQuestions2, int cur) {
        return putNumbers(getLinesOfText(contestQuestions2, cur));
    }

    public static void saveAll(Contest contest, ObservableList<ContestQuestion> listQuestions,
        ObservableList<ContestText> texts) {
        List<Contest> equals = CONTEST_DAO.hasEqual(contest);
        if (equals != null && !equals.isEmpty()) {
            for (Contest hasEqual : equals) {
                CONTEST_DAO.deleteContest(hasEqual);
            }
        }

        CONTEST_DAO.saveOrUpdate(contest);
        CONTEST_DAO.saveOrUpdate(listQuestions);
        CONTEST_DAO.saveOrUpdate(listQuestions.stream().filter(e -> e.getOptions() != null)
            .flatMap(e -> e.getOptions().stream().peek(o -> o.setExercise(e))).collect(Collectors.toList()));

        List<ContestText> nonNullTexts = texts.stream().filter(e -> StringUtils.isNotBlank(e.getText()))
            .collect(Collectors.groupingBy(ContestText::getText)).entrySet().stream().map(e -> e.getValue().get(0))
            .collect(Collectors.toList());
        CONTEST_DAO.saveOrUpdate(nonNullTexts);
    }

    private static List<String> getLinesOfText(ContestDTO contestQuestions2, int cur) {
        List<ContestText> contestTexts = contestQuestions2.getContestTexts();
        if (contestTexts.stream().noneMatch(t -> isBetween(t, cur))) {
            ContestQuestion contestQuestion = contestQuestions2.getListQuestions().get(cur);
            String subject = contestQuestion.getSubject();
            ContestText distinct = contestQuestions2.getListQuestions().stream()
                .filter(e -> Objects.equals(e.getSubject(), subject))
                .flatMap(e -> getContestTexts(contestTexts, e.getNumber())).distinct()
                .min(Comparator.comparing(e -> Math.abs(e.getMax() + e.getMin() - cur * 2))).orElse(null);
            if (distinct != null && distinct.getText() != null) {
                return Stream.of(distinct.getText().split("\n")).filter(StringUtils::isNotBlank).map(String::trim)
                    .collect(Collectors.toList());
            }
        }

        return getContestTexts(contestTexts, cur).map(ContestText::getText).filter(StringUtils::isNotBlank)
            .flatMap(s -> Stream.of(s.split("\n"))).distinct().map(String::trim).collect(Collectors.toList());
    }

    private static boolean isBetween(ContestText tex, int j) {
        if (tex.getMin() == null || tex.getMax() == null) {
            return false;
        }
        int i = j + 1;
        return tex.getMin() <= i && tex.getMax() >= i;
    }

    private static Map<Contest, List<ContestText>> textsByContest() {
        return CONTEST_DAO.listTexts().stream().collect(Collectors.groupingBy(ContestText::getContest));
    }

}
