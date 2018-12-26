package ml.graph;

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
import ml.data.Country;
import utils.ResourceFXUtils;
import utils.RotateUtils;

public class MapGraph extends Application {
    private static final int FLAG_HEIGHT = 72;
    private static final int FLAG_WIDTH = 96;
    private ObjectProperty<Country> currentCountry = new SimpleObjectProperty<>();

    @Override
    public void start(Stage stage) {
        // Setting the SVGPath in the form of string
        Group root = new Group();
        StackPane flowPane = new StackPane(root);
        Scene scene = new Scene(flowPane);
        flowPane.maxWidthProperty().bind(scene.widthProperty());
        stage.setMaximized(true);
        stage.setScene(scene);
        List<Text> texts = new ArrayList<>();
        WebView flagImage = new WebView();
        flagImage.setMaxWidth(FLAG_WIDTH);
        flagImage.setMaxHeight(FLAG_HEIGHT);
        RotateUtils.setZoomable(flowPane, true);
        List<SVGPath> countries = Stream.of(Country.values())
                .map(country -> mapCountryToPath(texts, flagImage, country))
                .collect(Collectors.toList());
        // Creating a Group object
        root.getChildren().addAll(flagImage);
        root.getChildren().addAll(countries);
        root.getChildren().addAll(texts);
        currentCountry.addListener((obj, old, newV) -> {
            flagImage.setVisible(newV != null || flagImage.isHover());
            if (newV != null) {
                String url = "4x3/" + newV.getCode() + ".svg";
                File fullPath = ResourceFXUtils.toFile(url);
                if (fullPath.exists()) {
                    flagImage.getEngine().load(ResourceFXUtils.toURL(url).toString());
                }
            }
        });
        flagImage.setZoom(3. / 20);
        stage.setTitle("Map Graph Example");
        stage.show();
    }

    private SVGPath mapCountryToPath(List<Text> texts, WebView imageView, Country country) {
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
            text.setVisible(false);
            if (!svgPath.contains(o.getX(), o.getY())) {
                currentCountry.set(null);
            }
        };
        svgPath.setOnMouseExited(value2);
        text.setOnMouseExited(value2);
        updateLayout(country, text);

        texts.add(text);
        return svgPath;
    }

    private void updateLayout(Country country, Text text) {
        text.setLayoutX(country.getCenterX() - text.getBoundsInParent().getWidth() / 2);
        text.setLayoutY(country.getCenterY() - text.getBoundsInParent().getHeight());
    }


    public static void main(String[] args) {
        launch(args);
    }

}