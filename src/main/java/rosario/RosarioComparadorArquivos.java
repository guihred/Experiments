package rosario;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class RosarioComparadorArquivos extends Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(RosarioComparadorArquivos.class);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Comparação Estoque e ANVISA");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 600, 250, Color.WHITE);
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

		Label estoqueRosario = new Label("Estoque Rosário");
		GridPane.setHalignment(estoqueRosario, HPos.CENTER);

		FileChooser fileChooserRosario = new FileChooser();
		fileChooserRosario.setTitle("Carregar Arquivo Rosário");
		fileChooserRosario.getExtensionFilters().addAll(new ExtensionFilter("Excel", "*.xlsx", "*.xls"),
				new ExtensionFilter("PDF", "*.pdf"));
		Button buttonEstoque = new Button("Carregar Arquivo Rosário");
		final TableView<Medicamento> medicamentosEstoqueTable = tabelaMedicamentos(false);
		buttonEstoque.setOnAction(e -> {
			File selectedFile = fileChooserRosario.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				ObservableList<Medicamento> medicamentosRosario = getMedicamentosRosario(selectedFile);
				FilteredList<Medicamento> filteredData = new FilteredList<>(medicamentosRosario, p -> true);
				medicamentosEstoqueTable.setItems(filteredData);
				filterField.textProperty().addListener((observable, oldValue, newValue) -> {
					filteredData.setPredicate(medicamento -> {
						if (newValue == null || newValue.isEmpty()) {
							return true;
						}
						return medicamento.toString().toLowerCase().contains(newValue.toLowerCase());
					});
				});
			}
		});

		gridpane.getChildren().add(new VBox(estoqueRosario, buttonEstoque, medicamentosEstoqueTable));

		Label estoqueSNGPC = new Label("Estoque SNGPC");
		GridPane.setHalignment(estoqueSNGPC, HPos.CENTER);

		FileChooser fileChooserSNGPC = new FileChooser();
		fileChooserSNGPC.setTitle("Carregar Arquivo SNGPC");
		fileChooserSNGPC.getExtensionFilters().addAll(new ExtensionFilter("PDF", "*.pdf"));
		Button buttonEstoqueSNGPC = new Button("Carregar Arquivo SNGPC");
		final TableView<Medicamento> medicamentosEstoqueSNGPCTable = tabelaMedicamentos(true);
		buttonEstoqueSNGPC.setOnAction(e -> {
			File selectedFile = fileChooserSNGPC.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				ObservableList<Medicamento> medicamentosSNGPC = getMedicamentosSNGPC(selectedFile);
				FilteredList<Medicamento> filteredData = new FilteredList<>(medicamentosSNGPC, p -> true);
				medicamentosEstoqueSNGPCTable.setItems(filteredData);
				filterField.textProperty().addListener((observable, oldValue, newValue) -> {
					filteredData.setPredicate(medicamento -> {
						if (newValue == null || newValue.isEmpty()) {
							return true;
						}
						return medicamento.toString().toLowerCase().contains(newValue.toLowerCase());
					});
				});
				atualizarPorCodigo(medicamentosSNGPC, medicamentosEstoqueTable);
			}
		});
		gridpane.getChildren().add(new VBox(estoqueSNGPC, buttonEstoqueSNGPC, medicamentosEstoqueSNGPCTable));

		Label estoqueAnvisa = new Label("Estoque Anvisa");
		GridPane.setHalignment(estoqueAnvisa, HPos.CENTER);
		FileChooser fileChooser2 = new FileChooser();
		fileChooser2.setTitle("Carregar Arquivo Anvisa");
		fileChooser2.getExtensionFilters().addAll(new ExtensionFilter("Excel", "*.xlsx", "*.xls"),
				new ExtensionFilter("PDF", "*.pdf"));
		Button button2 = new Button("Carregar Arquivo Anvisa");
		final TableView<Medicamento> medicamentosAnvisaTable = tabelaMedicamentos(true);
		button2.setOnAction(e -> {
			File selectedFile = fileChooser2.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				ObservableList<Medicamento> medicamentosAnvisa = getMedicamentosAnvisa(selectedFile);
				FilteredList<Medicamento> filteredData = new FilteredList<>(medicamentosAnvisa, p -> true);
				filterField.textProperty().addListener((observable, oldValue, newValue) -> {
					filteredData.setPredicate(medicamento -> {
						if (newValue == null || newValue.isEmpty()) {
							return true;
						}
						return medicamento.toString().toLowerCase().contains(newValue.toLowerCase());
					});
				});
				medicamentosAnvisaTable.setItems(filteredData);
				atualizar(medicamentosEstoqueSNGPCTable.getItems(), medicamentosAnvisaTable);
				atualizar(medicamentosAnvisaTable.getItems(), medicamentosEstoqueSNGPCTable);
			}
		});
		gridpane.getChildren().add(new VBox(estoqueAnvisa, button2, medicamentosAnvisaTable));
		exportar.setOnAction(e -> {
			try {
				ObservableList<Medicamento> items0 = medicamentosEstoqueTable.getItems();
				ObservableList<Medicamento> items = medicamentosEstoqueSNGPCTable.getItems();
				ObservableList<Medicamento> items2 = medicamentosAnvisaTable.getItems();
				items0.stream().peek(m -> m.quantidadeCodigoValidoProperty(items))
						.forEach(m -> m.codigoValidoProperty(items));
				items.stream().peek(m -> m.registroValidoProperty(items2)).peek(m -> m.loteValidoProperty(items2))
						.forEach(m -> m.quantidadeValidoProperty(items2));
				items2.stream().peek(m -> m.registroValidoProperty(items)).peek(m -> m.loteValidoProperty(items))
						.forEach(m -> m.quantidadeValidoProperty(items));

				LeitorArquivos.exportarArquivo(items0, items, items2);
			} catch (Exception e1) {
				LOGGER.error("", e1);
			}
		});

		// selection listening
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@SuppressWarnings("unchecked")
	private void atualizar(ObservableList<Medicamento> medicamentos,
			final TableView<Medicamento> medicamentosAnvisaTable) {
		ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();
		TableColumn<Medicamento, String> tableColumn = (TableColumn<Medicamento, String>) columns.get(0);
		tableColumn.setCellFactory(param -> new TableCell<Medicamento, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				int index = getIndex();
				int size = getTableView().getItems().size();
				if (index >= 0 && index < size) {
					Medicamento auxMed = getTableView().getItems().get(index);
					setText(auxMed.getRegistro());

					styleProperty().bind(
							Bindings.when(auxMed.registroValidoProperty(medicamentos)).then("")
									.otherwise("-fx-background-color:lightcoral"));
				}
			}

		});

		TableColumn<Medicamento, String> colunaLote = (TableColumn<Medicamento, String>) columns
				.get(columns.size() - 3);
		colunaLote.setCellFactory(param -> new TableCell<Medicamento, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				int index = getIndex();
				int size = getTableView().getItems().size();
				if (index >= 0 && index < size) {
					Medicamento auxMed = getTableView().getItems().get(index);
					setText(auxMed.getLote());
					styleProperty().bind(
							Bindings.when(auxMed.loteValidoProperty(medicamentos)).then("")
									.otherwise("-fx-background-color:lightcoral"));

				}
			}

		});
		TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 2);
		colunaQntd.setCellFactory(param -> new TableCell<Medicamento, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				int index = getIndex();
				int size = getTableView().getItems().size();
				if (index >= 0 && index < size) {
					Medicamento auxMed = getTableView().getItems().get(index);
					setText(Integer.toString(auxMed.getQuantidade()));
					styleProperty().bind(
							Bindings.when(auxMed.quantidadeValidoProperty(medicamentos)).then("")
									.otherwise("-fx-background-color:lightcoral"));
				}
			}

		});

	}

	@SuppressWarnings("unchecked")
	private void atualizarPorCodigo(ObservableList<Medicamento> medicamentos,
			final TableView<Medicamento> medicamentosAnvisaTable) {
		ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();

		TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 2);
		colunaQntd.setCellFactory(param -> new TableCell<Medicamento, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				int index = getIndex();
				int size = getTableView().getItems().size();
				if (index >= 0 && index < size) {
					Medicamento auxMed = getTableView().getItems().get(index);
					setText(Integer.toString(auxMed.getQuantidade()));
					styleProperty().bind(
							Bindings.when(auxMed.quantidadeCodigoValidoProperty(medicamentos)).then("")
									.otherwise("-fx-background-color:lightcoral"));
				}
			}

		});
		TableColumn<Medicamento, Integer> colunaCodigo = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 1);
		colunaCodigo.setCellFactory(param -> new TableCell<Medicamento, Integer>() {
			@Override
			protected void updateItem(Integer item, boolean empty) {
				super.updateItem(item, empty);
				int index = getIndex();
				int size = getTableView().getItems().size();
				if (index >= 0 && index < size) {
					Medicamento auxMed = getTableView().getItems().get(index);
					setText(Integer.toString(auxMed.getCodigo()));
					styleProperty().bind(
							Bindings.when(auxMed.codigoValidoProperty(medicamentos)).then("")
									.otherwise("-fx-background-color:lightcoral"));
				}
			}

		});

	}

	private TableView<Medicamento> tabelaMedicamentos(boolean completa) {

		final TableView<Medicamento> medicamentosTable = new TableView<>();
		medicamentosTable.setPrefWidth(300);
		medicamentosTable.setScaleShape(false);
		if (completa) {

			TableColumn<Medicamento, String> registroMedicamento = new TableColumn<>("Registro");
			registroMedicamento.setCellValueFactory(new PropertyValueFactory<>("registro"));
			registroMedicamento.setSortable(true);
			registroMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
			medicamentosTable.getColumns().add(registroMedicamento);
		}

		TableColumn<Medicamento, String> nomeMedicamento = new TableColumn<>("Nome");
		nomeMedicamento.setSortable(true);
		nomeMedicamento.setCellValueFactory(new PropertyValueFactory<>("nome"));
		nomeMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		medicamentosTable.getColumns().add(nomeMedicamento);

		if (completa) {
			TableColumn<Medicamento, String> loteMedicamento = new TableColumn<>("Lote");
			loteMedicamento.setSortable(true);
			loteMedicamento.setCellValueFactory(new PropertyValueFactory<>("lote"));
			loteMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
			medicamentosTable.getColumns().add(loteMedicamento);
		}

		TableColumn<Medicamento, String> quantidadeMedicamento = new TableColumn<>("Quantidade");
		quantidadeMedicamento.setSortable(true);
		quantidadeMedicamento.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
		quantidadeMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		medicamentosTable.getColumns().add(quantidadeMedicamento);

		TableColumn<Medicamento, String> codigoMedicamento = new TableColumn<>("Código");
		codigoMedicamento.setSortable(true);
		codigoMedicamento.setCellValueFactory(new PropertyValueFactory<>("codigo"));
		codigoMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		medicamentosTable.getColumns().add(codigoMedicamento);

		return medicamentosTable;
	}

	private ObservableList<Medicamento> getMedicamentosRosario(File file) {
		try {
			return LeitorArquivos.getMedicamentosRosario(file);
		} catch (Exception e) {
			LOGGER.error("", e);
			return FXCollections.emptyObservableList();
		}
	}

	private ObservableList<Medicamento> getMedicamentosSNGPC(File file) {
		try {
			return LeitorArquivos.getMedicamentosSNGPCPDF(file);
		} catch (Exception e) {
			LOGGER.error("", e);
			return FXCollections.emptyObservableList();
		}
	}

	private ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) {
		try {
			return LeitorArquivos.getMedicamentosAnvisa(selectedFile);
		} catch (Exception e) {
			LOGGER.error("", e);
			return FXCollections.emptyObservableList();
		}
	}
}
