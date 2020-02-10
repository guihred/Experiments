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

    public static ObservableList<ContestReader> getAllContests() {
        List<Contest> listContests = ContestHelper.CONTEST_DAO.listContests();
        List<ContestText> listTexts = ContestHelper.CONTEST_DAO.listTexts();
        Map<Contest, List<ContestText>> textsByContest = listTexts.stream()
            .collect(Collectors.groupingBy(ContestText::getContest));
        return listContests.stream().map(c -> {
            ContestReader contestReader = new ContestReader();
            contestReader.setContest(c);
            contestReader.getListQuestions().setAll(ContestHelper.CONTEST_DAO.list(c));
            contestReader.getTexts().setAll(textsByContest.getOrDefault(c, Collections.emptyList()));
            return contestReader;
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static void saveAll(Contest contest, ObservableList<ContestQuestion> listQuestions,
        ObservableList<ContestText> texts) {
        ContestHelper.CONTEST_DAO.saveOrUpdate(contest);
        ContestHelper.CONTEST_DAO.saveOrUpdate(listQuestions);
        ContestHelper.CONTEST_DAO
            .saveOrUpdate(listQuestions.stream().filter(e -> e.getOptions() != null).flatMap(e -> {
                e.getOptions().forEach(o -> o.setExercise(e));
                return e.getOptions().stream();
            }).collect(Collectors.toList()));

        List<ContestText> nonNullTexts = texts.stream().filter(e -> StringUtils.isNotBlank(e.getText()))
            .collect(Collectors.groupingBy(ContestText::getText)).entrySet().stream().map(e -> e.getValue().get(0))
            .collect(Collectors.toList());
        ContestHelper.CONTEST_DAO.saveOrUpdate(nonNullTexts);
    }

    enum ReaderState {
        IGNORE,
        OPTION,
        QUESTION,
        TEXT;
    }

}
