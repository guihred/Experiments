package fxpro.ch05;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Note
 */
public final class SimplePerson {

    private StringProperty firstName;

    private StringProperty lastName;

    private StringProperty phone;

    public SimplePerson(String firstName, String lastName, String phone) {
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
    }
    public StringProperty firstNameProperty() {
        if (firstName == null) {
            firstName = new SimpleStringProperty(this, "firstName");
        }
        return firstName;
    }

    public String getFirstName() {
        return firstNameProperty().get();
    }

    public String getLastName() {
        return lastNameProperty().get();
    }

    public String getPhone() {
        return phoneProperty().get();
    }
    public StringProperty lastNameProperty() {
        if (lastName == null) {
            lastName = new SimpleStringProperty(this, "lastName");
        }
        return lastName;
    }

    public StringProperty phoneProperty() {
        if (phone == null) {
            phone = new SimpleStringProperty(this, "phone");
        }
        return phone;
    }

    public void setFirstName(String value) {
        firstNameProperty().set(value);
    }

    public void setLastName(String value) {
        lastNameProperty().set(value);
    }

    public void setPhone(String value) {
        phoneProperty().set(value);
    }

    @Override
    public String toString() {
        return "Person: " + firstName.getValue() + " " + lastName.getValue();
    }
}
