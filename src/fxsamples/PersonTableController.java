package fxsamples;

import java.util.Arrays;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PersonTableController extends Application {

	private TextField filterField = new TextField();
	private TableView<Person> personTable = new TableView<Person>();
	private TableColumn<Person, String> firstNameColumn = new TableColumn<Person, String>("First Name");
	private TableColumn<Person, String> lastNameColumn = new TableColumn<Person, String>("Last Name");

	private ObservableList<Person> masterData = FXCollections.observableArrayList();

	/**
	 * Just add some sample data in the constructor.
	 */
	public PersonTableController() {
		masterData.add(new Person("Hans", "Muster"));
		masterData.add(new Person("Ruth", "Mueller"));
		masterData.add(new Person("Heinz", "Kurz"));
		masterData.add(new Person("Cornelia", "Meier"));
		masterData.add(new Person("Werner", "Meyer"));
		masterData.add(new Person("Lydia", "Kunz"));
		masterData.add(new Person("Anna", "Best"));
		masterData.add(new Person("Stefan", "Meier"));
		masterData.add(new Person("Martin", "Mueller"));
	}

	private void initialize() {
		// 0. Initialize the columns.
		firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
		lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

		// 1. Wrap the ObservableList in a FilteredList (initially display all
		// data).
		FilteredList<Person> filteredData = new FilteredList<>(masterData, p -> true);

		// 2. Set the filter Predicate whenever the filter changes.
		filterField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(person -> {
				// If filter text is empty, display all persons.
					if (newValue == null || newValue.isEmpty()) {
						return true;
					}

					// Compare first name and last name of every person with
					// filter text.
					String lowerCaseFilter = newValue.toLowerCase();

					if (person.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
						return true; // Filter matches first name.
					} else if (person.getLastName().toLowerCase().contains(lowerCaseFilter)) {
						return true; // Filter matches last name.
					}
					return false; // Does not match.
				});
		});

		// 3. Wrap the FilteredList in a SortedList.
		SortedList<Person> sortedData = new SortedList<>(filteredData);

		// 4. Bind the SortedList comparator to the TableView comparator.
		sortedData.comparatorProperty().bind(personTable.comparatorProperty());

		// 5. Add sorted (and filtered) data to the table.
		personTable.setItems(sortedData);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("PersonFilter");
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 600, 250, Color.WHITE);
		// create a grid pane
		FlowPane gridpane = new FlowPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(10);
		gridpane.setVgap(10);
		root.setCenter(gridpane);

		gridpane.getChildren().addAll(new Label("Filter Field"), filterField, personTable);
		this.personTable.getColumns().setAll(Arrays.asList(firstNameColumn, lastNameColumn));
		initialize();
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}

