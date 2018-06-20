package ml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import simplebuilder.CommonsFX;
import simplebuilder.ResourceFXUtils;

public class MapGraph extends Application {
    private ObjectProperty<Country> currentCountry = new SimpleObjectProperty<>();

    @Override
    public void start(Stage stage) {
        // Setting the SVGPath in the form of string
        Group root = new Group();
        StackPane flowPane = new StackPane(root);
        Scene scene = new Scene(flowPane, 600, 300);
        flowPane.maxWidthProperty().bind(scene.widthProperty());
        stage.setMaximized(true);
        stage.setScene(scene);
        List<Text> texts = new ArrayList<>();
        WebView imageView = new WebView();
        imageView.setMaxWidth(96);
        imageView.setMaxHeight(72);
        CommonsFX.setZoomable(flowPane, true);
        List<SVGPath> countries = Stream.of(Country.values()).map(country -> {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(country.getPath());
            Text text = new Text();
            text.setVisible(false);
            text.setText(country.getCountryName());
            Color initialColor = country.getContinent() != null ? country.getContinent().getColor() : Color.BLACK;
            svgPath.setFill(initialColor);
            Color gray = Color.GRAY;
            svgPath.setStroke(gray);
            EventHandler<MouseEvent> value = ev -> {
                svgPath.setFill(Color.PURPLE);
                svgPath.setStroke(Color.RED);
                text.setVisible(true);
                svgPath.toFront();
                updateLayout(country, text);
                texts.forEach(Node::toFront);
                imageView.setLayoutX(country.getCenterX());
                imageView.setLayoutY(country.getCenterY());
                imageView.toFront();
                currentCountry.set(country);
            };
            svgPath.setOnMouseEntered(value);
            text.setOnMouseEntered(value);

            EventHandler<? super MouseEvent> value2 = o -> {
                if (!imageView.isVisible()) {
                    return;
                }
                svgPath.setFill(initialColor);
                svgPath.setStroke(gray);
                if (!svgPath.contains(o.getX(), o.getY())) {
                    text.setVisible(false);
                    currentCountry.set(null);
                }
            };
            svgPath.setOnMouseExited(value2);
            text.setOnMouseExited(value2);
            updateLayout(country, text);

            texts.add(text);
            return svgPath;
        }).collect(Collectors.toList());
        // Creating a Group object
        root.getChildren().addAll(imageView);
        root.getChildren().addAll(countries);
        root.getChildren().addAll(texts);
        currentCountry.addListener((obj, old, newV) -> {
            imageView.setVisible(newV != null || imageView.isHover());
            if (newV != null) {
                String url = "4x3/" + newV.getCode() + ".svg";
                File fullPath = ResourceFXUtils.toFile(url);
                if (fullPath.exists()) {
                    imageView.getEngine().load(ResourceFXUtils.toURL(url).toString());
                }
            }
        });
        imageView.setZoom(0.15);
        //        imageView.setOnScroll(e -> imageView.setZoom(imageView.getZoom() + (e.getDeltaY() < 0 ? -0.01 : 0.01)));
        // Creating a scene object
        // Setting title to the Stage
        stage.setTitle("Drawing the world");
        // Adding scene to the stage
        // Displaying the contents of the stage
        stage.show();
    }

    private void updateLayout(Country country, Text text) {
        text.setLayoutX(country.getCenterX() - text.getBoundsInParent().getWidth() / 2);
        text.setLayoutY(country.getCenterY() - text.getBoundsInParent().getHeight());
    }


    public static void main(String[] args) {
        launch(args);
    }

}