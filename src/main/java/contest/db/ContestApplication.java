package contest.db;

import static simplebuilder.SimpleListViewBuilder.newCellFactory;

import japstudy.db.HibernateUtil;
import java.util.List;
import java.util.Objects;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;
import utils.HasImage;
import utils.HasLogging;
import utils.ImageTableCell;
import utils.ResourceFXUtils;

public class ContestApplication extends Application implements HasLogging {

    private ContestReader contestQuestions;
    private ObservableList<ContestReader> allContests;

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
        HBox root = new HBox(20);
        root.setPrefWidth(1000);
        root.setPrefHeight(500);
        Scene scene = new Scene(root, Color.WHITE);
        // create a grid pane
        root.setPadding(new Insets(5));

        ObservableList<HasImage> observableArrayList = FXCollections.observableArrayList();
        FilteredList<HasImage> li = observableArrayList.filtered(e -> e != null && e.getImage() != null);
        ObservableList<ContestQuestion> questions = contestQuestions.getListQuestions();
        ObservableList<ContestText> texts = contestQuestions.getContestTexts();

        final TableView<ContestQuestion> questionsTable = createContestQuestionsTable(root);
        questionsTable.setItems(questions);

        root.getChildren().add(createVbox("Questions", questionsTable));

        TableView<HasImage> imagesTable = createImagesTable(root);

        ListChangeListener<? super HasImage> listener = c -> {
            while (c.next()) {
                List<? extends HasImage> addedSubList = c.getAddedSubList();
                if (addedSubList != null) {
                    observableArrayList.addAll(addedSubList);
                }
            }
        };
        questions.addListener(listener);
        texts.addListener(listener);

        imagesTable.setItems(li);

        root.getChildren().add(createVbox("Images", imagesTable));
        if (allContests != null) {
            ListView<ContestReader> e = new ListView<>(allContests);
            e.setCellFactory(newCellFactory((item, cell) -> cell
                .setText(Objects.toString(item.getContest().getJob(), "") + "\n" + item.getContest().getName())));
            root.getChildren().add(0, e);
        }

        TableView<ContestText> textsTable = createTextsTable(root);
        textsTable.setItems(texts);
        root.getChildren().add(createVbox("Texts", textsTable));
        // selection listening
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static TableView<ContestQuestion> createContestQuestionsTable(Region root) {
        return new SimpleTableViewBuilder<ContestQuestion>().prefWidth(root.widthProperty().add(-10).divide(3))
            .prefHeight(root.heightProperty().add(-30)).scaleShape(false).addColumn("Number", "number")
            .addColumn("Question", "exercise").addColumn("Options", "formattedOptions").addColumn("Subject", "subject")
            .equalColumns().build();
    }

    private static TableView<HasImage> createImagesTable(Region root) {

        return new SimpleTableViewBuilder<HasImage>().prefWidth(root.widthProperty().add(-10).divide(3))
            .prefHeight(root.heightProperty().add(-30)).scaleShape(false)
            .addColumn("Image", "image", s -> new ImageTableCell<>()).equalColumns().build();
    }

    private static TableView<ContestText> createTextsTable(Region root) {
        return new SimpleTableViewBuilder<ContestText>().prefWidth(root.widthProperty().add(-10).divide(3))
            .prefHeight(root.heightProperty().add(-30)).scaleShape(false).addColumn("Text", "text").equalColumns()
            .build();
    }

    private static VBox createVbox(String text, final Node medicamentosEstoqueTable) {
        Label estoqueRosario = new Label(text);
        GridPane.setHalignment(estoqueRosario, HPos.CENTER);
        return new VBox(estoqueRosario, medicamentosEstoqueTable);
    }
}
