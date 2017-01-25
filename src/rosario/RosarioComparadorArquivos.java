package rosario;

import java.io.File;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
		// candidates label
		// List of leaders

		Label estoqueRosario = new Label("Estoque Rosário");
		GridPane.setHalignment(estoqueRosario, HPos.CENTER);

		FileChooser fileChooserRosario = new FileChooser();
		fileChooserRosario.setTitle("Carregar Arquivo Rosário");
		fileChooserRosario.getExtensionFilters().addAll(new ExtensionFilter("PDF", "*.pdf"));
		Button buttonEstoque = new Button("Carregar Arquivo Rosário");
		final TableView<Medicamento> medicamentosEstoqueTable = tabelaMedicamentos();
		buttonEstoque.setOnAction(e -> {
			File selectedFile = fileChooserRosario.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				medicamentosEstoqueTable.setItems(getMedicamentosRosario(selectedFile));
			}
		});
		gridpane.getChildren().add(new VBox(estoqueRosario, buttonEstoque, medicamentosEstoqueTable));

		Label estoqueSNGPC = new Label("Estoque SNGPC");
		GridPane.setHalignment(estoqueSNGPC, HPos.CENTER);

		FileChooser fileChooserSNGPC = new FileChooser();
		fileChooserSNGPC.setTitle("Carregar Arquivo SNGPC");
		fileChooserSNGPC.getExtensionFilters().addAll(new ExtensionFilter("PDF", "*.pdf", "*.pdf"));
		Button buttonEstoqueSNGPC = new Button("Carregar Arquivo SNGPC");
		final TableView<Medicamento> medicamentosEstoqueSNGPCTable = tabelaMedicamentos();
		buttonEstoqueSNGPC.setOnAction(e -> {
			File selectedFile = fileChooserSNGPC.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				medicamentosEstoqueSNGPCTable.setItems(getMedicamentosSNGPC(selectedFile));
				// atualizarPorCodigo(medicamentosEstoqueTable.getItems(),
				// medicamentosEstoqueSNGPCTable);
				atualizarPorCodigo(medicamentosEstoqueSNGPCTable.getItems(), medicamentosEstoqueTable);
			}
		});
		gridpane.getChildren().add(new VBox(estoqueSNGPC, buttonEstoqueSNGPC, medicamentosEstoqueSNGPCTable));

		Label estoqueAnvisa = new Label("Estoque Anvisa");
		GridPane.setHalignment(estoqueAnvisa, HPos.CENTER);
		FileChooser fileChooser2 = new FileChooser();
		fileChooser2.setTitle("Carregar Arquivo Anvisa");
		fileChooser2.getExtensionFilters().addAll(new ExtensionFilter("Excel", "*.xlsx"));
		Button button2 = new Button("Carregar Arquivo Anvisa");
		final TableView<Medicamento> medicamentosAnvisaTable = tabelaMedicamentos();
		button2.setOnAction(e -> {
			File selectedFile = fileChooser2.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				medicamentosAnvisaTable.setItems(getMedicamentosAnvisa(selectedFile));
				atualizar(medicamentosEstoqueSNGPCTable.getItems(), medicamentosAnvisaTable);
				atualizar(medicamentosAnvisaTable.getItems(), medicamentosEstoqueSNGPCTable);
			}
		});
		gridpane.getChildren().add(new VBox(estoqueAnvisa, button2, medicamentosAnvisaTable));

		Button exportar = new Button("Exportar Excel");
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
				e1.printStackTrace();
			}
		});
		gridpane.getChildren().add(exportar);
		// selection listening
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@SuppressWarnings("unchecked")
	private void atualizar(ObservableList<Medicamento> medicamentos,
			final TableView<Medicamento> medicamentosAnvisaTable) {
		ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();
		TableColumn<Medicamento, String> tableColumn = (TableColumn<Medicamento, String>) columns.get(0);
		tableColumn.setCellFactory(param -> {
			return new TableCell<Medicamento, String>() {
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

			};
		});

		TableColumn<Medicamento, String> colunaLote = (TableColumn<Medicamento, String>) columns
				.get(columns.size() - 3);
		colunaLote.setCellFactory(param -> {
			return new TableCell<Medicamento, String>() {
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

			};
		});
		TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 2);
		colunaQntd.setCellFactory(param -> {
			return new TableCell<Medicamento, Integer>() {
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

			};
		});

	}

	@SuppressWarnings("unchecked")
	private void atualizarPorCodigo(ObservableList<Medicamento> medicamentos,
			final TableView<Medicamento> medicamentosAnvisaTable) {
		ObservableList<TableColumn<Medicamento, ?>> columns = medicamentosAnvisaTable.getColumns();

		TableColumn<Medicamento, Integer> colunaQntd = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 2);
		colunaQntd.setCellFactory(param -> {
			return new TableCell<Medicamento, Integer>() {
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

			};
		});
		TableColumn<Medicamento, Integer> colunaCodigo = (TableColumn<Medicamento, Integer>) columns
				.get(columns.size() - 1);
		colunaCodigo.setCellFactory(param -> {
			return new TableCell<Medicamento, Integer>() {
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

			};
		});

	}

	@SuppressWarnings("unchecked")
	private TableView<Medicamento> tabelaMedicamentos() {

		final TableView<Medicamento> medicamentosTable = new TableView<>();
		medicamentosTable.setPrefWidth(300);
		medicamentosTable.setScaleShape(false);

		TableColumn<Medicamento, String> registroMedicamento = new TableColumn<>("Registro");
		registroMedicamento.setCellValueFactory(new PropertyValueFactory<>("registro"));

		registroMedicamento.setSortable(true);

		registroMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		TableColumn<Medicamento, String> nomeMedicamento = new TableColumn<>("Nome");

		nomeMedicamento.setSortable(true);
		nomeMedicamento.setCellValueFactory(new PropertyValueFactory<>("nome"));
		nomeMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);

		// TableColumn<Medicamento, String> apresentacaoMedicamento = new
		// TableColumn<>("Apresentacao");
		// apresentacaoMedicamento.setSortable(true);
		// apresentacaoMedicamento.setCellValueFactory(new
		// PropertyValueFactory<>("apresentacao"));

		TableColumn<Medicamento, String> loteMedicamento = new TableColumn<>("Lote");
		loteMedicamento.setSortable(true);
		loteMedicamento.setCellValueFactory(new PropertyValueFactory<>("lote"));
		loteMedicamento

		.setPrefWidth(medicamentosTable.getPrefWidth() / 4);
		TableColumn<Medicamento, String> quantidadeMedicamento = new TableColumn<>("Quantidade");
		quantidadeMedicamento.setSortable(true);
		quantidadeMedicamento.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
		quantidadeMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);

		TableColumn<Medicamento, String> codigoMedicamento = new TableColumn<>("Código");
		codigoMedicamento.setSortable(true);
		codigoMedicamento.setCellValueFactory(new PropertyValueFactory<>("codigo"));
		codigoMedicamento.setPrefWidth(medicamentosTable.getPrefWidth() / 4);

		medicamentosTable.getColumns().setAll(registroMedicamento, nomeMedicamento,
				loteMedicamento, quantidadeMedicamento, codigoMedicamento);
		return medicamentosTable;
	}

	private ObservableList<Medicamento> getMedicamentosRosario(File file) {
		try {
			ObservableList<Medicamento> people = LeitorArquivos.getMedicamentosRosario(file);
			return people;
		} catch (Exception e) {
			e.printStackTrace();
			return FXCollections.emptyObservableList();
		}
	}

	private ObservableList<Medicamento> getMedicamentosSNGPC(File file) {
		try {
			ObservableList<Medicamento> people = LeitorArquivos.getMedicamentosSNGPCPDF(file);
			return people;
		} catch (Exception e) {
			e.printStackTrace();
			return FXCollections.emptyObservableList();
		}
	}

	private ObservableList<Medicamento> getMedicamentosAnvisa(File selectedFile) {
		try {
			ObservableList<Medicamento> people = LeitorArquivos.getMedicamentosAnvisa(selectedFile);
			return people;
		} catch (Exception e) {
			e.printStackTrace();
			return FXCollections.emptyObservableList();
		}
	}
}
