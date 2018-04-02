package contest.db;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ContestApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Contest Questions");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 600, 250, Color.WHITE);
        // create a grid pane
        FlowPane gridpane = new FlowPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        root.setCenter(gridpane);
        File file = new File("102 - Analista de Tecnologia da Informacao - Tipo D.pdf");
        ObservableList<ContestQuestion> medicamentosSNGPCPDF = ContestReader.getContestQuestions(file);

        final TableView<ContestQuestion> medicamentosEstoqueTable = tabelaContestQuestions(root);
        medicamentosEstoqueTable.setItems(medicamentosSNGPCPDF);
        Label estoqueRosario = new Label("Questions");
        GridPane.setHalignment(estoqueRosario, HPos.CENTER);

        VBox e = new VBox(estoqueRosario, medicamentosEstoqueTable);
        gridpane.getChildren().add(e);

        // selection listening
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TableView<ContestQuestion> tabelaContestQuestions(BorderPane root) {

        final TableView<ContestQuestion> medicamentosTable = new TableView<>();
        medicamentosTable.setPrefWidth(300);
        medicamentosTable.setScaleShape(false);
        medicamentosTable.prefWidthProperty().bind(root.widthProperty().add(-10));
        medicamentosTable.prefHeightProperty().bind(root.heightProperty().add(-30));

        TableColumn<ContestQuestion, String> loteContestQuestion = new TableColumn<>("Number");
        loteContestQuestion.setSortable(true);
        loteContestQuestion.setCellValueFactory(new PropertyValueFactory<>("number"));
        loteContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 6);
        medicamentosTable.getColumns().add(loteContestQuestion);

        TableColumn<ContestQuestion, String> registroContestQuestion = new TableColumn<>("Question");
        registroContestQuestion.setCellValueFactory(new PropertyValueFactory<>("exercise"));
        registroContestQuestion.setSortable(true);
        registroContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 3);
        medicamentosTable.getColumns().add(registroContestQuestion);

        TableColumn<ContestQuestion, String> quantidadeContestQuestion = new TableColumn<>("Options");
        quantidadeContestQuestion.setSortable(true);
        quantidadeContestQuestion.setCellValueFactory(new PropertyValueFactory<>("formattedOptions"));
        quantidadeContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 3);
        medicamentosTable.getColumns().add(quantidadeContestQuestion);

        TableColumn<ContestQuestion, String> nomeContestQuestion = new TableColumn<>("Subject");
        nomeContestQuestion.setSortable(true);
        nomeContestQuestion.setCellValueFactory(new PropertyValueFactory<>("subject"));
        nomeContestQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 6);
        medicamentosTable.getColumns().add(nomeContestQuestion);

        TableColumn<ContestQuestion, String> imageQuestion = new TableColumn<>("Image");
        imageQuestion.setSortable(true);
        imageQuestion.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageQuestion.setCellFactory(s -> new TableCell<ContestQuestion, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item == getItem()) {
                            return;
                        }
                        super.updateItem(item, empty);
                        if (item == null) {
                            super.setText(null);
                            super.setGraphic(null);
                        } else {
                            super.setGraphic(new VBox(Stream.of(item.split(";")).map(i -> new ImageView("file:" + i))
                                    .toArray(ImageView[]::new)));
                        }
                    }
                });
        imageQuestion.setPrefWidth(medicamentosTable.getPrefWidth() / 6);
        medicamentosTable.getColumns().add(imageQuestion);

        return medicamentosTable;
    }

}
