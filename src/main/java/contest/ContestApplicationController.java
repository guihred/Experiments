package contest;

import static contest.ContestHelper.*;
import static java.lang.String.format;
import static simplebuilder.SimpleListViewBuilder.newCellFactory;
import static utils.ex.FunctionEx.mapIf;

import contest.db.ContestQuestion;
import contest.db.ContestQuestionAnswer;
import contest.db.ContestText;
import contest.db.ContestQuestionType;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import utils.fx.ImageTableCell;

public class ContestApplicationController {
    private static final String ERRADO = "errado";
    private static final String CERTO = "certo";
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
    private final ObservableMap<Integer, String> answersCorrect = FXCollections.observableHashMap();
    private final ObservableMap<ListCell<ContestQuestion>, Integer> cellMap = FXCollections.observableHashMap();
    @FXML
    private IntegerProperty current;

    public void initialize() {
        current.set(-1);
        ObservableList<ContestDTO> allContests2 = getAllContests();
        allContests.setCellFactory(newCellFactory((t, u) -> u.setText(formatDTO(t))));
        allContests.setItems(allContests2);
        if (!allContests2.isEmpty()) {
            contestQuestions = allContests2.get(0);
            questions.setItems(contestQuestions.getListQuestions());
            splitPane.setDividerPositions(1. / 6, 2. / 6, 4. / 6);
            fixContests(allContests2);
        } else {
            splitPane.getItems().remove(0);
        }

        questions.getSelectionModel().selectedIndexProperty().addListener((o, old, n) -> current.set(n.intValue()));
        answersCorrect.addListener((MapChangeListener<Integer, String>) change -> cellMap.entrySet().stream()
            .filter(e -> Objects.equals(e.getValue(), change.getKey())).findFirst()
            .ifPresent(cel -> {
                cel.getKey().getStyleClass().removeAll("", CERTO, ERRADO, CERTO0, ERRADO0);
                if (change.wasAdded() && cel.getKey().getItem().getNumber() == cel.getValue()) {
                    cel.getKey().getStyleClass().add(change.getValueAdded());
                }
            }));

        questions.setCellFactory(newCellFactory((ContestQuestion c, ListCell<ContestQuestion> v) -> {
            v.setText(mapIf(c, c0 -> format("%s nº%d", c0.getSubject(), c0.getNumber())));
            v.getStyleClass().removeAll("", CERTO, ERRADO, CERTO0, ERRADO0);
            if (c != null) {
                cellMap.put(v, c.getNumber());
                v.getStyleClass().addAll(answersCorrect.getOrDefault(c.getNumber(), ""));
            } else {
                cellMap.remove(v);
            }
        }));
        options.getSelectionModel().selectedItemProperty().addListener((observable, old, value) -> {
            if (value == null) {
                return;
            }
            answersCorrect.put(value.getExercise().getNumber(), value.getCorrect() ? CERTO : ERRADO);
            questions.setItems(contestQuestions.getListQuestions());
            splitPane.lookupAll(".cell").stream().map(Node::getStyleClass).forEach(e -> {
                if (e.contains(CERTO0)) {
                    e.add(CERTO);
                }
                if (e.contains(ERRADO0)) {
                    e.add(ERRADO);
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
            answersCorrect.clear();
            questions.setItems(c.getListQuestions());
            current.set(0);
        }
    }

    public void setContestQuestions(ContestDTO contestQuestions) {
        this.contestQuestions = contestQuestions;
        current.set(-1);
        questions.setItems(contestQuestions.getListQuestions());
        current.set(0);
    }

    private void addImages(String item) {
        images.getChildren().addAll(ImageTableCell.createImages(item, scrollPane2.widthProperty()));
    }

    private void changeOptions(ContestQuestion contestQuestion) {
        if (contestQuestion.getOptions() == null && contestQuestion.getType() == ContestQuestionType.TRUE_FALSE) {
            ContestQuestionAnswer e = new ContestQuestionAnswer();
            e.setAnswer("Certo");
            options.getItems().add(e);
            e = new ContestQuestionAnswer();
            e.setAnswer("Errado");
            options.getItems().add(e);
            updateCellFactory();
            return;
        }
        options.getSelectionModel().clearSelection();
        List<ContestQuestionAnswer> items = options.getItems();
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
        getContestTexts(contestQuestions.getContestTexts(), cur).map(ContestText::getImage).filter(Objects::nonNull)
            .forEach(this::addImages);
        if (contestQuestion.getImage() != null) {
            addImages(contestQuestion.getImage());
        }
        setText(cur);
    }

    private void setText(int cur) {
        String text2 = getText(contestQuestions, cur);
        text.setText(text2);
        double[] dividerPositions = splitPane.getDividerPositions();
        dividerPositions[dividerPositions.length - 1] = text2.isEmpty() && images.getChildren().isEmpty()
            ? dividerPositions[dividerPositions.length - 2]
            : 4. / 6;
        splitPane.setDividerPositions(dividerPositions);
    }

    private void updateCellFactory() {
        options.getSelectionModel().clearSelection();
        options.setCellFactory(newCellFactory((el, cell) -> {
            Text text1 = new Text(el != null ? el.getAnswer() : "");
            text1.wrappingWidthProperty().bind(options.widthProperty().add(-10));
            cell.setGraphic(text1);
            cell.getStyleClass().removeAll("", CERTO, ERRADO, CERTO0, ERRADO0);
            cell.getStyleClass().add(mapIf(el, e -> e.getCorrect() ? CERTO0 : ERRADO0, ""));
        }));
    }



}
