package rosario;

import static rosario.LeitorArquivos.*;

import java.awt.Desktop;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.HasLogging;

public class RosarioComparadorArquivos extends Application implements HasLogging {




    private static final String FX_BACKGROUND_COLOR_LIGHTCORAL = "-fx-background-color:lightcoral";
    private Map<String, FileChooser> fileChoose = new HashMap<>();
    private boolean openAtExport = true;

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
		buttonEstoque.setOnAction(e -> {
			File selectedFile = fileChooserRosario.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				if (LeitorArquivos.isExcel(selectedFile)) {
					showImportDialog(selectedFile,
							FXCollections.observableArrayList(CODIGO, NOME, QUANTIDADE, ""),
							meds -> configurarFiltroRapido(filterField, medicamentosEstoqueTable, meds));
					return;
				}
				ObservableList<Medicamento> medicamentosRosario = getMedicamentosRosario(selectedFile);
				configurarFiltroRapido(filterField, medicamentosEstoqueTable, medicamentosRosario);
			}
		});

		VBox e = new VBox(estoqueRosario, buttonEstoque, medicamentosEstoqueTable);
		gridpane.getChildren().add(e);

		Label estoqueSNGPC = new Label("Estoque SNGPC");
		GridPane.setHalignment(estoqueSNGPC, HPos.CENTER);

		FileChooser fileChooserSNGPC = choseFile("Carregar Arquivo SNGPC");
		final TableView<Medicamento> medicamentosEstoqueSNGPCTable = tabelaMedicamentos(true);
		Button buttonEstoqueSNGPC = new Button("Carregar Arquivo SNGPC");
        buttonEstoqueSNGPC.setId("SNGPC");
		buttonEstoqueSNGPC.setOnAction(a -> {
			File selectedFile = fileChooserSNGPC.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				if (LeitorArquivos.isExcel(selectedFile)) {
					showImportDialog(selectedFile,
							FXCollections.observableArrayList(REGISTRO, NOME, LOTE, QUANTIDADE, CODIGO, ""),
							meds -> {
						configurarFiltroRapido(filterField, medicamentosEstoqueSNGPCTable, meds);
						atualizarPorCodigo(meds, medicamentosEstoqueTable);
					});
					return;
				}
				ObservableList<Medicamento> medicamentosSNGPC = getMedicamentosSNGPC(selectedFile);
				configurarFiltroRapido(filterField, medicamentosEstoqueSNGPCTable, medicamentosSNGPC);
				atualizarPorCodigo(medicamentosSNGPC, medicamentosEstoqueTable);
			}
		});
		gridpane.getChildren().add(new VBox(estoqueSNGPC, buttonEstoqueSNGPC, medicamentosEstoqueSNGPCTable));

		Label estoqueAnvisa = new Label("Estoque Anvisa");
		GridPane.setHalignment(estoqueAnvisa, HPos.CENTER);
		FileChooser fileChooser2 = choseFile("Carregar Arquivo Anvisa");
		Button button2 = new Button("Carregar Arquivo Anvisa");
		final TableView<Medicamento> medicamentosAnvisaTable = tabelaMedicamentos(true);
		button2.setOnAction(a -> {
			File selectedFile = fileChooser2.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				if (LeitorArquivos.isExcel(selectedFile)) {
					showImportDialog(selectedFile,
							FXCollections.observableArrayList(REGISTRO, NOME, LOTE, QUANTIDADE, ""), meds -> {
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
		});
		gridpane.getChildren().add(new VBox(estoqueAnvisa, button2, medicamentosAnvisaTable));
        exportar.setOnAction(a -> exportarMedicamentos(medicamentosEstoqueTable, medicamentosEstoqueSNGPCTable,
                medicamentosAnvisaTable));
		// selection listening
        primaryStage.setScene(new Scene(root, 1000, 500, Color.WHITE));
		primaryStage.show();
	}

	@SuppressWarnings("unchecked")
	private void atualizar(ObservableList<Medicamento> medicamentos,
			final TableView<Medicamento> medicamentosAnvisaTable) {
		ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();
		TableColumn<Medicamento, String> tableColumn = (TableColumn<Medicamento, String>) columns.get(0);
        tableColumn.setCellFactory(param -> new CustomableTableCell<String>() {
            @Override
            void setStyleable(Medicamento auxMed) {
                setText(auxMed.getRegistro());
                styleProperty().bind(Bindings.when(auxMed.registroValidoProperty(medicamentos)).then("")
                        .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }
		});

		TableColumn<Medicamento, String> colunaLote = (TableColumn<Medicamento, String>) columns
				.get(columns.size() - 3);
        colunaLote.setCellFactory(param -> new CustomableTableCell<String>() {
            @Override
            void setStyleable(Medicamento auxMed) {
                setText(auxMed.getLote());
                styleProperty().bind(Bindings.when(auxMed.loteValidoProperty(medicamentos)).then("")
                        .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }

		});
        TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
                .get(columns.size() - 2);
        colunaQntd.setCellFactory(param -> new CustomableTableCell<Integer>() {
            @Override
            void setStyleable(Medicamento auxMed) {
                setText(Integer.toString(auxMed.getQuantidade()));
                styleProperty().bind(Bindings.when(auxMed.quantidadeValidoProperty(medicamentos)).then("")
                        .otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
            }
        });

	}



    @SuppressWarnings("unchecked")
	private void atualizarPorCodigo(ObservableList<Medicamento> medicamentos,
			final TableView<Medicamento> medicamentosAnvisaTable) {
		ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();

		TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 2);
        colunaQntd.setCellFactory(param -> new CustomableTableCell<Integer>() {
            @Override
            void setStyleable(Medicamento auxMed) {
					setText(Integer.toString(auxMed.getQuantidade()));
					styleProperty().bind(
							Bindings.when(auxMed.quantidadeCodigoValidoProperty(medicamentos)).then("")
									.otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));
			}

		});
		TableColumn<Medicamento, Integer> colunaCodigo = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 1);
        colunaCodigo.setCellFactory(param -> new CustomableTableCell<Integer>() {
            @Override
            void setStyleable(Medicamento auxMed) {
					setText(Integer.toString(auxMed.getCodigo()));
					styleProperty().bind(
							Bindings.when(auxMed.codigoValidoProperty(medicamentos)).then("")
									.otherwise(FX_BACKGROUND_COLOR_LIGHTCORAL));

			}

		});

	}

	private FileChooser choseFile(String value) {
		FileChooser fileChooser2 = new FileChooser();
		fileChooser2.setTitle(value);
        fileChooser2.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Excel ou PDF", "*.xlsx", "*.xls", "*.pdf"));
        fileChoose.put(value, fileChooser2);
		return fileChooser2;
	}

	private void configurarFiltroRapido(TextField filterField, final TableView<Medicamento> medicamentosEstoqueTable,
			ObservableList<Medicamento> medicamentosRosario) {
		FilteredList<Medicamento> filteredData = new FilteredList<>(medicamentosRosario, p -> true);
		medicamentosEstoqueTable.setItems(filteredData);
		filterField.textProperty()
                .addListener((observable, oldValue, newValue) -> filteredData.setPredicate(medicamento -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    return medicamento.toString().toLowerCase().contains(newValue.toLowerCase());
                }));
	}

	private void exportarMedicamentos(final TableView<Medicamento> medicamentosEstoqueTable,
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
            getLogger().error("", e1);
        }
    }

	private ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) {
		try {
			return LeitorArquivos.getMedicamentosAnvisa(selectedFile);
		} catch (Exception e) {
            getLogger().error("", e);
			return FXCollections.emptyObservableList();
		}
	}

	private ObservableList<Medicamento> getMedicamentosRosario(File file) {
		try {
			return LeitorArquivos.getMedicamentosRosario(file);
		} catch (Exception e) {
            getLogger().error("", e);
			return FXCollections.emptyObservableList();
		}
	}

	private ObservableList<Medicamento> getMedicamentosSNGPC(File file) {
		try {
			return LeitorArquivos.getMedicamentosSNGPCPDF(file);
		} catch (Exception e) {
            getLogger().error("", e);
			return FXCollections.emptyObservableList();
		}
	}

	private void showImportDialog(File excel, ObservableList<String> items,
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
		selectSheet.getSelectionModel().selectedItemProperty().addListener(
				(val, old, newValue) -> {
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
			registroMedicamento.setCellValueFactory(param -> new SimpleStringProperty(
					Objects.toString(
							param.getValue() == null || j >= param.getValue().size() ? null : param.getValue().get(j),
							"")));
			medicamentosTable.getColumns().add(registroMedicamento);
		}
		Button button = new Button("Importar Arquivo");
		button.setOnAction(a -> {
            ObservableList<Medicamento> medicamentos = LeitorArquivos
                    .converterMedicamentos(medicamentosTable.getItems(), colunas.stream()
                            .map(e -> e.getSelectionModel().getSelectedItem()).collect(Collectors.toList()));
			consumer.accept(medicamentos);
			stage.hide();
		});
		stage.setScene(
				new Scene(new VBox(new HBox(new Label("Selecione Planilha"), selectSheet), medicamentosTable, button)));
		stage.show();
	}

	private TableView<Medicamento> tabelaMedicamentos(boolean completa) {

		final TableView<Medicamento> medicamentosTable = new TableView<>();
		medicamentosTable.setPrefWidth(300);
		medicamentosTable.setScaleShape(false);
		if (completa) {
			TableColumn<Medicamento, String> registroMedicamento = new TableColumn<>(REGISTRO);
			registroMedicamento.setCellValueFactory(new PropertyValueFactory<>("registro"));
			registroMedicamento.setSortable(true);
			registroMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
			medicamentosTable.getColumns().add(registroMedicamento);
		}

		TableColumn<Medicamento, String> nomeMedicamento = new TableColumn<>(NOME);
		nomeMedicamento.setSortable(true);
		nomeMedicamento.setCellValueFactory(new PropertyValueFactory<>("nome"));
		nomeMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		medicamentosTable.getColumns().add(nomeMedicamento);

		if (completa) {
			TableColumn<Medicamento, String> loteMedicamento = new TableColumn<>(LOTE);
			loteMedicamento.setSortable(true);
			loteMedicamento.setCellValueFactory(new PropertyValueFactory<>("lote"));
			loteMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
			medicamentosTable.getColumns().add(loteMedicamento);
		}

		TableColumn<Medicamento, String> quantidadeMedicamento = new TableColumn<>(QUANTIDADE);
		quantidadeMedicamento.setSortable(true);
		quantidadeMedicamento.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
		quantidadeMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		medicamentosTable.getColumns().add(quantidadeMedicamento);

        TableColumn<Medicamento, String> codigoMedicamento = new TableColumn<>(CODIGO);
		codigoMedicamento.setSortable(true);
		codigoMedicamento.setCellValueFactory(new PropertyValueFactory<>("codigo"));
		codigoMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		medicamentosTable.getColumns().add(codigoMedicamento);

		return medicamentosTable;
	}


    public Map<String, FileChooser> getFileChoose() {
        return fileChoose;
    }

    public void setOpenAtExport(boolean openAtExport) {
        this.openAtExport = openAtExport;
    }

    private abstract class CustomableTableCell<T> extends TableCell<Medicamento, T> {

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

        abstract void setStyleable(Medicamento auxMed);
    }
    public static void main(String[] args) {
		launch(args);
	}

}
