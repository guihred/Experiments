package fxpro.ch06;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

class ThreadInformationView {

    private Scene scene;
    private final TextArea stackTrace = new TextArea();
    private final ListView<String> threadNames;
    private final Button updateButton;

    public ThreadInformationView(ThreadInformationModel model) {
        threadNames = new ListView<>(model.getThreadNames());
        updateButton = new Button("Update");
        final VBox vbox = new VBox(10, threadNames, stackTrace, updateButton);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        scene = new Scene(vbox);
        hookupEvents(model);
    }

    public Scene getScene() {
        return scene;
    }

    private void hookupEvents(ThreadInformationModel model) {
        updateButton.setOnAction(e -> model.update());
        threadNames.getSelectionModel().selectedIndexProperty().addListener(
            (o, oldValue, newValue) -> stackTrace.setText(model.getStackTraces().get(newValue.intValue())));
    }

}