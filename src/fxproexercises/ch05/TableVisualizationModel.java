package fxproexercises.ch05;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TableVisualizationModel {

    public ObservableList<Person> getTeamMembers() {
        ObservableList<Person> teamMembers = FXCollections.observableArrayList();
		for (int i = 1; i <= 100; i++) {
            teamMembers.add(new Person("FirstName" + i,
                    "LastName" + i,
                    "Phone" + i));
        }
        return teamMembers;
    }

    public String getRandomWebSite() {
        String[] webSites = {
            "http://javafx.com",
            "http://fxexperience.com",
            "http://steveonjava.com",
            "http://javafxpert.com",
            "http://pleasingsoftware.blogspot.com",
            "http://www.weiqigao.com/blog",
            "http://google.com"
        };
        int randomIdx = (int) (Math.random() * webSites.length);
        return webSites[randomIdx];
    }

    public ObservableList<String> listViewItems = FXCollections.observableArrayList();
    public ObservableList<String> choiceBoxItems = FXCollections.observableArrayList(
            "Choice A",
            "Choice B",
            "Choice C",
            "Choice D"
    );
    public double maxRpm = 8000.0;
    public DoubleProperty rpm = new SimpleDoubleProperty(0);
    public double maxKph = 300.0;
    public DoubleProperty kph = new SimpleDoubleProperty(0);
}
