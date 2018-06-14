package ml;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MapGraph extends Application {


    @Override
	public void start(Stage stage) {
		Text text = new Text();
		text.setVisible(false);
		// Setting the SVGPath in the form of string
		List<SVGPath> collect = Stream.of(Countries.values()).map(e -> {
			SVGPath svgPath = new SVGPath();
			svgPath.setContent(e.getPath());
			Color initialColor = Color.BLUE;
			svgPath.setFill(initialColor);
			Color gray = Color.BLACK;
			svgPath.setStroke(gray);
			svgPath.setOnMouseEntered(o -> {
				svgPath.setFill(Color.GREEN);
				svgPath.setStroke(Color.RED);
				Bounds boundsInParent = svgPath.getBoundsInParent();
				text.setLayoutX(boundsInParent.getMinX() + boundsInParent.getWidth() / 2);
				text.setLayoutY(boundsInParent.getMinY() + boundsInParent.getHeight() / 2);
				text.setText(e.getCountryName());
				text.setVisible(true);
			});
			svgPath.setOnMouseExited(o -> {
				svgPath.setFill(initialColor);
				svgPath.setStroke(gray);
				text.setVisible(false);
			});
			return svgPath;
		}).collect(Collectors.toList());

		// Creating a Group object
		Group root = new Group();
		root.getChildren().addAll(collect);
		root.getChildren().add(text);
		// Creating a scene object
		FlowPane flowPane = new FlowPane(root);
		Scene scene = new Scene(flowPane, 600, 300);
		flowPane.maxWidthProperty().bind(scene.widthProperty());
		// Setting title to the Stage
		stage.setTitle("Drawing the world");
		// Adding scene to the stage
		stage.setScene(scene);
		stage.setMaximized(true);
		// Displaying the contents of the stage
		stage.show();
	}
    public static void main(String[] args) {
        launch(args);
    }

}