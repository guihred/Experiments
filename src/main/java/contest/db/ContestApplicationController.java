package contest.db;

import static simplebuilder.SimpleListViewBuilder.newCellFactory;

import japstudy.db.HibernateUtil;
import java.util.Objects;
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
import javafx.stage.Stage;
import org.slf4j.Logger;
import schema.sngpc.FXMLCreatorHelper;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ContestApplicationController {
    private static final Logger LOG = HasLogging.log();
    @FXML
    private ScrollPane scrollPane2;
    @FXML
    private ListView<ContestQuestionAnswer> options;
    @FXML
    private SplitPane splitPane0;
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
            splitPane0.setDividerPositions(1. / 5, 3. / 5);
        } else {
            splitPane0.getItems().remove(0);
        }
        options.getSelectionModel().selectedItemProperty().addListener((observable, old, value) -> {
            if (value == null) {
                return;
            }
            Boolean correct = value.getCorrect();
            ObservableList<ContestQuestion> contestTexts = contestQuestions.getListQuestions();
            Integer number = contestTexts.get(current.get()).getNumber();
            LOG.info("Question {} Answer {}", number, correct);
            splitPane0.lookupAll(".cell").stream().map(Node::getStyleClass).forEach(e -> {
                if (e.contains("certo0")) {
                    e.add("certo");
                }
                if (e.contains("errado0")) {
                    e.add("errado");
                }
            });
        });
        options.setCellFactory(newCellFactory((el, cell) -> {
            Text text1 = new Text(el.getAnswer());
            text1.wrappingWidthProperty().bind(scrollPane3.widthProperty().add(-10));
            cell.setGraphic(text1);
            cell.getStyleClass().removeAll("certo", "errado", "certo0", "errado0");
            cell.getStyleClass().add(el.getCorrect() ? "certo0" : "errado0");
        }));
        current.addListener((ob, old, value) -> {
            int cur = value.intValue();

            text.setText(ContestApplication.getText(contestQuestions, cur));
            if (cur == -1) {
                question.setText("");
                return;
            }
            ContestQuestion contestQuestion = contestQuestions.getListQuestions().get(cur);
            question.setText(contestQuestion.getExercise());
            ContestApplication.extracted(options, contestQuestion);
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

    public static void main(String[] args) {
        Stage duplicate = FXMLCreatorHelper.duplicate(ResourceFXUtils.toFile("ContestApplication.fxml"),
            "Contest Application");
        duplicate.setOnCloseRequest(e -> HibernateUtil.shutdown());
    }
}