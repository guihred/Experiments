package contest.db;

import java.io.File;
import java.io.IOException;
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
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import pdfreader.HasImage;
import simplebuilder.SimpleTableViewBuilder;
import utils.HasLogging;
import utils.ImageTableCell;
import utils.ResourceFXUtils;

public class ContestApplication extends Application implements HasLogging {


    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Contest Questions");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 600, 250, Color.WHITE);
        // create a grid pane
        HBox gridpane = new HBox();
        gridpane.setPadding(new Insets(5));

        root.setCenter(gridpane);
        String arquivo = "102 - Analista de Tecnologia da Informacao - Tipo D.pdf";
        File file = ResourceFXUtils.toFile(arquivo);
        getLogger().info("File exists={}", file.exists());
        ObservableList<HasImage> observableArrayList = FXCollections.observableArrayList();
        FilteredList<HasImage> li = observableArrayList.filtered(e -> true);
        ObservableList<ContestQuestion> questions = ContestReader.getContestQuestions(file,
                () -> li.setPredicate(e -> e.getImage() != null));
        ObservableList<ContestText> texts = ContestReader.getContestTexts();



        final TableView<ContestQuestion> questionsTable = createContestQuestionsTable(root);
        questionsTable.setItems(questions);

        gridpane.getChildren().add(createVbox("Questions", questionsTable));

        TableView<HasImage> imagesTable = createImagesTable(root);

        ListChangeListener<? super HasImage> listener = c -> {
            while (c.next()) {
                observableArrayList.addAll(
                        c.getAddedSubList());
            }
        };
        questions.addListener(listener);
        texts.addListener(listener);

        imagesTable.setItems(li);

        gridpane.getChildren().add(createVbox("Images", imagesTable));

        TableView<ContestText> textsTable = createTextsTable(root);
        textsTable.setItems(texts);
        gridpane.getChildren().add(createVbox("Texts", textsTable));
        // selection listening
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TableView<ContestQuestion> createContestQuestionsTable(BorderPane root) {
        return new SimpleTableViewBuilder<ContestQuestion>()
                .prefWidth(root.widthProperty().add(-10).divide(3))
                .prefHeight(root.heightProperty().add(-30))
                .scaleShape(false)
                .addColumn("Number", "number")
                .addColumn("Question", "exercise")
                .addColumn("Options", "formattedOptions")
                .addColumn("Subject", "subject")
                .equalColumns()
                .build();
    }

    private TableView<HasImage> createImagesTable(BorderPane root) {

        return new SimpleTableViewBuilder<HasImage>()
                .prefWidth(root.widthProperty().add(-10).divide(3))
                .prefHeight(root.heightProperty().add(-30))
                .scaleShape(false)
                .addColumn("Image", "image",s -> new ImageTableCell())
                .equalColumns()
                .build();
    }

    private TableView<ContestText> createTextsTable(BorderPane root) {
        return new SimpleTableViewBuilder<ContestText>()
                .prefWidth(root.widthProperty().add(-10).divide(3))
                .prefHeight(root.heightProperty().add(-30))
                .scaleShape(false)
                .addColumn("Text", "text")
                .equalColumns()
                .build();
    }

    private VBox createVbox(String text, final Node medicamentosEstoqueTable) {
        Label estoqueRosario = new Label(text);
        GridPane.setHalignment(estoqueRosario, HPos.CENTER);
        return new VBox(estoqueRosario, medicamentosEstoqueTable);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
