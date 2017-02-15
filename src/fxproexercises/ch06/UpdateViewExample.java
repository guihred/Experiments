/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

/**
 *
 * @author Note
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class UpdateViewExample extends Application {

	private static final class Model {

		protected final ObjectProperty<Paint> fillPaint = new SimpleObjectProperty<>();
		protected final ObjectProperty<Paint> strokePaint = new SimpleObjectProperty<>();

        private Model() {
            fillPaint.set(Color.LIGHTGRAY);
            strokePaint.set(Color.DARKGRAY);
        }
    }

	private static final class View {

        public HBox buttonHBox;
        public Button changeFillButton;
        public Button changeStrokeButton;
        public Rectangle rectangle;
        public Scene scene;

        private View(Model model) {
            rectangle = new Rectangle(200, 200);
            rectangle.setStrokeWidth(10);
			rectangle.fillProperty().bind(model.fillPaint);
            rectangle.strokeProperty().bind(model.strokePaint);
            changeFillButton = new Button("Change Fill");
            changeStrokeButton = new Button("Change Stroke");
            buttonHBox = new HBox(10, changeFillButton, changeStrokeButton);
            buttonHBox.setPadding(new Insets(10, 10, 10, 10));
            buttonHBox.setAlignment(Pos.CENTER);
            final BorderPane borderPane = new BorderPane(rectangle, null, null, buttonHBox, null);
            borderPane.setPadding(new Insets(10, 10, 10, 10));

            scene = new Scene(borderPane);
        }
    }

    private final Model model;

    private View view;

    public UpdateViewExample() {
        model = new Model();
    }

    private void hookupEvents() {
		view.changeFillButton.setOnAction((ActionEvent actionEvent) -> {
            final Paint fillPaint = model.fillPaint.get();
			model.fillPaint.set(fillPaint.equals(Color.LIGHTGRAY) ? Color.GRAY : Color.LIGHTGRAY);
			new Thread(() -> {
				try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        final Rectangle rect = view.rectangle;
                        double newArcSize
                                = rect.getArcHeight() < 20 ? 30 : 0;
                        rect.setArcWidth(newArcSize);
                        rect.setArcHeight(newArcSize);
                    });
                } catch (InterruptedException e) {
                }
			}).start();
        });
		view.changeStrokeButton.setOnAction(e -> model.strokePaint
				.set(model.strokePaint.get().equals(Color.DARKGRAY) ? Color.BLACK : Color.DARKGRAY));
    }

    @Override
    public void start(Stage stage) throws Exception {
        view = new View(model);
        hookupEvents();
        stage.setTitle("Unresponsive UI Example");
        stage.setScene(view.scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
