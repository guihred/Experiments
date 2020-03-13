package contest;

import contest.db.*;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.HasLogging;

public class ContestDTO implements HasLogging {
    protected static final String CONHECIMENTO = "C(?i)ONHECIMENTO.*|L[íi]ngua.*";
    protected static final String DISCURSIVA_PATTERN = " *P *R *O *V *A *D *I *S *C *U *R *S *I *V *A *";
    protected static final int OPTIONS_PER_QUESTION = 5;
    protected static final String LINE_PATTERN = "^\\s*\\d+\\s*$";

    protected static final String OPTION_PATTERN = "[\\. ]*[ \\(][A-E]\\).+";
    protected static final String SUBJECT_PATTERN = "(?i)[\\w ]*Questões de \\d+ a \\d+\\.*\\s*";
    protected static final String SUBJECT_2_PATTERN = "(?i)(.+)[-–]*\\s*\\(*Quest.*es .*\\d+ . \\d+\\)*\\s*";

    protected ContestQuestionAnswer answer = new ContestQuestionAnswer();
    protected Contest contest;
    protected ContestQuestion contestQuestion = new ContestQuestion();
    protected final ObservableList<ContestQuestion> listQuestions = FXCollections.observableArrayList();
    protected QuestionType questionType = QuestionType.OPTIONS;
    protected int option;
    protected int pageNumber;
    protected final List<QuestionPosition> questionPosition = new ArrayList<>();

    protected final SimpleObjectProperty<ContestDTO.ReaderState> state = new SimpleObjectProperty<>(
        ContestDTO.ReaderState.IGNORE);

    protected String subject;
    protected ContestText text = new ContestText();
    protected final ObservableList<ContestText> texts = FXCollections.observableArrayList();

    public ContestDTO() {
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

    public ContestDTO.ReaderState getState() {
        return state.get();
    }

    public ObservableList<ContestText> getTexts() {
        return texts;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public void setState(ContestDTO.ReaderState state) {
        this.state.set(state);
    }

    enum ReaderState {
        IGNORE,
        OPTION,
        QUESTION,
        TEXT;
    }

}