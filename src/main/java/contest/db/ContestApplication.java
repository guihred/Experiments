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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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

        final TableView<ContestQuestion> medicamentosTable = new TableView<>();
        medicamentosTable.setPrefWidth(300);
        medicamentosTable.setScaleShape(false);
        medicamentosTable.prefWidthProperty().bind(root.widthProperty().add(-10).divide(3));
        medicamentosTable.prefHeightProperty().bind(root.heightProperty().add(-30));

        TableColumn<ContestQuestion, String> loteContestQuestion = new TableColumn<>("Number");
        loteContestQuestion.setSortable(true);
        loteContestQuestion.setCellValueFactory(new PropertyValueFactory<>("number"));
        loteContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
        medicamentosTable.getColumns().add(loteContestQuestion);

        TableColumn<ContestQuestion, String> registroContestQuestion = new TableColumn<>("Question");
        registroContestQuestion.setCellValueFactory(new PropertyValueFactory<>("exercise"));
        registroContestQuestion.setSortable(true);
        registroContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
        medicamentosTable.getColumns().add(registroContestQuestion);

        TableColumn<ContestQuestion, String> quantidadeContestQuestion = new TableColumn<>("Options");
        quantidadeContestQuestion.setSortable(true);
        quantidadeContestQuestion.setCellValueFactory(new PropertyValueFactory<>("formattedOptions"));
        quantidadeContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
        medicamentosTable.getColumns().add(quantidadeContestQuestion);

        TableColumn<ContestQuestion, String> nomeContestQuestion = new TableColumn<>("Subject");
        nomeContestQuestion.setSortable(true);
        nomeContestQuestion.setCellValueFactory(new PropertyValueFactory<>("subject"));
        nomeContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
        medicamentosTable.getColumns().add(nomeContestQuestion);

        return medicamentosTable;
    }

    private TableView<HasImage> createImagesTable(BorderPane root) {

        final TableView<HasImage> medicamentosTable = new TableView<>();
        medicamentosTable.setPrefWidth(300);
        medicamentosTable.setScaleShape(false);
        medicamentosTable.prefWidthProperty().bind(root.widthProperty().add(-10).divide(3));
        medicamentosTable.prefHeightProperty().bind(root.heightProperty().add(-30));

        TableColumn<HasImage, String> imageQuestion = new TableColumn<>("Image");
        imageQuestion.setSortable(true);
        imageQuestion.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageQuestion.setCellFactory(s -> new ImageTableCell());
        imageQuestion.prefWidthProperty().bind(medicamentosTable.prefWidthProperty());
        medicamentosTable.getColumns().add(imageQuestion);

        return medicamentosTable;
    }

    private TableView<ContestText> createTextsTable(BorderPane root) {

        final TableView<ContestText> medicamentosTable = new TableView<>();
        medicamentosTable.setPrefWidth(300);
        medicamentosTable.setScaleShape(false);
        medicamentosTable.prefWidthProperty().bind(root.widthProperty().add(-10).divide(3));
        medicamentosTable.prefHeightProperty().bind(root.heightProperty().add(-30));

        TableColumn<ContestText, String> nomeContestQuestion = new TableColumn<>("Text");
        nomeContestQuestion.setSortable(true);
        nomeContestQuestion.setCellValueFactory(new PropertyValueFactory<>("text"));
        nomeContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth());
        medicamentosTable.getColumns().add(nomeContestQuestion);

        return medicamentosTable;
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
