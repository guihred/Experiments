package contest;

import contest.db.Contest;
import contest.db.ContestQuestion;
import contest.db.ContestText;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

public final class ContestHelper {

    private static final ContestQuestionDAO CONTEST_DAO = new ContestQuestionDAO();

    private ContestHelper() {
    }

    public static void deleteContest(Contest hasEqual) {
        CONTEST_DAO.delete(CONTEST_DAO.listTexts(hasEqual));
        List<ContestQuestion> listQuestions2 = listByContest(hasEqual);
        CONTEST_DAO.delete(listQuestions2.stream().filter(e -> e.getOptions() != null)
            .flatMap(e -> e.getOptions().stream()).collect(Collectors.toList()));
        CONTEST_DAO.delete(listQuestions2);
        CONTEST_DAO.delete(hasEqual);
    }

    public static ObservableList<ContestDTO> getAllContests() {
        Map<Contest, List<ContestText>> textsByContest = textsByContest();
        return listContests().stream().map(c -> {
            ContestDTO contestReader = new ContestDTO();
            contestReader.setContest(c);
            contestReader.getListQuestions().setAll(listByContest(c));
            contestReader.getTexts().setAll(textsByContest.getOrDefault(c, Collections.emptyList()));
            return contestReader;
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static List<Contest> hasEqual(Contest contest) {
        return CONTEST_DAO.hasEqual(contest);
    }

    public static List<ContestQuestion> listByContest(Contest c) {
        return CONTEST_DAO.list(c);
    }

    public static List<Contest> listContests() {
        return CONTEST_DAO.listContests();
    }

    public static void saveAll(Contest contest, ObservableList<ContestQuestion> listQuestions,
        ObservableList<ContestText> texts) {
        List<Contest> equals = CONTEST_DAO.hasEqual(contest);
        if (equals != null && !equals.isEmpty()) {
            for (Contest hasEqual : equals) {
                deleteContest(hasEqual);
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

    public static Map<Contest, List<ContestText>> textsByContest() {
        List<ContestText> listTexts = CONTEST_DAO.listTexts();
        return listTexts.stream().collect(Collectors.groupingBy(ContestText::getContest));
    }

}
