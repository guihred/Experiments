package fxsamples;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class User {

	private static final String PASSWORD_PROP_NAME = "password";
	private static final String USERNAME_PROP_NAME = "user";
	private StringProperty userName = new ReadOnlyStringWrapper(this,
			USERNAME_PROP_NAME, "guilherme");
	private StringProperty password = new SimpleStringProperty(this,
			PASSWORD_PROP_NAME, "senha");

	public ObservableValue<String> userNameProperty() {
		return userName;
	}

	public StringProperty passwordProperty() {
		return password;
	}

	public String getUserName() {
		return userName.get();
	}

	public String getPassword() {
		return password.get();
	}

}
