package ml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MapGraph extends Application {

    @Override
    public void start(Stage stage) {
        // Setting the SVGPath in the form of string
        Group root = new Group();
        StackPane flowPane = new StackPane(root);
        Scene scene = new Scene(flowPane, 600, 300);
        flowPane.maxWidthProperty().bind(scene.widthProperty());
        //        stage.setMaximized(true);
        stage.setScene(scene);
        List<Text> texts = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        List<SVGPath> countries = Stream.of(Country.values()).map(country -> {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(country.getPath());
            Line line = new Line();
            Text text = new Text();
            ReadOnlyObjectProperty<Bounds> bounds = text.boundsInParentProperty();
            text.setOnMouseDragged(ev -> {
                text.setLayoutX(ev.getSceneX());
                text.setLayoutY(ev.getSceneY());
            });
            text.setVisible(true);
            text.setText(country.getCountryName());
            Color initialColor = country.getContinent() != null ? country.getContinent().getColor() : Color.BLACK;
            svgPath.setFill(initialColor);
            Color gray = Color.GRAY;
            svgPath.setStroke(gray);
            EventHandler<? super MouseEvent> value = o -> {
                svgPath.setFill(Color.PURPLE);
                svgPath.setStroke(Color.RED);
                text.setVisible(true);
                svgPath.toFront();
                lines.forEach(Node::toFront);
                texts.forEach(Node::toFront);
            };
            svgPath.setOnMouseEntered(value);
            text.setOnMouseEntered(value);
            EventHandler<? super MouseEvent> value2 = o -> {
                svgPath.setFill(initialColor);
                svgPath.setStroke(gray);
                //                text.setVisible(false)
            };
            svgPath.setOnMouseExited(value2);
            text.setOnMouseExited(value2);
            text.setLayoutX(country.getCenterX() - text.getBoundsInParent().getWidth() / 2);
            text.setLayoutY(country.getCenterY());
            line.startXProperty().bind(Bindings.createDoubleBinding(country::getCenterX, bounds));
            line.startYProperty().bind(Bindings.createDoubleBinding(country::getCenterY, bounds));
            line.endXProperty().bind(Bindings.createDoubleBinding(() -> minDistance(line, bounds), bounds));
            line.endYProperty().bind(Bindings.createDoubleBinding(() -> minDistanceY(line, bounds), bounds));
            line.visibleProperty().bind(Bindings.createBooleanBinding(() -> !svgPath.getBoundsInParent().contains(bounds.get()),
                    svgPath.boundsInParentProperty(), bounds));
            texts.add(text);
            lines.add(line);
            return svgPath;
        }).collect(Collectors.toList());
        // Creating a Group object
        root.getChildren().addAll(countries);
        root.getChildren().addAll(lines);
        root.getChildren().addAll(texts);
        //        root.setScaleX(0.5);
        //        root.setScaleY(0.5);
        //        root.setTranslateX(-500);
        //        root.setTranslateY(-200);
        // Creating a scene object
        // Setting title to the Stage
        stage.setTitle("Drawing the world");
        // Adding scene to the stage
        // Displaying the contents of the stage
        stage.show();
    }

    private double minDistance(Line line, ReadOnlyObjectProperty<Bounds> bounds) {
        Bounds bounds2 = bounds.get();
        return DoubleStream
                .of(bounds2.getMinX(), bounds2.getMinX() + bounds2.getWidth() / 2, bounds2.getMaxX()).boxed()
                .min(Comparator.comparing(e -> Math.abs(line.getStartX() - e))).orElse(bounds2.getMinX());
    }

    private double minDistanceY(Line line, ReadOnlyObjectProperty<Bounds> bounds) {
        Bounds bounds2 = bounds.get();
        return DoubleStream.of(bounds2.getMinY(), bounds2.getMinY() + bounds2.getHeight() / 2, bounds2.getMaxY())
                .boxed().min(Comparator.comparing(e -> Math.abs(line.getStartY() - e))).orElse(bounds2.getMinY());
    }

    public static void main(String[] args) {
        launch(args);
    }

}