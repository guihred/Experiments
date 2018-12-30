package ex.j8;

import static javafx.beans.binding.Bindings.divide;
import static javafx.beans.binding.Bindings.min;
import static javafx.beans.binding.Bindings.multiply;

import java.util.function.BiFunction;
import java.util.function.Function;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.animation.ScaleTransition;
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
import simplebuilder.SimpleArcBuilder;
import utils.ResourceFXUtils;

public final class Chapter4 {
	private Chapter4() {
	}

	public static void main(String[] args) {
        Application.launch(Ex10.class, args);
	}

	/**
	 * Write a program with a text field and a label. As with the Hello, JavaFX
	 * program, the label should have the string Hello, FX in a 100 point font.
	 * Initialize the text field with the same string. Update the label as the
	 * user edits the text field.
	 */
	public static class Ex1 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
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
	 * Using the web viewer, implement a browser with a URL bar and a back
	 * button. Hint: WebEngine.getHistory().
	 */

	public static class Ex10 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
            TextField textField = new TextField(ResourceFXUtils.toExternalForm("About.html"));
			WebView browser = new WebView();
			WebEngine engine = browser.getEngine();
			Button backButton = new Button("Back");
			backButton.setOnAction(event -> engine.getHistory().go(engine.getHistory().getCurrentIndex() - 1));
			Button loadButton = new Button("Go");
			loadButton.setOnAction(event -> engine.load(textField.getText()));

			HBox top = new HBox();
			top.getChildren().addAll(backButton, textField, loadButton);
			BorderPane pane = new BorderPane();
			pane.setTop(top);
			pane.setCenter(browser);
			stage.setScene(new Scene(pane));
			stage.setTitle("EX10");
			stage.show();
		}
	}

    /**
	 * Enhance the program in Section 4.5, Bindings, on page 75 so that the
	 * circle stays centered and always touches at least two of the sides of the
	 * scene.s
	 */
	public static class Ex4 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
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
	 * public static <T, U, R> ObservableValue<R> observe( BiFunction<T, U, R>
	 * f, ObservableValue<T> t, ObservableValue<U> u)
	 * 
	 * that return observable values whose getValue method returns the value of
	 * the lambda expression, and whose invalidation and change listeners are
	 * fired when any of the inputs become invalid or change. For example,
	 * 
	 * larger.disableProperty().bind(observe( t -> t >= 100,
	 * gauge.widthProperty()));
	 */
	public static class Ex5 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
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

		public static <T, U, R> ObservableValue<R> observe(BiFunction<T, U, R> f, ObservableValue<T> t, ObservableValue<U> u) {
			return new SimpleObjectProperty<R>() {
				@Override
				public void addListener(ChangeListener<? super R> listener) {
					t.addListener((observable, oldValue, newValue) -> listener.changed(this, f.apply(oldValue, u.getValue()),f.apply(newValue, u.getValue())));
					u.addListener((observable, oldValue, newValue) -> listener.changed(this, f.apply(t.getValue(), oldValue),f.apply(t.getValue(), newValue)));
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
					t.addListener((arg0, arg1, arg2) -> listener.changed(this,
							f.apply(arg1), f.apply(arg2)));
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
	}

	/** 6. Center the top and bottom buttons in Figure 4.7. */
	public static class Ex6 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
			BorderPane pane = new BorderPane(new Button("Center"),new Button("Top"),new Button("Right"),new Button("Bottom"),new Button("Left"));
			BorderPane.setAlignment(pane.getTop(), Pos.CENTER);
			BorderPane.setAlignment(pane.getBottom(), Pos.CENTER);
			Scene scene = new Scene(pane);
			stage.setScene(scene);
			stage.setTitle("EX6");
			stage.show();
		}
	}

	/** Find out how to set the border of a control without using CSS. */
	public static class Ex7 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
			BorderPane pane = new BorderPane();
			pane.setCenter(new Button("Center"));
			Button top = new Button("Top");
			pane.setLeft(new Button("Left"));
			pane.setBottom(new Button("Bottom"));
			pane.setRight(new Button("Right"));
			Scene scene = new Scene(pane);
			top.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
			pane.setTop(top);

			stage.setScene(scene);
			stage.setTitle("EX7");
			stage.show();
		}
	}

	/**
	 * Animate a circle, representing a planet, so it travels along an
	 * elliptical orbit. Use a PathTransition.
	 */

	public static class Ex9 extends Application {

		@Override
		public void start(Stage stage) throws Exception {
			BorderPane pane = new BorderPane();
			Scene scene = new Scene(pane);
            Circle planet = new Circle(scene.getWidth() / 4 + 100, scene.getWidth() / 4, 100 / 4);
			planet.setFill(Color.BLUE);
			planet.centerXProperty().bind(divide(scene.widthProperty(), 2).add(100));
			planet.centerYProperty().bind(divide(scene.heightProperty(), 2));
			Circle theSun = new Circle(50);
			theSun.setFill(Color.YELLOW);
			theSun.centerXProperty().bind(divide(scene.widthProperty(), 2));
			theSun.centerYProperty().bind(divide(scene.heightProperty(), 2));
			BorderPane center = new BorderPane();
			center.getChildren().addAll(theSun, planet);

			HBox right = new HBox();
			Slider rotationSlider = new Slider();
			rotationSlider.setOrientation(Orientation.VERTICAL);
			rotationSlider.setValue(10);

			Slider radiusSlider = new Slider();
			radiusSlider.setOrientation(Orientation.VERTICAL);
			radiusSlider.setValue(30);
			right.getChildren().addAll(radiusSlider, rotationSlider);

			pane.setRight(right);
			pane.setCenter(center);
			BorderPane.setAlignment(pane.getCenter(), Pos.CENTER);


			Button pulse = new Button("Pulse");
			pulse.setOnAction(event -> {
				ScaleTransition st = new ScaleTransition(Duration.millis(500));
                final double maxScale = 1.5;
                st.setByX(maxScale);
                st.setByY(maxScale);
				st.setCycleCount(Animation.INDEFINITE);
				st.setInterpolator(Interpolator.LINEAR);
				st.setAutoReverse(true);
				st.setNode(planet);
				st.play();
			});
			Button orbit = new Button("Orbit");

            final double scaleFactor = 3.6;
            final int startAngle = 45;
            final Arc arc = new SimpleArcBuilder().centerX(divide(scene.widthProperty(), 2)).radiusX(100).radiusY(150)
                    .startAngle(startAngle).length(360).centerY(divide(scene.heightProperty(), 2)).type(ArcType.CHORD)
					.stroke(Color.RED).strokeType(StrokeType.OUTSIDE).strokeLineCap(StrokeLineCap.ROUND).rotate(30D)
                    .radiusX(multiply(radiusSlider.valueProperty(), scaleFactor)).strokeLineJoin(StrokeLineJoin.ROUND)
                    .rotate(multiply(rotationSlider.valueProperty(), scaleFactor)).fill(Color.TRANSPARENT).build();
			center.getChildren().add(arc);
			orbit.setOnAction(event -> {
				PathTransition st = new PathTransition(Duration.millis(1000), arc, center.getCenter());
				st.setCycleCount(Animation.INDEFINITE);
				st.setInterpolator(Interpolator.LINEAR);
				st.setNode(planet);
				st.play();
			});


			HBox hBox = new HBox();
			hBox.getChildren().addAll(pulse, orbit);
			pane.setBottom(hBox);
			stage.setWidth(0.5);
			stage.setHeight(0.5);

			stage.setScene(scene);
			stage.setTitle("EX9");
			stage.show();
		}
	}

}

