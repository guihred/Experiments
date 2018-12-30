package japstudy;

import japstudy.db.HibernateUtil;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;

public class JapaneseLessonApplication extends Application {

    public static final String LESSON = "Lesson";

    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Japanese Lesson Table Displayer");
		BorderPane root = new BorderPane();
        final int width = 600;
        final int height = 250;
        Scene scene = new Scene(root, width, height, Color.WHITE);
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
        final int padding = 25;
        tabelaJapaneseLessons.prefWidthProperty().bind(primaryStage.widthProperty().add(-padding));
		primaryStage.setScene(scene);
		primaryStage.show();

        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
	}

	private void editItem(final TableView<JapaneseLesson> lessonsTable) {
        TableViewSelectionModel<JapaneseLesson> selectionModel = lessonsTable.getSelectionModel();
        if (!selectionModel.isEmpty()) {
            JapaneseLessonEditingDisplay japaneseLessonEditingDisplay = new JapaneseLessonEditingDisplay();
            Stage primaryStage = new Stage();
            japaneseLessonEditingDisplay.start(primaryStage);
            JapaneseLesson selectedItem = selectionModel.getSelectedItem();
            japaneseLessonEditingDisplay.setCurrent(selectedItem);
        }
    }

    private TableView<JapaneseLesson> tabelaJapaneseLessons() {

		return new SimpleTableViewBuilder<JapaneseLesson>()
		        .scaleShape(false)
		        .addColumn(LESSON, "lesson")
		        .addColumn("Number", "exercise")
		        .addColumn("English", "english")
		        .addColumn("Japanese", "japanese")
		        .addColumn("Romaji", "romaji")
		        .equalColumns()
                .onDoubleClick((JapaneseLesson selectedItem) -> {
		            JapaneseLessonEditingDisplay japaneseLessonEditingDisplay = new JapaneseLessonEditingDisplay();
		            japaneseLessonEditingDisplay.start(new Stage());
		            japaneseLessonEditingDisplay.setCurrent(selectedItem);
		        })
		        .build();
	}

	public static void main(String[] args) {
		launch(args);
	}

    private static ObservableList<JapaneseLesson> getLessons() {
        return JapaneseLessonReader.getLessonsWait();
	}
}
 