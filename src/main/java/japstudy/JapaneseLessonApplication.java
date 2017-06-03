package japstudy;

import java.io.IOException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JapaneseLessonApplication extends Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(JapaneseLessonApplication.class);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Lesson");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 600, 250, Color.WHITE);
		// create a grid pane
		FlowPane gridpane = new FlowPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(10);
		gridpane.setVgap(10);
		root.setCenter(gridpane);
		TableView<JapaneseLesson> tabelaJapaneseLessons = tabelaJapaneseLessons(gridpane);
		Label estoqueRosario = new Label("Lessons");
		gridpane.getChildren().add(new VBox(estoqueRosario, tabelaJapaneseLessons));

		GridPane.setHalignment(estoqueRosario, HPos.CENTER);
		tabelaJapaneseLessons.setItems(getLessons());
		// selection listening
		tabelaJapaneseLessons.prefWidthProperty().bind(primaryStage.widthProperty().add(-25));
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private ObservableList<JapaneseLesson> getLessons() {
		try {
			return JapaneseLessonReader.getLessons("jaftranscript.docx");
		} catch (IOException e) {
			LOGGER.error("ERRO AO", e);
			return FXCollections.observableArrayList();
		}
	}

	private TableView<JapaneseLesson> tabelaJapaneseLessons(FlowPane gridpane) {

		final TableView<JapaneseLesson> medicamentosTable = new TableView<>();

		medicamentosTable.setScaleShape(false);
		TableColumn<JapaneseLesson, String> registroJapaneseLesson = new TableColumn<>("Lesson");
		registroJapaneseLesson.setCellValueFactory(new PropertyValueFactory<>("lesson"));
		registroJapaneseLesson.setSortable(true);
		registroJapaneseLesson.prefWidthProperty().bind(medicamentosTable.prefWidthProperty().multiply(1.5 / 12));
		medicamentosTable.getColumns().add(registroJapaneseLesson);
		TableColumn<JapaneseLesson, String> codigoJapaneseLesson = new TableColumn<>("Number");
		codigoJapaneseLesson.setSortable(true);
		codigoJapaneseLesson.setCellValueFactory(new PropertyValueFactory<>("exercise"));
		codigoJapaneseLesson.prefWidthProperty().bind(medicamentosTable.prefWidthProperty().multiply(1.5 / 12));
		medicamentosTable.getColumns().add(codigoJapaneseLesson);

		TableColumn<JapaneseLesson, String> nomeJapaneseLesson = new TableColumn<>("English");
		nomeJapaneseLesson.setSortable(true);
		nomeJapaneseLesson.setCellValueFactory(new PropertyValueFactory<>("english"));
		nomeJapaneseLesson.prefWidthProperty().bind(medicamentosTable.prefWidthProperty().multiply(3.0 / 12));
		medicamentosTable.getColumns().add(nomeJapaneseLesson);

		TableColumn<JapaneseLesson, String> loteJapaneseLesson = new TableColumn<>("Japanese");
		loteJapaneseLesson.setSortable(true);
		loteJapaneseLesson.setCellValueFactory(new PropertyValueFactory<>("japanese"));
		loteJapaneseLesson.prefWidthProperty().bind(medicamentosTable.prefWidthProperty().multiply(3.0 / 12));
		medicamentosTable.getColumns().add(loteJapaneseLesson);

		TableColumn<JapaneseLesson, String> quantidadeJapaneseLesson = new TableColumn<>("Romaji");
		quantidadeJapaneseLesson.setSortable(true);
		quantidadeJapaneseLesson.setCellValueFactory(new PropertyValueFactory<>("romaji"));
		quantidadeJapaneseLesson.prefWidthProperty().bind(medicamentosTable.prefWidthProperty().multiply(3.0 / 12));
		medicamentosTable.getColumns().add(quantidadeJapaneseLesson);


		return medicamentosTable;
	}

}
