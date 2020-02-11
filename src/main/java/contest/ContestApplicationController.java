package contest;

import static java.lang.String.format;
import static simplebuilder.SimpleListViewBuilder.newCellFactory;
import static utils.FunctionEx.mapIf;

import contest.db.ContestQuestion;
import contest.db.ContestQuestionAnswer;
import contest.db.ContestText;
import contest.db.QuestionType;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import utils.ImageTableCell;
import utils.StringSigaUtils;

public class ContestApplicationController {
    private static final String ERRADO0 = "errado0";
    private static final String CERTO0 = "certo0";
    @FXML
    private ScrollPane scrollPane2;
    @FXML
    private ListView<ContestQuestion> questions;
    @FXML
    private ListView<ContestQuestionAnswer> options;
    @FXML
    private VBox images;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Text text;
    @FXML
    private Text questionNumber;
    @FXML
    private Text question;
    @FXML
    private ScrollPane scrollPane3;
    @FXML
    private ListView<ContestDTO> allContests;
    private ContestDTO contestQuestions;
    @FXML
    private IntegerProperty current;

    public void initialize() {
        current.set(-1);
        ObservableList<ContestDTO> allContests2 = ContestReader.getAllContests();
        allContests.setCellFactory(newCellFactory(ContestApplicationController::setText));
        allContests.setItems(allContests2);
        if (!allContests2.isEmpty()) {
            contestQuestions = allContests2.get(0);
            questions.setItems(contestQuestions.getListQuestions());
            splitPane.setDividerPositions(1. / 6, 2. / 6, 4. / 6);
        } else {
            splitPane.getItems().remove(0);
        }

        questions.getSelectionModel().selectedIndexProperty().addListener((o, old, n) -> current.set(n.intValue()));

        questions.setCellFactory(
            newCellFactory((c, v) -> v.setText(mapIf(c, c0 -> format("%s nº%d", c0.getSubject(), c0.getNumber())))));
        options.getSelectionModel().selectedItemProperty().addListener((observable, old, value) -> {
            if (value == null) {
                return;
            }
            ObservableList<ContestQuestion> contestTexts = contestQuestions.getListQuestions();
            questions.setItems(contestTexts);
            splitPane.lookupAll(".cell").stream().map(Node::getStyleClass).forEach(e -> {
                if (e.contains(CERTO0)) {
                    e.add("certo");
                }
                if (e.contains(ERRADO0)) {
                    e.add("errado");
                }
            });
        });
        questionNumber.textProperty().bind(current.add(1).asString("Questão %d"));
        updateCellFactory();
        current.addListener((ob, old, value) -> onCurrentChange(value));
        current.set(0);
    }

    public void onActionNext() {
        current.set((current.get() + 1) % contestQuestions.getListQuestions().size());
    }

    public void onMouseClickedListView1(MouseEvent e) {
        if (e.getClickCount() > 1) {

            ContestDTO c = allContests.getSelectionModel().getSelectedItem();
            contestQuestions = c;
            current.set(-1);
            questions.setItems(c.getListQuestions());
            current.set(0);
        }
    }

    public void setContestQuestions(ContestReader contestQuestions) {
        this.contestQuestions = contestQuestions;
        current.set(-1);
        questions.setItems(contestQuestions.getListQuestions());
        current.set(0);
    }

    private void addImages(String item) {
        ImageView[] imageViews = Stream.of(item.split(";"))
            .map(e -> ImageTableCell.newImage(e, scrollPane2.widthProperty())).toArray(ImageView[]::new);
        images.getChildren().addAll(imageViews);
    }

    private void changeOptions(ContestQuestion contestQuestion) {
        if (contestQuestion.getOptions() == null && contestQuestion.getType() == QuestionType.TRUE_FALSE) {
            ContestQuestionAnswer e = new ContestQuestionAnswer();
            e.setAnswer("Certo");
            options.getItems().add(e);
            e = new ContestQuestionAnswer();
            e.setAnswer("Errado");
            options.getItems().add(e);
            updateCellFactory();
            return;
        }

        ObservableList<ContestQuestionAnswer> items = options.getItems();
        for (int i = 0; i < contestQuestion.getOptions().size(); i++) {
            if (i < 0 || i >= items.size()) {
                items.add(contestQuestion.getOptions().get(i));
            } else {
                items.set(i, contestQuestion.getOptions().get(i));
            }
        }
        updateCellFactory();
    }

    private void onCurrentChange(Number value) {
        int cur = value.intValue();
        if (cur < 0 || contestQuestions.getListQuestions().isEmpty()) {
            question.setText("");
            text.setText("");
            options.setItems(FXCollections.observableArrayList());
            return;
        }
        questions.getSelectionModel().select(cur);
        ContestQuestion contestQuestion = contestQuestions.getListQuestions().get(cur);
        question.setText(contestQuestion.getExercise());
        changeOptions(contestQuestion);
        images.getChildren().clear();
        getContextTexts(contestQuestions, cur).map(ContestText::getImage).filter(Objects::nonNull)
            .forEach(this::addImages);
        if (contestQuestion.getImage() != null) {
            addImages(contestQuestion.getImage());
        }
        setText(cur);
    }

    private void setText(int cur) {
        String text2 = ContestApplicationController.getText(contestQuestions, cur);
        text.setText(text2);
        double[] dividerPositions = splitPane.getDividerPositions();
        dividerPositions[dividerPositions.length - 1] = text2.isEmpty() ? dividerPositions[dividerPositions.length - 2]
            : 4. / 6;
        splitPane.setDividerPositions(dividerPositions);
    }

    private void updateCellFactory() {
        options.setCellFactory(newCellFactory((el, cell) -> {
            Text text1 = new Text(el != null ? el.getAnswer() : "");
            text1.wrappingWidthProperty().bind(options.widthProperty().add(-10));
            cell.setGraphic(text1);
            cell.getStyleClass().removeAll("", "certo", "errado", CERTO0, ERRADO0);
            cell.getStyleClass().add(mapIf(el, e -> e.getCorrect() ? CERTO0 : ERRADO0, ""));
        }));
        options.getSelectionModel().clearSelection();
    }

    static String getText(ContestDTO contestQuestions2, int cur) {
        List<String> map = getContextTexts(contestQuestions2, cur).map(ContestText::getText)
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

    private static Stream<ContestText> getContextTexts(ContestDTO contestQuestions2, int cur) {
        return contestQuestions2.getContestTexts().stream().filter(t -> ContestApplicationController.isBetween(t, cur));
    }

    private static void setText(ContestDTO item, ListCell<ContestDTO> cell) {
        cell.setText(
            mapIf(item, it -> Objects.toString(it.getContest().getJob(), "") + "\n" + it.getContest().getName()));
    }

}
