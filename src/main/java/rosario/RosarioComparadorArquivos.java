package rosario;

import static rosario.RosarioCommons.*;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;

public class RosarioComparadorArquivos extends Application implements HasLogging {

    static final Logger LOG = HasLogging.log();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Comparação Estoque e ANVISA");
        BorderPane root = new BorderPane();
        // create a grid pane
        FlowPane gridpane = new FlowPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        root.setCenter(gridpane);
        Button exportar = new Button("Exportar Excel");
        TextField filterField = new TextField();
        Label pesquisar = new Label("Pesquisar medicamento");
        gridpane.getChildren().add(new VBox(pesquisar, filterField, exportar));

        final TableView<Medicamento> medicamentosEstoqueTable = tabelaMedicamentos(false);
        Label estoqueRosario = new Label("Estoque Loja");
        GridPane.setHalignment(estoqueRosario, HPos.CENTER);
        FileChooser fileChooserRosario = choseFile("Carregar Arquivo Loja");
        Button buttonEstoque = new Button("Carregar Arquivo Loja");
        buttonEstoque.setOnAction(e -> carregarEstoqueLoja(filterField, medicamentosEstoqueTable, fileChooserRosario));

        VBox e = new VBox(estoqueRosario, buttonEstoque, medicamentosEstoqueTable);
        gridpane.getChildren().add(e);

        Label estoqueSNGPC = new Label("Estoque SNGPC");
        GridPane.setHalignment(estoqueSNGPC, HPos.CENTER);

        FileChooser fileChooserSNGPC = choseFile("Carregar Arquivo SNGPC");
        final TableView<Medicamento> medicamentosEstoqueSNGPCTable = tabelaMedicamentos(true);
        Button buttonEstoqueSNGPC = new Button("Carregar Arquivo SNGPC");
        buttonEstoqueSNGPC.setId("SNGPC");
        buttonEstoqueSNGPC.setOnAction(
            a -> carregarSNGPC(filterField, medicamentosEstoqueTable, fileChooserSNGPC, medicamentosEstoqueSNGPCTable));
        gridpane.getChildren().add(new VBox(estoqueSNGPC, buttonEstoqueSNGPC, medicamentosEstoqueSNGPCTable));

        Label estoqueAnvisa = new Label("Estoque Anvisa");
        GridPane.setHalignment(estoqueAnvisa, HPos.CENTER);
        FileChooser fileChooser2 = choseFile("Carregar Arquivo Anvisa");
        Button button2 = new Button("Carregar Arquivo Anvisa");
        final TableView<Medicamento> medicamentosAnvisaTable = tabelaMedicamentos(true);
        button2.setId("anvisa");
        button2.setOnAction(a -> carregarEstoqueAnvisa(filterField, medicamentosEstoqueSNGPCTable, fileChooser2,
            medicamentosAnvisaTable));
        gridpane.getChildren().add(new VBox(estoqueAnvisa, button2, medicamentosAnvisaTable));
        exportar.setOnAction(a -> exportarMedicamentos(medicamentosEstoqueTable, medicamentosEstoqueSNGPCTable,
            medicamentosAnvisaTable));
        // selection listening
        primaryStage.setScene(new Scene(root, 1000, 500, Color.WHITE));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }



    protected abstract static class CustomableTableCell<T> extends TableCell<Medicamento, T> {

        protected abstract void setStyleable(Medicamento auxMed);

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            int index = getIndex();
            int size = getTableView().getItems().size();
            if (index >= 0 && index < size) {
                Medicamento auxMed = getTableView().getItems().get(index);
                setStyleable(auxMed);
            }
        }
    }

}
