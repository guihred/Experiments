package rosario;

import static extract.ExcelService.isExcel;
import static rosario.LeitorArquivos.*;
import static simplebuilder.SimpleTableViewBuilder.newCellFactory;
import static utils.RunnableEx.runIf;

import java.awt.Desktop;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.SimpleButtonBuilder;
import utils.RunnableEx;
import utils.SupplierEx;

public final class RosarioCommons {
    private static final String FX_BACKGROUND_COLOR_LIGHTCORAL = "-fx-background-color:lightcoral";
    private static boolean openAtExport = true;
    private static final Map<String, FileChooser> FILE_CHOOSE = new HashMap<>();

    private RosarioCommons() {
    }

    public static void carregarEstoqueAnvisa(TextField filterField, TableView<Medicamento> sNGPCTable,
        FileChooser fileChooser2, TableView<Medicamento> anvisaTable) {
        File chosenFile = fileChooser2.showOpenDialog(filterField.getScene().getWindow());
        runIf(chosenFile, selectedFile -> {
            if (isExcel(selectedFile)) {
                showImportDialog(selectedFile, FXCollections.observableArrayList(REGISTRO, NOME, LOTE, QUANTIDADE, ""),
                    meds -> {
                        configurarFiltroRapido(filterField, anvisaTable, meds);
                        atualizar(sNGPCTable.getItems(), anvisaTable);
                        atualizar(anvisaTable.getItems(), sNGPCTable);
                    });
                return;
            }
            ObservableList<Medicamento> medicamentosAnvisa = getMedicamentosAnvisa(selectedFile);
            configurarFiltroRapido(filterField, anvisaTable, medicamentosAnvisa);
            atualizar(sNGPCTable.getItems(), anvisaTable);
            atualizar(anvisaTable.getItems(), sNGPCTable);
        });
    }

    public static void carregarEstoqueLoja(TextField filterField, TableView<Medicamento> estoqueTable,
        FileChooser fileChooserRosario) {
        File chosenFile = fileChooserRosario.showOpenDialog(filterField.getScene().getWindow());
        runIf(chosenFile, selectedFile -> {
            if (isExcel(selectedFile)) {
                showImportDialog(selectedFile, FXCollections.observableArrayList(CODIGO, NOME, QUANTIDADE, ""),
                    meds -> configurarFiltroRapido(filterField, estoqueTable, meds));
                return;
            }
            ObservableList<Medicamento> medicamentosRosario = getMedicamentosRosario(selectedFile);
            configurarFiltroRapido(filterField, estoqueTable, medicamentosRosario);
        });
    }

    public static void carregarSNGPC(TextField filter, TableView<Medicamento> estoqueTable, FileChooser chooser,
        TableView<Medicamento> sNGPCTable) {
        File chosenFile = chooser.showOpenDialog(filter.getScene().getWindow());
        runIf(chosenFile, selectedFile -> {
            if (isExcel(selectedFile)) {
                showImportDialog(selectedFile,
                    FXCollections.observableArrayList(REGISTRO, NOME, LOTE, QUANTIDADE, CODIGO, ""), meds -> {
                        configurarFiltroRapido(filter, sNGPCTable, meds);
                        atualizarPorCodigo(meds, estoqueTable);
                    });
                return;
            }
            ObservableList<Medicamento> medicamentosSNGPC = getMedicamentosSNGPC(selectedFile);
            configurarFiltroRapido(filter, sNGPCTable, medicamentosSNGPC);
            atualizarPorCodigo(medicamentosSNGPC, estoqueTable);
        });
    }

    public static FileChooser choseFile(String value) {
        return FILE_CHOOSE.computeIfAbsent(value, v -> {
            FileChooser fileChooser2 = new FileChooser();
            fileChooser2.setTitle(value);
            fileChooser2.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Excel ou PDF", "*.xlsx", "*.xls", "*.pdf"));
            return fileChooser2;
        });
    }

    public static void exportarMedicamentos(TableView<Medicamento> medicamentosEstoqueTable,
        TableView<Medicamento> medicamentosEstoqueSNGPCTable, TableView<Medicamento> medicamentosAnvisaTable) {
        RunnableEx.run(() -> {
            ObservableList<Medicamento> items0 = medicamentosEstoqueTable.getItems();
            ObservableList<Medicamento> items1 = medicamentosEstoqueSNGPCTable.getItems();
            ObservableList<Medicamento> items2 = medicamentosAnvisaTable.getItems();
            items0.stream().peek(m -> m.quantidadeCodigoValidoProperty(items1))
                .forEach(m -> m.codigoValidoProperty(items1));
            items1.stream().peek(m -> m.registroValidoProperty(items2)).peek(m -> m.loteValidoProperty(items2))
                .forEach(m -> m.quantidadeValidoProperty(items2));
            items2.stream().peek(m -> m.registroValidoProperty(items1)).peek(m -> m.loteValidoProperty(items1))
                .forEach(m -> m.quantidadeValidoProperty(items1));

            File exportarArquivo = LeitorArquivos.exportarArquivo(items0, items1, items2);
            if (openAtExport) {
                Desktop.getDesktop().open(exportarArquivo);
            }
        });
    }

    public static void setOpenAtExport(boolean openAtExport) {
        RosarioCommons.openAtExport = openAtExport;
    }

