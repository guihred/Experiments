package fxpro.ch05;

import java.security.SecureRandom;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class TableVisualizationModel {
    private static final int LIST_SIZE = 10000;

    public static final ObservableList<String> CHOICE_BOX_ITEMS = FXCollections.observableArrayList(
            "Choice A",
            "Choice B",
            "Choice C",
            "Choice D"
    );

	public static final DoubleProperty KPH = new SimpleDoubleProperty(0);

	public static final ObservableList<String> LIST_VIEW_ITEMS = FXCollections.observableArrayList();

	public static final double MAX_KPH = 300.0;
	public static final double MAX_RPM = 8000.0;
	public static final DoubleProperty RPM = new SimpleDoubleProperty(0);

	public static final	SecureRandom secureRandom = new SecureRandom();

    private TableVisualizationModel() {
	}

    public static String getRandomWebSite() {
        String[] webSites = {
            "http://javafx.com",
            "http://fxexperience.com",
            "http://steveonjava.com",
            "http://javafxpert.com",
            "http://pleasingsoftware.blogspot.com",
            "http://www.weiqigao.com/blog",
            "http://google.com"
        };
        return webSites[secureRandom.nextInt(webSites.length)];
    }
	public static ObservableList<Person> getTeamMembers() {
        ObservableList<Person> teamMembers = FXCollections.observableArrayList();
		for (int i = 1; i <= 100; i++) {
            teamMembers.add(new Person("FirstName" + i,
                    "LastName" + i,
                    "Phone" + i));
        }
        return teamMembers;
    }

	public static void updateList(String newValue) {
        TableVisualizationModel.LIST_VIEW_ITEMS.clear();
        for (int i = 1; i <= LIST_SIZE; i++) {
            TableVisualizationModel.LIST_VIEW_ITEMS.add(newValue + " " + i);
        }
    }
}
