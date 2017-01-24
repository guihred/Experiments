package fxproexercises.ch05;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Note
 */
public final class Person {

    private StringProperty firstName;

    public void setFirstName(String value) {
        firstNameProperty().set(value);
    }

    public String getFirstName() {
        return firstNameProperty().get();
    }

    public StringProperty firstNameProperty() {
        if (firstName == null) {
            firstName = new SimpleStringProperty(this, "firstName");
        }
        return firstName;
    }
    private StringProperty lastName;

    public void setLastName(String value) {
        lastNameProperty().set(value);
    }

    public String getLastName() {
        return lastNameProperty().get();
    }

    public StringProperty lastNameProperty() {
        if (lastName == null) {
            lastName = new SimpleStringProperty(this, "lastName");
        }
        return lastName;
    }
    private StringProperty phone;

    public void setPhone(String value) {
        phoneProperty().set(value);
    }

    public String getPhone() {
        return phoneProperty().get();
    }

    public StringProperty phoneProperty() {
        if (phone == null) {
            phone = new SimpleStringProperty(this, "phone");
        }
        return phone;
    }

    public Person(String firstName, String lastName, String phone) {
        setFirstName(firstName);
        setLastName(lastName);
        setPhone(phone);
    }

    @Override
    public String toString() {
        return "Person: " + firstName.getValue() + " " + lastName.getValue();
    }
}
