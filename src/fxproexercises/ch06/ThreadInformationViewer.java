/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class ThreadInformationViewer extends Application
        implements EventHandler<ActionEvent>, ChangeListener<Number> {

    public static class Model {

		public ObservableList<String> stackTraces = FXCollections.observableArrayList();
		public ObservableList<String> threadNames = FXCollections.observableArrayList();

        public Model() {
            update();
        }

        private String formatStackTrace(StackTraceElement[] value) {
			return Stream.of(value).map(StackTraceElement::toString).collect(Collectors.joining("\n at ", "StackTrace: ", ""));
        }

        public final void update() {
            threadNames.clear();
            stackTraces.clear();
            final Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            map.forEach((k, v) -> {
                threadNames.add("\"" + k.getName() + "\"");
                stackTraces.add(formatStackTrace(v));
            });
        }
    }

	private static final class View {

        public Scene scene;
        public TextArea stackTrace;
        public ListView<String> threadNames;
        public Button updateButton;

        private View(Model model) {
            threadNames = new ListView<>(model.threadNames);
            stackTrace = new TextArea();
            updateButton = new Button("Update");
            final VBox vbox = new VBox(10, threadNames, stackTrace, updateButton);
            vbox.setPadding(new Insets(10, 10, 10, 10));
            scene = new Scene(vbox);
        }
    }

    private final Model model;

    private View view;

    public ThreadInformationViewer() {
        model = new Model();
    }

    @Override
    public void changed(ObservableValue<? extends Number> observableValue,
            Number oldValue, Number newValue) {
        int index = (Integer) newValue;
        if (index >= 0) {
            view.stackTrace.setText(model.stackTraces.get(index));
        }
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        model.update();
    }

    private void hookupEvents() {
        view.updateButton.setOnAction(this);
        view.threadNames.getSelectionModel().selectedIndexProperty().addListener(this);
    }

    @Override
    public void start(Stage stage) throws Exception {
        view = new View(model);
        hookupEvents();
        stage.setTitle("JavaFX Threads Information");
        stage.setScene(view.scene);

        stage.setWidth(440);
        stage.setHeight(640);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
