package fxsamples.person;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Person {
	private static final String PASS_PROP_NAME = "password";
	private static final String USERNAME_PROP_NAME = "user";
	private StringProperty aliasName;
	private ObservableList<Person> employees = FXCollections
			.observableArrayList();
    private StringProperty firstName;
    private StringProperty lastName;
    private StringProperty userName = new ReadOnlyStringWrapper(this, USERNAME_PROP_NAME, "guilherme");
    private StringProperty password = new SimpleStringProperty(this, PASS_PROP_NAME, "senha");

    public Person() {

    }

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

	public String getPassword() {
        return password.get();
    }

	public String getUserName() {
        return userName.get();
    }

	public StringProperty lastNameProperty() {
		if (lastName == null) {
			lastName = new SimpleStringProperty();
		}
		return lastName;
	}

	public StringProperty passwordProperty() {
        return password;
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

	public ObservableValue<String> userNameProperty() {
        return userName;
    }
}