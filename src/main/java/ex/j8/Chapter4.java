package ex.j8;

import static javafx.beans.binding.Bindings.divide;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.multiply;

import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import simplebuilder.*;
import utils.ResourceFXUtils;
import utils.RunnableEx;
import utils.StageHelper;

public final class Chapter4 {
    private Chapter4() {
    }

    public static Circle buildSun(Scene scene) {
        Circle theSun = new SimpleCircleBuilder().radius(50).fill(Color.YELLOW).build();
        theSun.centerXProperty().bind(divide(scene.widthProperty(), 2));
        theSun.centerYProperty().bind(divide(scene.heightProperty(), 2));
        return theSun;
    }

    public static void createOrbitAnimation(Scene scene, Circle planet, Slider rotationSlider, Slider radiusSlider) {
        new SimplePathTransitionBuilder().duration(Duration.millis(1000)).interpolator(Interpolator.LINEAR).node(planet)
            .cycleCount(Animation.INDEFINITE).path(buildArc(scene, rotationSlider, radiusSlider)).build().play();
    }

    public  static void createPulseAnimation(Circle planet) {
        final double maxScale = 1.5;
        new SimpleScaleTransitionBuilder().byX(maxScale).byY(maxScale).cycleCount(Animation.INDEFINITE)
            .interpolator(Interpolator.LINEAR).duration(Duration.millis(500)).autoReverse(true).node(planet).build()
            .play();
    }

    public static void main(String[] args) {
        Application.launch(Ex9.class, args);
    }

    public static <T, U, R> ObservableValue<R> observe(BiFunction<T, U, R> f, ObservableValue<T> t,
        ObservableValue<U> u) {
        return new SimpleObjectProperty<R>() {
            @Override
            public void addListener(ChangeListener<? super R> listener) {
                t.addListener((o, old, value) -> listener.changed(this, f.apply(old, u.getValue()),
                    f.apply(value, u.getValue())));
                u.addListener((o, old, value) -> listener.changed(this, f.apply(t.getValue(), old),
                    f.apply(t.getValue(), value)));
            }

            @Override
            public void addListener(InvalidationListener listener) {
                t.addListener(listener);
                u.addListener(listener);
            }

            @Override
            public R getValue() {
                return f.apply(t.getValue(), u.getValue());
            }
        };
    }

	public static <T, R> ObservableValue<R> observe(Function<T, R> f, ObservableValue<T> t) {
        return new SimpleObjectProperty<R>() {
            @Override
            public void addListener(ChangeListener<? super R> listener) {
                t.addListener((arg0, arg1, arg2) -> listener.changed(this, f.apply(arg1), f.apply(arg2)));
            }

            @Override
            public void addListener(InvalidationListener arg0) {
                t.addListener(arg0);
            }

            @Override
            public R getValue() {
                return f.apply(t.getValue());
            }
        };
    }

	private static Arc buildArc(Scene scene, Slider rotationSlider, Slider radiusSlider) {
        final double scaleFactor = 3.6;
        final int startAngle = 45;
        final int radiusY = 150;
        return new SimpleArcBuilder().centerX(divide(scene.widthProperty(), 2)).radiusX(100).radiusY(radiusY)
            .startAngle(startAngle).length(360).centerY(divide(scene.heightProperty(), 2)).type(ArcType.CHORD)
            .stroke(Color.RED).strokeType(StrokeType.OUTSIDE).strokeLineCap(StrokeLineCap.ROUND).rotate(30D)
            .radiusX(multiply(radiusSlider.valueProperty(), scaleFactor)).strokeLineJoin(StrokeLineJoin.ROUND)
            .rotate(multiply(rotationSlider.valueProperty(), scaleFactor)).fill(Color.TRANSPARENT).build();
    }

	private static Circle buildPlanet(Scene scene) {
        final int defaultRadius = 25;
        Circle planet = new SimpleCircleBuilder().centerX(scene.getWidth() / 4 + 100).centerY(scene.getWidth() / 4)
            .fill(Color.BLUE).radius(defaultRadius).build();
        planet.centerXProperty().bind(divide(scene.widthProperty(), 2).add(100));
        planet.centerYProperty().bind(divide(scene.heightProperty(), 2));
        return planet;
    }

    /**
     * Write a program with a text field and a label. As with the Hello, JavaFX
     * program, the label should have the string Hello, FX in a 100 point font.
     * Initialize the text field with the same string. Update the label as the user
     * edits the text field.
     */
    public static class Ex1 extends Application {

        @Override
		public void start(Stage stage) {
            Label message = new Label("Hello, JavaFX!");
            message.setFont(new Font(100));
            TextField textField = new TextField("Hello, JavaFX!");
            message.textProperty().bind(textField.textProperty());
            VBox pane = new VBox(10);
            pane.getChildren().addAll(message, textField);
            stage.setScene(new Scene(pane));
            stage.setTitle("EX1");
            stage.show();
        }
    }

    /**
     * Using the web viewer, implement a browser with a URL bar and a back button.
     * Hint: WebEngine.getHistory().
     */

    public static class Ex10 extends Application {

