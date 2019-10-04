package contest;

import static simplebuilder.SimpleListViewBuilder.newCellFactory;

import contest.db.ContestQuestion;
import contest.db.ContestQuestionAnswer;
import contest.db.ContestText;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.StringSigaUtils;

public class ContestApplicationController {
    private static final String ERRADO0 = "errado0";
	private static final String CERTO0 = "certo0";
	private static final Logger LOG = HasLogging.log();
    @FXML
    private ScrollPane scrollPane2;
    @FXML
    private ListView<ContestQuestionAnswer> options;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Text text;
    @FXML
    private Text question;
    @FXML
    private ScrollPane scrollPane3;
    @FXML
    private ListView<ContestReader> allContests;
    private ContestReader contestQuestions;
    private IntegerProperty current = new SimpleIntegerProperty(-1);

    public void initialize() {
        ObservableList<ContestReader> allContests2 = ContestReader.getAllContests();
        allContests.setCellFactory(newCellFactory((item, cell) -> cell
            .setText(Objects.toString(item.getContest().getJob(), "") + "\n" + item.getContest().getName())));
        allContests.setItems(allContests2);
        if (!allContests2.isEmpty()) {
            contestQuestions = allContests2.get(0);
            splitPane.setDividerPositions(1. / 5, 3. / 5);
        } else {
            splitPane.getItems().remove(0);
        }
        options.getSelectionModel().selectedItemProperty().addListener((observable, old, value) -> {
            if (value == null) {
                return;
            }
            Boolean correct = value.getCorrect();
            ObservableList<ContestQuestion> contestTexts = contestQuestions.getListQuestions();
            Integer number = contestTexts.get(current.get()).getNumber();
            LOG.info("Question {} Answer {}", number, correct);
            splitPane.lookupAll(".cell").stream().map(Node::getStyleClass).forEach(e -> {
                if (e.contains(CERTO0)) {
                    e.add("certo");
                }
                if (e.contains(ERRADO0)) {
                    e.add("errado");
                }
            });
        });
        options.setCellFactory(newCellFactory((el, cell) -> {
            Text text1 = new Text(el.getAnswer());
            text1.wrappingWidthProperty().bind(scrollPane3.widthProperty().add(-10));
            cell.setGraphic(text1);
            cell.getStyleClass().removeAll("certo", "errado", CERTO0, ERRADO0);
            cell.getStyleClass().add(el.getCorrect() ? CERTO0 : ERRADO0);
        }));
        current.addListener((ob, old, value) -> {
            int cur = value.intValue();

            text.setText(ContestApplicationController.getText(contestQuestions, cur));
            if (cur == -1) {
                question.setText("");
                return;
            }
            ContestQuestion contestQuestion = contestQuestions.getListQuestions().get(cur);
            question.setText(contestQuestion.getExercise());
            ContestApplicationController.changeOptions(options, contestQuestion);
        });
        current.set(0);
    }

    public void onActionNext() {
        current.set((current.get() + 1) % contestQuestions.getListQuestions().size());
    }

    public void onMouseClickedListView1(MouseEvent e) {
        if (e.getClickCount() > 1) {
            ContestReader c = allContests.getSelectionModel().getSelectedItem();
            contestQuestions = c;
            current.set(-1);
            current.set(0);
        }
    }

    public void setContestQuestions(ContestReader contestQuestions) {
        this.contestQuestions = contestQuestions;
        current.set(-1);
        current.set(0);
    }

    static void changeOptions(ListView<ContestQuestionAnswer> options, ContestQuestion contestQuestion) {
        ObservableList<ContestQuestionAnswer> items = options.getItems();
        for (int i = 0; i < contestQuestion.getOptions().size(); i++) {
            if (i < 0 || i >= items.size()) {
                items.add(contestQuestion.getOptions().get(i));
            } else {
                items.set(i, contestQuestion.getOptions().get(i));
            }
        }
    }

    static String getText(ContestReader contestQuestions2, int cur) {
        List<String> map = contestQuestions2.getContestTexts().stream()
            .filter(t -> ContestApplicationController.isBetween(t, cur)).map(ContestText::getText)
            .filter(StringUtils::isNotBlank).flatMap(s -> Stream.of(s.split("\n"))).map(String::trim)
            .collect(Collectors.toList());

        int orElse = map.stream().mapToInt(String::length).max().orElse(0);

        return IntStream.range(0, map.size()).mapToObj(i -> ContestApplicationController.mapLines(map, orElse, i))
            .collect(Collectors.joining("\n"));
    }

    static boolean isBetween(ContestText tex, int j) {
        if (tex.getMin() == null || tex.getMax() == null) {
            return false;
        }
        int i = j + 1;
        return tex.getMin() <= i && tex.getMax() >= i;
    }

    static String mapLines(List<String> map, int orElse, int i) {

        String object = StringSigaUtils.justified(map, orElse, i);
        return String.format("(%02d)    %s", i + 1, object);
    }

}
