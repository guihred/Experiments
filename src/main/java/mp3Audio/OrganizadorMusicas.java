package mp3Audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javax.swing.filechooser.FileSystemView;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public class OrganizadorMusicas extends Application {
    private static final Logger LOGGER = HasLogging.log();
	public static void main(String[] args) {
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Organizador ");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 600, 250, Color.WHITE);
		FlowPane gridpane = new FlowPane();
		gridpane.setVgap(10);
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(10);
		root.setCenter(gridpane);

		Label listaMusicas = new Label("Lista Músicas");
		GridPane.setHalignment(listaMusicas, HPos.CENTER);
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Carregar Pasta de Músicas");
		Button buttonEstoque = new Button("Carregar Musicas");
		final TableView<Musica> medicamentosEstoqueTable = tabelaMusicas();
		String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
		File musicsDirectory = new File(new File(path).getParentFile(), "Music");

		medicamentosEstoqueTable.setItems(getMusicas(musicsDirectory));
		medicamentosEstoqueTable.prefWidthProperty().bind(root.widthProperty().add(-50));
		medicamentosEstoqueTable.prefHeightProperty().bind(root.heightProperty());
		buttonEstoque.setOnAction(e -> {
			File selectedFile = chooser.showDialog(primaryStage);
			if (selectedFile != null) {
				medicamentosEstoqueTable.setItems(getMusicas(selectedFile));
			}
		});
		gridpane.getChildren().add(new VBox(listaMusicas, buttonEstoque, medicamentosEstoqueTable));

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@SuppressWarnings("unchecked")
	private TableView<Musica> tabelaMusicas() {

		final TableView<Musica> musicaTable = new TableView<>();
		musicaTable.setPrefWidth(600);
		musicaTable.setScaleShape(false);

		musicaTable.setOnMousePressed(event -> handleMousePressed(musicaTable, event));

		TableColumn<Musica, String> tituloMusica = new TableColumn<>("Título");
		tituloMusica.setCellValueFactory(new PropertyValueFactory<>("titulo"));

		tituloMusica.setSortable(true);

		int other = 6;
		tituloMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));
		TableColumn<Musica, String> nomeMusica = new TableColumn<>("Artista");

		nomeMusica.setSortable(true);
		nomeMusica.setCellValueFactory(new PropertyValueFactory<>("artista"));
		nomeMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));

		TableColumn<Musica, String> albumMusica = new TableColumn<>("Álbum");
		albumMusica.setSortable(true);
		albumMusica.setCellValueFactory(new PropertyValueFactory<>("album"));
		albumMusica

		.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));
		TableColumn<Musica, String> anoMusica = new TableColumn<>("Ano");
		anoMusica.setSortable(true);
		anoMusica.setCellValueFactory(new PropertyValueFactory<>("ano"));
		anoMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));

		TableColumn<Musica, String> generoMusica = new TableColumn<>("Gênero");
		generoMusica.setSortable(true);
		generoMusica.setCellValueFactory(new PropertyValueFactory<>("genero"));
		generoMusica.prefWidthProperty().bind(musicaTable.prefWidthProperty().divide(other));

		musicaTable.getColumns().setAll(tituloMusica, nomeMusica, albumMusica, anoMusica, generoMusica);
		return musicaTable;
	}


	private void handleMousePressed(final TableView<Musica> musicaTable, MouseEvent event) {
		if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
			Musica selectedItem = musicaTable.getSelectionModel().getSelectedItem();
			Stage stage = new Stage();
			VBox root = new VBox();
			root.getChildren().addAll(criarField("Título", selectedItem.tituloProperty()));
			root.getChildren().addAll(criarField("Artista", selectedItem.artistaProperty()));
			root.getChildren().addAll(criarField("Álbum", selectedItem.albumProperty()));

			byte[] extractEmbeddedImageData = LeitorMusicas.extractEmbeddedImageData(selectedItem.getArquivo());
			if (extractEmbeddedImageData != null) {
				Image image = new Image(new ByteArrayInputStream(extractEmbeddedImageData));
				root.getChildren().addAll(new ImageView(image));
			} else {
				Button button = new Button("Escolha a Foto");
				root.getChildren().add(button);
				button.setOnAction(a -> {
					Stage stage2 = new Stage();
					FlowPane flow = new FlowPane();
					flow.setPrefWrapLength(300);

					List<String> imagens = LeitorMusicas.getImagens("\"" + selectedItem.getArtista() + "\" \""
							+ selectedItem.getTitulo() + "\" \"" + selectedItem.getAlbum() + "\"");
					for (String url : imagens) {
						ImageView pages = new ImageView(url);
                        pages.setOnMouseClicked(e -> LOGGER.info(url));

						flow.getChildren().add(pages);
					}

					stage.setScene(new Scene(flow));
					stage2.show();
				});

			}
			stage.setScene(new Scene(root));
			stage.show();
            LOGGER.info("{}", selectedItem);
		}
	}

	private ObservableList<Musica> getMusicas(File file) {
		try {
			return LeitorMusicas.getMusicas(file);
		} catch (Exception e) {
			LOGGER.error("", e);
			return FXCollections.emptyObservableList();
		}
	}

	private Node[] criarField(String nome, StringProperty propriedade) {
		TextField textField = new TextField();
		textField.textProperty().bindBidirectional(propriedade);
		return new Node[] { new Label(nome), textField };
	}

}
