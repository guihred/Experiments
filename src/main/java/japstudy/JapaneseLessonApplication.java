package japstudy;

import static utils.CommonsFX.onCloseWindow;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import simplebuilder.SimpleDialogBuilder;
import utils.CommonsFX;
import utils.HibernateUtil;

public class JapaneseLessonApplication extends Application {
    @FXML
    private TableView<JapaneseLesson> tableView1;

    public void initialize() {
        tableView1.setItems(JapaneseLessonReader.getLessonsWait());
    }

    public void onActionButton2() {
        JapaneseLessonDisplayer displayer = new JapaneseLessonDisplayer(tableView1.getItems());
        SimpleDialogBuilder.bindWindow(displayer, tableView1);
        displayer.show();
    }

    public void onMouseClickedTableView1(MouseEvent e) {
        if (e.getClickCount() > 1) {
            editItem(tableView1);
        }
    }

    @Override
	public void start(Stage primaryStage) {
        final int width = 600;
        final int height = 250;
        CommonsFX.loadFXML("Japanese Lesson Table Displayer", "JapaneseLessonApplication.fxml", this, primaryStage,
            width, height);
        primaryStage.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                editItem(tableView1);
            }
        });
        onCloseWindow(primaryStage, () -> HibernateUtil.shutdown());
    }

    private void editItem(final TableView<JapaneseLesson> lessonsTable) {
        TableViewSelectionModel<JapaneseLesson> selectionModel = lessonsTable.getSelectionModel();
        if (!selectionModel.isEmpty()) {
            showEditingDiplay(selectionModel.getSelectedItem());
        }
    }

    private void showEditingDiplay(JapaneseLesson selectedItem) {
        JapaneseLessonEditingDisplay japaneseLessonEditingDisplay = new JapaneseLessonEditingDisplay();
        Stage primaryStage = new Stage();
        SimpleDialogBuilder.bindWindow(primaryStage, tableView1);
        japaneseLessonEditingDisplay.start(primaryStage);
        japaneseLessonEditingDisplay.setCurrent(selectedItem);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
