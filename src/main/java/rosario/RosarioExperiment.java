package rosario;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import schema.sngpc.FXMLCreatorHelper;

public class RosarioExperiment {
    @FXML
    private TextField searchText;
    @FXML
    private TableView<Medicamento> medicamentosEstoqueTable;
    @FXML
    private TableView<Medicamento> medicamentosEstoqueSNGPCTable;
    @FXML
    private TableView<Medicamento> medicamentosAnvisaTable;

    public void carregarEstoqueAnvisa(){
        FileChooser fileChooserRosario = RosarioCommons.choseFile("Carregar Arquivo Anvisa");
        RosarioCommons.carregarEstoqueAnvisa(searchText, medicamentosEstoqueSNGPCTable, fileChooserRosario,
            medicamentosAnvisaTable);
    }

    public void carregarEstoqueLoja(){
        FileChooser fileChooserRosario = RosarioCommons.choseFile("Carregar Arquivo Loja");
        RosarioCommons.carregarEstoqueLoja(searchText, medicamentosEstoqueTable, fileChooserRosario);
    }

    public void carregarSNGPC(){
        FileChooser choseFile = RosarioCommons.choseFile("Carregar Arquivo SNGPC");
        RosarioCommons.carregarSNGPC(searchText, medicamentosEstoqueTable, choseFile,
        medicamentosEstoqueSNGPCTable);
    }

    public void exportar() {
        RosarioCommons.exportarMedicamentos(medicamentosEstoqueTable, medicamentosEstoqueSNGPCTable,
            medicamentosAnvisaTable);
    }

    public static void main(String[] args) {
        FXMLCreatorHelper.duplicate("RosarioComparadorArquivos.fxml");
    }

}
