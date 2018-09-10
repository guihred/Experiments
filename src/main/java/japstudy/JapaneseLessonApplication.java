package japstudy;

import japstudy.db.HibernateUtil;
import japstudy.db.JapaneseLesson;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JapaneseLessonApplication extends Application {

    public static final String LESSON = "Lesson";

    public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Japanese Lesson Table Displayer");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 600, 250, Color.WHITE);
		// create a grid pane
		FlowPane gridpane = new FlowPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(10);
		gridpane.setVgap(10);
		root.setCenter(gridpane);
		TableView<JapaneseLesson> tabelaJapaneseLessons = tabelaJapaneseLessons();
		Label estoqueRosario = new Label("Lessons");
		Button button = new Button("Start");
        button.setOnAction(e -> new JapaneseLessonDisplayer(tabelaJapaneseLessons.getItems()).show());
		gridpane.getChildren().add(new VBox(estoqueRosario, tabelaJapaneseLessons, button));
		GridPane.setHalignment(estoqueRosario, HPos.CENTER);
		tabelaJapaneseLessons.setItems(getLessons());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                editItem(tabelaJapaneseLessons);
            }
        });
		// selection listening
		tabelaJapaneseLessons.prefWidthProperty().bind(primaryStage.widthProperty().add(-25));
		primaryStage.setScene(scene);
		primaryStage.show();

        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
	}

	private ObservableList<JapaneseLesson> getLessons() {
		return JapaneseLessonReader.getLessons();
	}

	private TableView<JapaneseLesson> tabelaJapaneseLessons() {

		final TableView<JapaneseLesson> medicamentosTable = new TableView<>();

		medicamentosTable.setScaleShape(false);
		TableColumn<JapaneseLesson, String> registroJapaneseLesson = new TableColumn<>(LESSON);
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


        medicamentosTable.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {

                editItem(medicamentosTable);
            }
        });

		return medicamentosTable;
	}

    private void editItem(final TableView<JapaneseLesson> medicamentosTable) {
        TableViewSelectionModel<JapaneseLesson> selectionModel = medicamentosTable.getSelectionModel();
        if (!selectionModel.isEmpty()) {
            JapaneseLessonEditingDisplay japaneseLessonEditingDisplay = new JapaneseLessonEditingDisplay();
            Stage primaryStage = new Stage();
            japaneseLessonEditingDisplay.start(primaryStage);
            JapaneseLesson selectedItem = selectionModel.getSelectedItem();
            japaneseLessonEditingDisplay.setCurrent(selectedItem);
        }
    }
}
 