package fxsamples;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.CommonsFX;

public class WorkingListsViews extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Creating and Working with Observable Lists");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);
        GridPane gridpane = new GridPane();
        gridpane.setVgap(10);
        gridpane.setHgap(10);
        gridpane.setPadding(new Insets(5));
        final int minWidth = 150;
        ColumnConstraints column1 = new ColumnConstraints(minWidth, minWidth, Double.MAX_VALUE);
        ColumnConstraints column2 = new ColumnConstraints(50);
        ColumnConstraints column3 = new ColumnConstraints(minWidth, minWidth, Double.MAX_VALUE);
        column1.setHgrow(Priority.ALWAYS);
        column3.setHgrow(Priority.ALWAYS);
        gridpane.getColumnConstraints().addAll(column1, column2, column3);
        // Candidates label
        Label candidatesLbl = new Label("Candidates");
        GridPane.setHalignment(candidatesLbl, HPos.CENTER);
        gridpane.add(candidatesLbl, 0, 0);
        // Heroes label
        Label heroesLbl = new Label("Heroes");
        gridpane.add(heroesLbl, 2, 0);
        GridPane.setHalignment(heroesLbl, HPos.CENTER);
        // Candidates
        final ObservableList<String> candidates = FXCollections.observableArrayList("Superman", "Spiderman",
            "Wolverine", "Police", "Fire Rescue", "Soldiers", "Dad & Mom", "Doctor", "Politician", "Pastor", "Teacher");
        final ListView<String> candidatesListView = new ListView<>(candidates);
        gridpane.add(candidatesListView, 0, 1);
        // heroes
        final ObservableList<String> heroes = FXCollections.observableArrayList();
        final ListView<String> heroListView = new ListView<>(heroes);
        gridpane.add(heroListView, 2, 1);
        // select heroes
        Button sendRightButton = CommonsFX.newButton(" > ", event -> {
            String potential = candidatesListView.getSelectionModel().getSelectedItem();
            if (potential != null) {
                candidatesListView.getSelectionModel().clearSelection();
                candidates.remove(potential);
                heroes.add(potential);
            }
        });
        // deselect heroes
        Button sendLeftButton = CommonsFX.newButton(" < ", e -> {
            String notHero = heroListView.getSelectionModel().getSelectedItem();
            if (notHero != null) {
                heroListView.getSelectionModel().clearSelection();
                heroes.remove(notHero);
                candidates.add(notHero);
            }
        });
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(sendRightButton, sendLeftButton);
        gridpane.add(vbox, 1, 1);
        root.setCenter(gridpane);
        GridPane.setVgrow(root, Priority.ALWAYS);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