        @Override
		public void start(Stage stage) {
            TextField textField = new TextField(ResourceFXUtils.toExternalForm("About.html"));
            WebView browser = new WebView();
            WebEngine engine = browser.getEngine();
            Button backButton = SimpleButtonBuilder.newButton("Back",
                event -> RunnableEx.ignore(() -> engine.getHistory().go(engine.getHistory().getCurrentIndex() - 1)));
            Button loadButton = new Button("Go");
            loadButton.setOnAction(event -> engine.load(textField.getText()));
            HBox top = new HBox();
            top.getChildren().addAll(textField, loadButton, backButton);
            BorderPane pane = new BorderPane();
            pane.setTop(top);
            pane.setCenter(browser);
            stage.setScene(new Scene(pane));
            stage.setTitle("EX10");
            stage.show();
        }
    }

    /**
     * Enhance the program in Section 4.5, Bindings, on page 75 so that the circle
     * stays centered and always touches at least two of the sides of the scene.s
     */
    public static class Ex4 extends Application {

        @Override
		public void start(Stage stage) {
            Circle circle = new Circle(100, 100, 100);
            circle.setFill(Color.RED);
            Pane pane = new Pane();
            Scene scene = new Scene(pane);
            pane.getChildren().add(circle);
            circle.centerXProperty().bind(divide(scene.widthProperty(), 2));
            circle.centerYProperty().bind(divide(scene.heightProperty(), 2));
            circle.radiusProperty().bind(min(divide(scene.widthProperty(), 2), divide(scene.heightProperty(), 2)));
            stage.setScene(scene);
            stage.setTitle("EX4");
            stage.show();
        }
    }

    /**
     * Write methods
     * 
     * public static <T, R> ObservableValue<R> observe( Function<T, R> f,
     * ObservableValue<T> t)
     * 
     * public static <T, U, R> ObservableValue<R> observe( BiFunction<T, U, R> f,
     * ObservableValue<T> t, ObservableValue<U> u)
     * 
     * that return observable values whose getValue method returns the value of the
     * lambda expression, and whose invalidation and change listeners are fired when
     * any of the inputs become invalid or change. For example,
     * 
     * larger.disableProperty().bind(observe( t -> t >= 100,
     * gauge.widthProperty()));
     */
    public static class Ex5 extends Application {

        @Override
		public void start(Stage stage) {
            Pane pane = new VBox();
            Scene scene = new Scene(pane);
            Slider slider = new Slider();
            Button button = new Button("Teste Disabled");
            Label message = new Label("Teste Disabled");

            button.disableProperty().bind(observe(t -> t.doubleValue() > 50, slider.valueProperty()));
            message.textProperty().bind(observe(t -> (t.doubleValue() > 50) + "", slider.valueProperty()));
            pane.getChildren().addAll(slider, button, message);
            stage.setScene(scene);
            stage.setTitle("EX5");
            stage.show();
        }

    }

    /** 6. Center the top and bottom buttons in Figure 4.7. */
    public static class Ex6 extends Application {

        @Override
		public void start(Stage stage) {
            BorderPane pane = new BorderPane(new Button("Center"), new Button("Top"), new Button("Right"),
                new Button("Bottom"), new Button("Left"));
            BorderPane.setAlignment(pane.getTop(), Pos.CENTER);
            BorderPane.setAlignment(pane.getBottom(), Pos.CENTER);
            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.setTitle("EX6");
            stage.show();
            StageHelper.displayCSSStyler(scene, "chapter4.css");
        }
    }

    /** Find out how to set the border of a control without using CSS. */
    public static class Ex7 extends Application {

        @Override
		public void start(Stage stage) {
            BorderPane pane = new BorderPane();
            pane.setCenter(new Button("Center"));
            Button top = new Button("Top");
            pane.setLeft(new Button("Left"));
            pane.setBottom(new Button("Bottom"));
            pane.setRight(new Button("Right"));
            Scene scene = new Scene(pane);
            top.setBorder(new Border(
                new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
            pane.setTop(top);

            stage.setScene(scene);
            stage.setTitle("EX7");
            stage.show();
        }
    }

    /**
     * Animate a circle, representing a planet, so it travels along an elliptical
     * orbit. Use a PathTransition.
     */

    public static class Ex9 extends Application {
        @Override
		public void start(Stage stage) {
            BorderPane pane = new BorderPane();
            Scene scene = new Scene(pane);
            Circle planet = buildPlanet(scene);
            Circle theSun = buildSun(scene);
            BorderPane center = new BorderPane();
            center.getChildren().addAll(theSun, planet);
            Slider rotationSlider = new Slider();
            rotationSlider.setOrientation(Orientation.VERTICAL);
            rotationSlider.setValue(10);
            Slider radiusSlider = new Slider();
            radiusSlider.setOrientation(Orientation.VERTICAL);
            radiusSlider.setValue(30);
            pane.setRight(new HBox(radiusSlider, rotationSlider));
            pane.setCenter(center);
            BorderPane.setAlignment(pane.getCenter(), Pos.CENTER);
            Button pulse = SimpleButtonBuilder.newButton("Pulse", e -> createPulseAnimation(planet));
            center.getChildren().add(buildArc(scene, rotationSlider, radiusSlider));
            Button orbit = SimpleButtonBuilder.newButton("Orbit",
                e -> createOrbitAnimation(scene, planet, rotationSlider, radiusSlider));
            pane.setBottom(new HBox(pulse, orbit));
            stage.setScene(scene);
            stage.setTitle("EX9");
            stage.show();
        }
    }

}
