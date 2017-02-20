package fxsamples;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Person {
	private StringProperty aliasName;
	private ObservableList<Person> employees = FXCollections
			.observableArrayList();
	private StringProperty firstName;
	private StringProperty lastName;

	public Person(String firstName, String lastName) {
		setFirstName(firstName);
		setLastName(lastName);
	}

	public Person(String alias, String firstName, String lastName) {
		setAliasName(alias);
		setFirstName(firstName);
		setLastName(lastName);
	}

	public StringProperty aliasNameProperty() {
		if (aliasName == null) {
			aliasName = new SimpleStringProperty();
		}
		return aliasName;
	}

	public ObservableList<Person> employeesProperty() {
		return employees;
	}

	public StringProperty firstNameProperty() {
		if (firstName == null) {
			firstName = new SimpleStringProperty();
		}
		return firstName;
	}

	public final String getAliasName() {
		return aliasNameProperty().get();
	}

	public final String getFirstName() {
		return firstNameProperty().get();
	}

	public final String getLastName() {
		return lastNameProperty().get();
	}

	public StringProperty lastNameProperty() {
		if (lastName == null) {
			lastName = new SimpleStringProperty();
		}
		return lastName;
	}

	public final void setAliasName(String value) {
		aliasNameProperty().set(value);
	}

	public final void setFirstName(String value) {
		firstNameProperty().set(value);
	}

	public final void setLastName(String value) {
		lastNameProperty().set(value);
	}
}