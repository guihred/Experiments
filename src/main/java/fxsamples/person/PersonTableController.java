package fxsamples.person;

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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PersonTableController extends Application {

    private static final double WIDTH = 500;
    private TextField filterField = new TextField();
    private TableView<Person> personTable = new TableView<>();
    private TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
    private TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");

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

    @Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Person Table");
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, WIDTH / 2, WIDTH, Color.WHITE);
        // create a grid pane
        FlowPane gridpane = new FlowPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        root.setCenter(gridpane);
        personTable.setPrefWidth(WIDTH / 2 - 10);
        gridpane.getChildren().addAll(new VBox(new Label("Filter Field"), filterField), personTable);
        personTable.getColumns().setAll(Arrays.asList(firstNameColumn, lastNameColumn));
        personTable.getColumns().forEach(e -> e.setPrefWidth(WIDTH / 7));
        initialize();
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initialize() {
        // 0. Initialize the columns.
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        // 1. Wrap the ObservableList in a FilteredList (initially display all
        // data).
        FilteredList<Person> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(person -> {
            // If filter text is empty, display all persons.
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            // Compare first name and last name of every person with
            // filter text.
            String lowerCaseFilter = newValue.toLowerCase();
            return person.getFirstName().toLowerCase().contains(lowerCaseFilter)
                || person.getLastName().toLowerCase().contains(lowerCaseFilter);
        }));

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<Person> sortedData = new SortedList<>(filteredData);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(personTable.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        personTable.setItems(sortedData);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
