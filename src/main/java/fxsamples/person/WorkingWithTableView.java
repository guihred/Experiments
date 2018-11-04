package fxsamples.person;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleTableViewBuilder;

public class WorkingWithTableView extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage
                .setTitle("Chapter 4 Working with Tables");
		BorderPane root = new BorderPane();
		// create a grid pane
		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(10);
		gridpane.setVgap(10);
		root.setCenter(gridpane);
		// candidates label
		Label candidatesLbl = new Label("Boss");
		GridPane.setHalignment(candidatesLbl, HPos.CENTER);
		gridpane.add(candidatesLbl, 0, 0);
		// List of leaders
		ObservableList<Person> leaders = getPeople();
		final ListView<Person> leaderListView = new ListView<>(leaders);
		leaderListView.setPrefWidth(150);
		leaderListView.setMaxWidth(Double.MAX_VALUE);
		leaderListView.setPrefHeight(150);
		// display first and last name with tooltip using alias
		leaderListView
				.setCellFactory(param -> {
					Label leadLbl = new Label();
					Tooltip tooltip = new Tooltip();
					 // ListCell
					return new ListCell<Person>() {
						@Override
						public void updateItem(Person item, boolean empty) {
							super.updateItem(item, empty);
							if (item != null) {
								leadLbl.setText(item.getAliasName());
								setText(item.getFirstName() + " "
										+ item.getLastName());
								tooltip.setText(item.getAliasName());
								setTooltip(tooltip);
							}
						}
					};
				}); // setCellFactory
		gridpane.add(leaderListView, 0, 1);
		Label emplLbl = new Label("Employees");
		gridpane.add(emplLbl, 2, 0);
		GridPane.setHalignment(emplLbl, HPos.CENTER);
		final ObservableList<Person> teamMembers = FXCollections.observableArrayList();
		final TableView<Person> employeeTableView = new SimpleTableViewBuilder<Person>()
		        .prefWidth(300)
		        .items(teamMembers)
                .addColumn("Alias", "aliasName", true)
		        .addColumn("First Name", "firstName")
		        .addColumn("Last Name", "lastName")
		        .equalColumns()
		        .build();
		gridpane.add(employeeTableView, 2, 1);
		// selection listening
		leaderListView
				.getSelectionModel()
				.selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (observable != null && observable.getValue() != null) {
                        teamMembers.clear();
                        teamMembers.addAll(observable.getValue().employeesProperty());
                    }
                });
		Scene scene = new Scene(root, 500, 250, Color.WHITE);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private ObservableList<Person> getPeople() {
		ObservableList<Person> people = FXCollections
				.<Person> observableArrayList();
		Person docX = new Person("Professor X", "Charles", "Xavier");
		docX.employeesProperty().add(
				new Person("Wolverine", "James", "Howlett"));
		docX.employeesProperty().add(new Person("Cyclops", "Scott", "Summers"));
		docX.employeesProperty().add(new Person("Storm", "Ororo", "Munroe"));
		Person magneto = new Person("Magneto", "Max", "Eisenhardt");
		// ...code to add employees
		Person biker = new Person("Mountain Biker", "Jonathan", "Gennick");
		// ...code to add employees
		people.addAll(docX, magneto, biker);
		return people;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
