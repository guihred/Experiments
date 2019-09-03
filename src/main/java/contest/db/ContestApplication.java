package contest.db;

import japstudy.db.HibernateUtil;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import simplebuilder.SimpleListViewBuilder;
import utils.ClassReflectionUtils;
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
            ListView<ContestReader> e = new SimpleListViewBuilder<ContestReader>().items(allContests)
                .cellFactory((item, cell) -> cell
                    .setText(Objects.toString(item.getContest().getJob(), "") + "\n" + item.getContest().getName()))
                .onDoubleClick(c -> {
                    contestQuestions = c;
                    current.set(1);
                    current.set(0);
                }).build();

            root.getItems().add(0, e);
        }
        Text text = new Text();
        text.setTextAlignment(TextAlignment.JUSTIFY);
        text.setWrappingWidth(500);
        Text question = new Text();

        HBox hBox = new HBox();
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
//                current.set((current.get() + 1) % contestTexts.size());
            }).cellFactory((el, cell) -> {
                Text text1 = new Text(el.getAnswer());
                text1.wrappingWidthProperty().bind(hBox.widthProperty().add(-10));
                cell.setGraphic(text1);
                cell.getStyleClass().add(el.getCorrect() ? "amarelo" : "vermelho");
            }).prefWidth(200).build();
        hBox.getChildren().addAll(new VBox(question, options));
        current.addListener((ob, old, value) -> {
            int cur = value.intValue();
            String collect = contestQuestions.getContestTexts().stream().filter(t -> isBetween(t, cur))
                .map(ContestText::getText).collect(Collectors.joining("\n"));
            text.setText(collect);
            ContestQuestion contestQuestion = contestQuestions.getListQuestions().get(cur);
            question.setText(contestQuestion.getExercise());
            extracted(options, contestQuestion);
        });

        root.getItems().add(new ScrollPane(text));
        root.getItems().add(scrollPane);
        // selection listening
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("filesComparator.css"));
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
        String displayStyleClass = ClassReflectionUtils.displayStyleClass(scene.getRoot());

        current.set(0);
        getLogger().info(displayStyleClass);
    }

    public static void main(String[] args) {
        launch(args);
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
}