    @SuppressWarnings("unchecked")
    private static void atualizar(ObservableList<Medicamento> medicamentos,
        TableView<Medicamento> medicamentosAnvisaTable) {
        ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();
        TableColumn<Medicamento, String> tableColumn = (TableColumn<Medicamento, String>) columns.get(0);
        tableColumn.setCellFactory(newCellFactory((auxMed, cell) -> {
            cell.setText(auxMed.getRegistro());
            cell.styleProperty().bind(Bindings.when(auxMed.registroValidoProperty(medicamentos)).then("")
                .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
        }));

        TableColumn<Medicamento, String> colunaLote = (TableColumn<Medicamento, String>) columns
            .get(columns.size() - 3);
        colunaLote.setCellFactory(newCellFactory((auxMed, cell) -> {
            cell.setText(auxMed.getLote());
            cell.styleProperty().bind(Bindings.when(auxMed.loteValidoProperty(medicamentos)).then("")
                .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
        }));
        TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
            .get(columns.size() - 2);
        colunaQntd.setCellFactory(newCellFactory((auxMed, cell) -> {
            cell.setText(Integer.toString(auxMed.getQuantidade()));
            cell.styleProperty().bind(Bindings.when(auxMed.quantidadeValidoProperty(medicamentos)).then("")
                .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
        }));

    }

    @SuppressWarnings("unchecked")
    private static void atualizarPorCodigo(ObservableList<Medicamento> medicamentos,
        TableView<Medicamento> medicamentosAnvisaTable) {
        ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();

        TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
            .get(columns.size() - 2);
        colunaQntd.setCellFactory(newCellFactory((auxMed, cell) -> {
            cell.setText(Integer.toString(auxMed.getQuantidade()));
            cell.styleProperty().bind(Bindings.when(auxMed.quantidadeCodigoValidoProperty(medicamentos)).then("")
                .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
        }));
        TableColumn<Medicamento, Integer> colunaCodigo = (TableColumn<Medicamento, Integer>) columns
            .get(columns.size() - 1);
        colunaCodigo.setCellFactory(newCellFactory((auxMed, cell) -> {
            cell.setText(Integer.toString(auxMed.getCodigo()));
            cell.styleProperty().bind(Bindings.when(auxMed.codigoValidoProperty(medicamentos)).then("")
                .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
        }));

    }

    private static void configurarFiltroRapido(TextField filterField, TableView<Medicamento> medicamentosEstoqueTable,
        ObservableList<Medicamento> medicamentosRosario) {
        FilteredList<Medicamento> filteredData = new FilteredList<>(medicamentosRosario, p -> true);
        medicamentosEstoqueTable.setItems(filteredData);
        StringProperty text = filterField.textProperty();
        text.addListener((o, old, value) -> filteredData.setPredicate(medicamento -> StringUtils.isBlank(value)
            || StringUtils.containsIgnoreCase(medicamento.toString(), value)));
    }

    private static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) {
        return SupplierEx.get(() -> LeitorArquivos.getMedicamentosAnvisa(selectedFile),
            FXCollections.emptyObservableList());
    }

    private static ObservableList<Medicamento> getMedicamentosRosario(File file) {
        return SupplierEx.get(() -> LeitorArquivos.getMedicamentosRosario(file), FXCollections.emptyObservableList());
    }

    private static ObservableList<Medicamento> getMedicamentosSNGPC(File file) {
        return SupplierEx.get(() -> LeitorArquivos.getMedicamentosSNGPCPDF(file), FXCollections.emptyObservableList());
    }

    private static void showImportDialog(File excel, ObservableList<String> items,
        Consumer<ObservableList<Medicamento>> consumer) {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.centerOnScreen();
        stage.toFront();
        stage.setTitle("Importar Excel");
        ObservableList<String> sheetsExcel = LeitorArquivos.getSheetsExcel(excel);
        ChoiceBox<String> selectSheet = new ChoiceBox<>(sheetsExcel);
        ObservableList<List<String>> listExcel = LeitorArquivos.getListExcel(excel, null);
        selectSheet.getSelectionModel().selectFirst();
        int orElse = listExcel.stream().mapToInt(List<String>::size).max().orElse(items.size());
        TableView<List<String>> medicamentosTable = new TableView<>(listExcel);
        selectSheet.getSelectionModel().selectedItemProperty().addListener((val, old, newValue) -> {
            if (!Objects.equals(old, newValue)) {
                medicamentosTable.setItems(LeitorArquivos.getListExcel(excel, newValue));
            }
        });

        List<ChoiceBox<String>> colunas = new ArrayList<>();
        for (int i = 0; i < orElse; i++) {
            int j = i;
            String string = items.get(i % items.size());
            ChoiceBox<String> choiceBox = new ChoiceBox<>(items);
            colunas.add(choiceBox);
            TableColumn<List<String>, String> registroMedicamento = new TableColumn<>();
            choiceBox.getSelectionModel().select(string);
            registroMedicamento.setGraphic(choiceBox);
            registroMedicamento.setCellValueFactory(param -> new SimpleStringProperty(Objects.toString(
                param.getValue() == null || j >= param.getValue().size() ? null : param.getValue().get(j), "")));
            medicamentosTable.getColumns().add(registroMedicamento);
        }
        Button button = SimpleButtonBuilder.newButton("Importar Arquivo", a -> {
            ObservableList<Medicamento> medicamentos = LeitorArquivos.converterMedicamentos(
                medicamentosTable.getItems(),
                colunas.stream().map(e -> e.getSelectionModel().getSelectedItem()).collect(Collectors.toList()));
            consumer.accept(medicamentos);
            stage.hide();
        });
        stage.setScene(
            new Scene(new VBox(new HBox(new Label("Selecione Planilha"), selectSheet), medicamentosTable, button)));
        stage.show();
    }

}
