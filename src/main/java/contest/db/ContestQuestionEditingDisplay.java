package contest.db;

import static simplebuilder.SimpleVBoxBuilder.newVBox;
import static utils.ResourceFXUtils.convertToURL;
import static utils.StringSigaUtils.intValue;

import japstudy.db.HibernateUtil;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ChoiceBoxListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleConverter;
import utils.CommonsFX;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class ContestQuestionEditingDisplay extends Application {
    private static final Logger LOG = HasLogging.log();
    private IntegerProperty current = new SimpleIntegerProperty(-1);
    private final ObservableList<ContestQuestion> lessons;
    private final ContestReader instance;
    private ObservableList<ContestQuestionAnswer> options = FXCollections.observableArrayList();

    public ContestQuestionEditingDisplay() {
        instance = IadesHelper.getContestQuestions(
            ResourceFXUtils.toFile("102 - Analista de Tecnologia da Informacao - Tipo D.pdf"), () -> current.set(0));
        lessons = instance.getListQuestions();
    }

    public ContestQuestionEditingDisplay(ContestReader instance) {
        this.instance = instance;
        lessons = instance.getListQuestions();
    }

    public IntegerProperty currentProperty() {
        return current;
    }

    public void setCurrent(ContestQuestion selectedItem) {
        current.set(lessons.indexOf(selectedItem));
    }

    @Override
    public void start(Stage primaryStage) {

        TextField number = new TextField();
        TextArea question = new TextArea();
        ListView<ContestQuestionAnswer> optionsListView = newOptionListView();
        TextField subject = new TextField();
        TextField image = new TextField();
        VBox newImage = new VBox();
        current.addListener((observable, oldValue, newValue) -> {
            if (newValue != null && lessons.size() > newValue.intValue() && newValue.intValue() >= 0) {
                ContestQuestion contestQuestion = lessons.get(newValue.intValue());
                number.setText(Objects.toString(contestQuestion.getNumber()));
                List<ContestQuestionAnswer> options2 = contestQuestion.getOptions();
                if (options2 != null) {
                    options.setAll(options2);
                }
                question.setText(contestQuestion.getExercise());

                subject.setText(contestQuestion.getSubject());
                image.setText(contestQuestion.getImage());
                setImage(newImage, contestQuestion.getImage());
            }
        });
        image.textProperty().addListener((o, v, n) -> {
            if (n != null && StringUtils.isNotBlank(n)) {
                setImage(newImage, n);
            }
        });
        question.textProperty().addListener((o, old, newV) -> setTextField(newV, ContestQuestion::setExercise));
        number.textProperty().addListener((o, old, newV) -> setNumberField(newV, ContestQuestion::setNumber));

        subject.textProperty().addListener((o, old, newV) -> setTextField(newV, ContestQuestion::setSubject));
        image.textProperty().addListener((o, old, newV) -> setTextField(newV, ContestQuestion::setImage));
        Button previous = CommonsFX.newButton("P_revious", e -> previousLesson());
        previous.disableProperty().bind(current.lessThanOrEqualTo(0));
        Button save = CommonsFX.newButton("_Save and Close", e -> saveAndClose(primaryStage));
        save.disableProperty().bind(current.lessThan(0));
        Button next = CommonsFX.newButton("_Next", e -> nextLesson());
        next.disableProperty()
            .bind(current.greaterThanOrEqualTo(Bindings.createIntegerBinding(() -> lessons.size() - 1, lessons))
                .or(current.lessThan(0)));

        final int width = 600;
        primaryStage.setWidth(width);

        primaryStage.centerOnScreen();
        Scene value = new Scene(new HBox(
            new VBox(newVBox("Number", number), newVBox("Subject", subject), newVBox("Questions", question),
                newVBox("Options", optionsListView), new HBox(previous, next, save)),
            newVBox("Image", image, newImage)));
        primaryStage.setScene(value);
        value.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                nextLesson();
            }
        });
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());

        primaryStage.show();
        current.set(0);
    }

    private ListView<ContestQuestionAnswer> newOptionListView() {
        ListView<ContestQuestionAnswer> listView = new ListView<>();
        listView.setItems(options);
        listView.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        listView.setCellFactory(
            ChoiceBoxListCell.forListView(new SimpleConverter<ContestQuestionAnswer>("answer"), options));

        listView.selectionModelProperty().get().selectedItemProperty().addListener((o, v, n) -> {
            if (n != null) {
                options.forEach(e -> e.setCorrect(false));
                n.setCorrect(true);
            }
        });
        options.addListener((ListChangeListener<ContestQuestionAnswer>) c -> {
            c.next();
            ContestQuestionAnswer orElse = c.getAddedSubList().stream().filter(ContestQuestionAnswer::getCorrect)
                .findAny().orElse(null);
            listView.selectionModelProperty().get().select(orElse);
        });
        return listView;
    }

    private void nextLesson() {
        current.set((current.get() + 1) % lessons.size());
    }

    private void previousLesson() {
        current.set((lessons.size() + current.get() - 1) % lessons.size());
    }

    private void saveAndClose(Stage primaryStage) {
        int index = current.get();
        ContestQuestion contestQuestion = lessons.get(index);
        lessons.set(index, contestQuestion);
        if (LOG.isInfoEnabled()) {
            LOG.trace(instance.getContest().toSQL());
            LOG.trace(lessons.stream().map(ContestQuestion::toSQL).collect(Collectors.joining("\n")));
            LOG.trace(lessons.stream().flatMap(e -> e.getOptions().stream()).map(ContestQuestionAnswer::toSQL)
                .collect(Collectors.joining("\n")));
            LOG.trace(instance.getContestTexts().stream().filter(e -> StringUtils.isNotBlank(e.getText()))
                .map(ContestText::toSQL).collect(Collectors.joining("\n")));
        }
        instance.saveAll();

        primaryStage.close();
    }

    private void setNumberField(String newV, ObjIntConsumer<ContestQuestion> a) {
        if (newV != null) {
            ContestQuestion contestQuestion = lessons.get(current.intValue());
            a.accept(contestQuestion, intValue(newV));
        }
    }

    private void setTextField(String newV, BiConsumer<ContestQuestion, String> a) {
        if (newV != null) {
            ContestQuestion contestQuestion = lessons.get(current.intValue());
            a.accept(contestQuestion, newV);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void setImage(VBox newImage, String image) {
        if (image != null) {
            List<ImageView> images = Stream.of(image.split(";"))
                .map(i -> new ImageView(convertToURL(new File(i)).toString()))
                .peek(e -> e.prefWidth(newImage.getWidth())).collect(Collectors.toList());
            newImage.getChildren().setAll(images);
        } else if (!newImage.getChildren().isEmpty()) {
            newImage.getChildren().clear();

        }
    }

}
