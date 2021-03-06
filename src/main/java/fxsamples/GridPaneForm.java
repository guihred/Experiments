package fxsamples;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GridPaneForm extends Application {
    private static final ColumnConstraints COLUMN_2 = new ColumnConstraints(50, 150, 300);
	@Override
	public void start(Stage primaryStage) {
        primaryStage.setTitle("Grid Pane Form");
		BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
		GridPane gridpane = new GridPane();
		gridpane.setHgap(5);
		gridpane.setPadding(new Insets(5));
		gridpane.setVgap(5);
		ColumnConstraints column1 = new ColumnConstraints(100);
        COLUMN_2.setHgrow(Priority.ALWAYS);
        gridpane.getColumnConstraints().addAll(column1, COLUMN_2);
		Label fNameLbl = new Label("First Name");
		TextField fNameFld = new TextField();
		Label lNameLbl = new Label("Last Name");
		TextField lNameFld = new TextField();
		Button saveButton = new Button("Save");
		// First name label
		GridPane.setHalignment(fNameLbl, HPos.RIGHT);
		gridpane.add(fNameLbl, 0, 0);
		// Last name label
		GridPane.setHalignment(lNameLbl, HPos.RIGHT);
		gridpane.add(lNameLbl, 0, 1);
		// First name field
		GridPane.setHalignment(fNameFld, HPos.LEFT);
		gridpane.add(fNameFld, 1, 0);
		// Last name field
		GridPane.setHalignment(lNameFld, HPos.LEFT);
		gridpane.add(lNameFld, 1, 1);
		// Save button
		GridPane.setHalignment(saveButton, HPos.RIGHT);
		gridpane.add(saveButton, 1, 2);
		FlowPane topBanner = new FlowPane();
        final int prefHeight = 40;
        topBanner.setPrefHeight(prefHeight);
		String backgroundStyle = "-fx-background-color: lightblue;"
            + "-fx-background-radius: 30%;-fx-background-inset: 5px;";
		topBanner.setStyle(backgroundStyle);
		SVGPath svgIcon = new SVGPath();
		// icon from http://raphaeljs.com/icons/#people
        svgIcon.setContent(
            "M24.778,21.419 19.276,15.917 24.777,10.415 21.949,7.585 16.447,13.087 "
                + "10.945,7.585 8.117,10.415 13.618,15.917 8.116,21.419 10.946,24.248 "
                + "16.447,18.746 21.948,24.248z");
		svgIcon.setStroke(Color.LIGHTGRAY);
		svgIcon.setFill(Color.WHITE);
		Text contactText = new Text("Contacts");
		contactText.setFill(Color.WHITE);
		Font serif = Font.font("Dialog", 30);
		contactText.setFont(serif);
		topBanner.getChildren().addAll(svgIcon, contactText);
		root.setTop(topBanner);
		root.setCenter(gridpane);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
