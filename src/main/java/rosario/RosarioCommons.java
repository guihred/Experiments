package rosario;

import static extract.ExcelService.isExcel;
import static rosario.LeitorArquivos.*;

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
import javafx.stage.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.HasLogging;

public final class RosarioCommons {
    private static final Logger LOG = HasLogging.log();
	private static final String FX_BACKGROUND_COLOR_LIGHTCORAL = "-fx-background-color:lightcoral";
	private static boolean openAtExport = true;
	private static final Map<String, FileChooser> FILE_CHOOSE = new HashMap<>();

    private RosarioCommons() {
    }

    @SuppressWarnings("unchecked")
    public static void atualizar(ObservableList<Medicamento> medicamentos,
        final TableView<Medicamento> medicamentosAnvisaTable) {
        ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();
        TableColumn<Medicamento, String> tableColumn = (TableColumn<Medicamento, String>) columns.get(0);
        tableColumn.setCellFactory(param -> new CustomableTableCell<String>() {
            @Override
            protected void setStyleable(Medicamento auxMed) {
                setText(auxMed.getRegistro());
                styleProperty().bind(Bindings.when(auxMed.registroValidoProperty(medicamentos)).then("")
                    .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }
        });

        TableColumn<Medicamento, String> colunaLote = (TableColumn<Medicamento, String>) columns
            .get(columns.size() - 3);
        colunaLote.setCellFactory(param -> new CustomableTableCell<String>() {
            @Override
            protected void setStyleable(Medicamento auxMed) {
                setText(auxMed.getLote());
                styleProperty().bind(Bindings.when(auxMed.loteValidoProperty(medicamentos)).then("")
                    .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }

        });
        TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
            .get(columns.size() - 2);
        colunaQntd.setCellFactory(param -> new CustomableTableCell<Integer>() {
            @Override
            protected void setStyleable(Medicamento auxMed) {
                setText(Integer.toString(auxMed.getQuantidade()));
                styleProperty().bind(Bindings.when(auxMed.quantidadeValidoProperty(medicamentos)).then("")
                    .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }
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

    public static void configurarFiltroRapido(TextField filterField,
        final TableView<Medicamento> medicamentosEstoqueTable, ObservableList<Medicamento> medicamentosRosario) {
        FilteredList<Medicamento> filteredData = new FilteredList<>(medicamentosRosario, p -> true);
        medicamentosEstoqueTable.setItems(filteredData);
        StringProperty text = filterField.textProperty();
        text.addListener((o, old, value) -> filteredData.setPredicate(medicamento -> StringUtils.isBlank(value)
            || StringUtils.containsIgnoreCase(medicamento.toString(), value)));
    }

    public static void exportarMedicamentos(final TableView<Medicamento> medicamentosEstoqueTable,
        final TableView<Medicamento> medicamentosEstoqueSNGPCTable,
        final TableView<Medicamento> medicamentosAnvisaTable) {
        try {
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
        } catch (Exception e1) {
            LOG.error("", e1);
        }
    }

    public static Map<String, FileChooser> getFileChoose() {
        return FILE_CHOOSE;
    }

    public static ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) {
        try {
            return LeitorArquivos.getMedicamentosAnvisa(selectedFile);
        } catch (Exception e) {
            LOG.error("", e);
            return FXCollections.emptyObservableList();
        }
    }

    public static ObservableList<Medicamento> getMedicamentosRosario(File file) {
        try {
            return LeitorArquivos.getMedicamentosRosario(file);
        } catch (Exception e) {
            LOG.error("", e);
            return FXCollections.emptyObservableList();
        }
    }

    public static ObservableList<Medicamento> getMedicamentosSNGPC(File file) {
        try {
            return LeitorArquivos.getMedicamentosSNGPCPDF(file);
        } catch (Exception e) {
            LOG.error("", e);
            return FXCollections.emptyObservableList();
        }
    }

    public static void setOpenAtExport(boolean openAtExport) {
        RosarioCommons.openAtExport = openAtExport;
    }

    public static void showImportDialog(File excel, ObservableList<String> items,
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
        final TableView<List<String>> medicamentosTable = new TableView<>(listExcel);
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

    public static TableView<Medicamento> tabelaMedicamentos(boolean completa) {

        final SimpleTableViewBuilder<Medicamento> medicamentosTable = new SimpleTableViewBuilder<Medicamento>()
            .prefWidth(250).scaleShape(false);
        if (completa) {
            medicamentosTable.addColumn(REGISTRO, "registro");
        }
        medicamentosTable.addColumn(NOME, "nome");
        if (completa) {
            medicamentosTable.addColumn(LOTE, "lote");
        }
        medicamentosTable.addColumn(QUANTIDADE, "quantidade");
        medicamentosTable.addColumn(CODIGO, "codigo");
        return medicamentosTable.equalColumns().build();
    }

    @SuppressWarnings("unchecked")
    static void atualizarPorCodigo(ObservableList<Medicamento> medicamentos,
        final TableView<Medicamento> medicamentosAnvisaTable) {
        ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();

        TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
            .get(columns.size() - 2);
        colunaQntd.setCellFactory(param -> new CustomableTableCell<Integer>() {
            @Override
            protected void setStyleable(Medicamento auxMed) {
                setText(Integer.toString(auxMed.getQuantidade()));
                styleProperty().bind(Bindings.when(auxMed.quantidadeCodigoValidoProperty(medicamentos)).then("")
                    .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }

        });
        TableColumn<Medicamento, Integer> colunaCodigo = (TableColumn<Medicamento, Integer>) columns
            .get(columns.size() - 1);
        colunaCodigo.setCellFactory(param -> new CustomableTableCell<Integer>() {
            @Override
            protected void setStyleable(Medicamento auxMed) {
                setText(Integer.toString(auxMed.getCodigo()));
                styleProperty().bind(Bindings.when(auxMed.codigoValidoProperty(medicamentos)).then("")
                    .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));

            }

        });

    }

    static void carregarEstoqueAnvisa(TextField filterField, final TableView<Medicamento> medicamentosEstoqueSNGPCTable,
        FileChooser fileChooser2, final TableView<Medicamento> medicamentosAnvisaTable) {
        Window primaryStage = filterField.getScene().getWindow();
        File selectedFile = fileChooser2.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            if (isExcel(selectedFile)) {
                showImportDialog(selectedFile, FXCollections.observableArrayList(REGISTRO, NOME, LOTE, QUANTIDADE, ""),
                    meds -> {
                        configurarFiltroRapido(filterField, medicamentosAnvisaTable, meds);
                        atualizar(medicamentosEstoqueSNGPCTable.getItems(), medicamentosAnvisaTable);
                        atualizar(medicamentosAnvisaTable.getItems(), medicamentosEstoqueSNGPCTable);
                    });
                return;
            }
            ObservableList<Medicamento> medicamentosAnvisa = getMedicamentosAnvisa(selectedFile);
            configurarFiltroRapido(filterField, medicamentosAnvisaTable, medicamentosAnvisa);
            atualizar(medicamentosEstoqueSNGPCTable.getItems(), medicamentosAnvisaTable);
            atualizar(medicamentosAnvisaTable.getItems(), medicamentosEstoqueSNGPCTable);
        }
    }

    static void carregarEstoqueLoja(TextField filterField, final TableView<Medicamento> medicamentosEstoqueTable,
        FileChooser fileChooserRosario) {
        Window primaryStage = filterField.getScene().getWindow();

        File selectedFile = fileChooserRosario.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            if (isExcel(selectedFile)) {
                showImportDialog(selectedFile, FXCollections.observableArrayList(CODIGO, NOME, QUANTIDADE, ""),
                    meds -> configurarFiltroRapido(filterField, medicamentosEstoqueTable, meds));
                return;
            }
            ObservableList<Medicamento> medicamentosRosario = getMedicamentosRosario(selectedFile);
            configurarFiltroRapido(filterField, medicamentosEstoqueTable, medicamentosRosario);
        }
    }

    static void carregarSNGPC(TextField filterField, final TableView<Medicamento> medicamentosEstoqueTable,
        FileChooser fileChooserSNGPC, final TableView<Medicamento> medicamentosEstoqueSNGPCTable) {
        Window primaryStage = filterField.getScene().getWindow();

        File selectedFile = fileChooserSNGPC.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            if (isExcel(selectedFile)) {
                showImportDialog(selectedFile,
                    FXCollections.observableArrayList(REGISTRO, NOME, LOTE, QUANTIDADE, CODIGO, ""), meds -> {
                        configurarFiltroRapido(filterField, medicamentosEstoqueSNGPCTable, meds);
                        atualizarPorCodigo(meds, medicamentosEstoqueTable);
                    });
                return;
            }
            ObservableList<Medicamento> medicamentosSNGPC = getMedicamentosSNGPC(selectedFile);
            configurarFiltroRapido(filterField, medicamentosEstoqueSNGPCTable, medicamentosSNGPC);
            atualizarPorCodigo(medicamentosSNGPC, medicamentosEstoqueTable);
        }
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
