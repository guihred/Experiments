package japstudy;


import static utils.CommonsFX.onCloseWindow;
import static utils.RunnableEx.runIf;

import java.util.Random;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.CommonsFX;
import utils.HibernateUtil;

public class JapaneseLessonDisplay extends Application {
    @FXML
    private Text english;
    @FXML
    private Text japanese;
    @FXML
    private CheckBox showRomaji;
    @FXML
    private Text romaji;
    @FXML
    private Button next;
    @FXML
    private Text lesson;
    @FXML
    private TextField answer;
    @FXML
    private Text scoreText;
    @FXML
    private Text japaneseText;
    @FXML
    private Text romajiText;
    private Random random = new Random();
    private IntegerProperty current = new SimpleIntegerProperty(1);
    private ObservableList<JapaneseLesson> lessons = JapaneseLessonReader.getLessonsWait();
    private DoubleProperty score = new SimpleDoubleProperty(1);
    private BooleanProperty tested = new SimpleBooleanProperty(false);

    public void initialize() {
        current.addListener((o, old, val) -> runIf(val, v -> {
            lesson.setText("Lesson: " + lessons.get(v.intValue()).getLesson());
            english.setText(lessons.get(v.intValue()).getEnglish());
            romaji.setText(lessons.get(v.intValue()).getRomaji());
            japanese.setText(lessons.get(v.intValue()).getJapanese());
        }));
        japanese.visibleProperty().bind(tested);
        if (!lessons.isEmpty()) {
            current.set(random.nextInt(lessons.size()));
        }
        scoreText.textProperty().bind(score.multiply(100).asString(" Score: %.02f%%"));
        japaneseText.visibleProperty().bind(tested);
        romajiText.visibleProperty().bind(showRomaji.selectedProperty());
        romaji.visibleProperty().bind(showRomaji.selectedProperty());
    }

    public void onActionNext() {
        if (!tested.get()) {
            tested.set(true);
            String text = answer.getText();
            String japanese2 = lessons.get(current.get()).getJapanese();
            double compare = CompareAnswers.compare(japanese2, text);
            score.set((score.get() + compare) / 2);
        } else {
            current.set((current.get() + 1) % lessons.size());
            tested.set(false);
            answer.setText("");
            showRomaji.setSelected(false);
        }
    }

    @Override
	public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Japanese Lesson Display", "JapaneseLessonDisplay.fxml", this, primaryStage);
        onCloseWindow(primaryStage, HibernateUtil::shutdown);
        primaryStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                onActionNext();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
