package contest.db;

import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static utils.ResourceFXUtils.toExternalForm;

import japstudy.db.HibernateUtil;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleListViewBuilder;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ContestApplication extends Application implements HasLogging {

    private ContestReader contestQuestions;
    private ObservableList<ContestReader> allContests;
    private IntegerProperty current = new SimpleIntegerProperty(-1);

    public ContestApplication() {
        allContests = ContestReader.getAllContests();
        if (allContests.isEmpty()) {
            contestQuestions = ContestReader.getContestQuestions(
                ResourceFXUtils.toFile("102 - Analista de Tecnologia da Informacao - Tipo D.pdf"), reader -> {
                    reader.getContest().setJob("Analista de Tecnologia da Informacao");
                    reader.getContest().setName("CFM Conselho Federal de Medicina");
                    reader.saveAll();
                    getLogger().info("Questions Read");
                });
            allContests.add(contestQuestions);

            return;
        }
        contestQuestions = allContests.get(0);

    }

    public ContestApplication(ContestReader contestQuestions) {
        this.contestQuestions = contestQuestions;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Contest Questions");
        SplitPane root = new SplitPane();
        Scene scene = new Scene(root, Color.WHITE);
        // create a grid pane

        if (allContests != null) {
            ListView<ContestReader> listView = new SimpleListViewBuilder<ContestReader>().items(allContests)
                .cellFactory((item, cell) -> cell
                    .setText(Objects.toString(item.getContest().getJob(), "") + "\n" + item.getContest().getName()))
                .onDoubleClick(c -> {
                    contestQuestions = c;
                    current.set(1);
                    current.set(0);
                }).build();

            root.getItems().add(listView);
        }
        Text text = new Text();
        text.setTextAlignment(TextAlignment.LEFT);
        text.setWrappingWidth(500);
        text.visibleProperty().bind(text.textProperty().isEmpty().not());

        Text question = new Text();

        VBox hBox = new VBox();
        ScrollPane scrollPane = new ScrollPane(hBox);
        ListView<ContestQuestionAnswer> options = new SimpleListViewBuilder<ContestQuestionAnswer>()
            .items(FXCollections.observableArrayList())
            .onSelect((old, value) -> {
                if (value == null) {
                    return;
                }
                Boolean correct = value.getCorrect();
                ObservableList<ContestQuestion> contestTexts = contestQuestions.getListQuestions();
                Integer number = contestTexts.get(current.get()).getNumber();
                getLogger().info("Question {} Answer {}", number, correct);
                root.lookupAll(".cell").stream().map(Node::getStyleClass).forEach(e -> {
                    if (e.contains("certo0")) {
                        e.add("certo");
                    } else if (e.contains("errado0")) {
                        e.add("errado");
                    }
                });

            }).cellFactory((el, cell) -> {
                Text text1 = new Text(el.getAnswer());
                text1.wrappingWidthProperty().bind(hBox.widthProperty().add(-10));
                cell.setGraphic(text1);
                cell.getStyleClass().removeAll("certo", "errado", "certo0", "errado0");
                cell.getStyleClass().add(el.getCorrect() ? "certo0" : "errado0");
            }).prefWidth(200).build();
        current.addListener((ob, old, value) -> {
            int cur = value.intValue();
            text.setText(getText(cur));
            ContestQuestion contestQuestion = contestQuestions.getListQuestions().get(cur);
            question.setText(contestQuestion.getExercise());
            extracted(options, contestQuestion);
        });
        Button newButton = CommonsFX.newButton("Next",
            e -> current.set((current.get() + 1) % contestQuestions.getListQuestions().size()));
        hBox.getChildren().addAll(question, newButton, options);

        root.getItems().add(new ScrollPane(text));
        root.getItems().add(scrollPane);
        if (root.getItems().size() == 3) {
            root.setDividerPositions(1. / 5, 3. / 5);
        }
        // selection listening
        scene.getStylesheets().add(toExternalForm("filesComparator.css"));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
        current.set(0);
    }

    private String getText(int cur) {
        List<String> map = contestQuestions.getContestTexts().stream().filter(t -> isBetween(t, cur))
            .map(ContestText::getText).filter(StringUtils::isNotBlank).flatMap(s -> Stream.of(s.split("\n")))
            .map(e -> e.trim())
            .collect(Collectors.toList());

        int orElse = map.stream().mapToInt(String::length).max().orElse(0);

        return IntStream.range(0, map.size())
            .mapToObj(i -> mapLines(map, orElse, i))
            .collect(Collectors.joining("\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static String addSpaces(String str, int diff) {

        Pattern compile = Pattern.compile(" +");
        Matcher matcher = compile.matcher(str);
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < diff; j++) {
            if (!matcher.find()) {
                matcher.appendTail(sb);
                matcher = compile.matcher(sb.toString().trim());
                sb.delete(0, sb.length());
                j--;
            } else {
                String group = matcher.group(0);
                matcher.appendReplacement(sb, group + " ");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static void extracted(ListView<ContestQuestionAnswer> options, ContestQuestion contestQuestion) {
        ObservableList<ContestQuestionAnswer> items = options.getItems();
        for (int i = 0; i < contestQuestion.getOptions().size(); i++) {
            if (i < 0 || i >= items.size()) {
                items.add(contestQuestion.getOptions().get(i));
            } else {
                items.set(i, contestQuestion.getOptions().get(i));
            }
        }
    }

    private static boolean isBetween(ContestText tex, int j) {
        int i = j + 1;
        return tex.getMin() <= i && tex.getMax() >= i;
    }

    private static String justified(List<String> map, int maxLetters, int i) {
        String str = map.get(i);
        int diff = maxLetters - str.length();
        if (i == 0 || paragraphEnd(map.get(i - 1))) {
            return leftPad(addSpaces(str, Math.max(diff - 8, 0)), maxLetters, "");
        }
        if (diff >= maxLetters / 2 || paragraphEnd(str)) {
            return rightPad(str, maxLetters, "");
        }
        String sb = addSpaces(str, diff);
        return rightPad(sb, maxLetters, "");
    }

    private static String mapLines(List<String> map, int orElse, int i) {

        String object = justified(map, orElse, i);
        return String.format("(%02d)    %s", i + 1, object);
    }

    private static boolean paragraphEnd(String str) {
        return str.endsWith(".") || str.endsWith(".”")||str.matches("Texto \\d+");
    }
}
