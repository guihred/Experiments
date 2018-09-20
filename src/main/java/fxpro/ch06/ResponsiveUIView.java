package fxpro.ch06;

import javafx.application.Platform;
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
import utils.HasLogging;
import utils.RunnableEx;

final class ResponsiveUIView implements HasLogging {

	private HBox buttonHBox;
	private Button changeFillButton;
	private Button changeStrokeButton;
	private Rectangle rectangle;
    private Scene scene;

    ResponsiveUIView(ResponsiveUIModel model) {
        rectangle = new Rectangle(200, 200);
        rectangle.setStrokeWidth(10);
		rectangle.fillProperty().bind(model.getFillPaint());
        rectangle.strokeProperty().bind(model.getStrokePaint());
        changeFillButton = new Button("Change Fill");
        changeStrokeButton = new Button("Change Stroke");
        buttonHBox = new HBox(10, changeFillButton, changeStrokeButton);
        buttonHBox.setPadding(new Insets(10, 10, 10, 10));
        buttonHBox.setAlignment(Pos.CENTER);
        final BorderPane borderPane = new BorderPane(rectangle, null, null, buttonHBox, null);
        borderPane.setPadding(new Insets(10, 10, 10, 10));

        scene = new Scene(borderPane);
		hookupEvents(model);
	}

	private void hookupEvents(ResponsiveUIModel model) {
		changeFillButton.setOnAction((ActionEvent actionEvent) -> {
			final Paint fillPaint = model.getFillPaint().get();
			model.getFillPaint().set(fillPaint.equals(Color.LIGHTGRAY) ? Color.GRAY : Color.LIGHTGRAY);
            new Thread(RunnableEx.makeRunnable(() -> {
				Thread.sleep(3000);
				Platform.runLater(() -> {
					final Rectangle rect = rectangle;
					double newArcSize = rect.getArcHeight() < 20 ? 30 : 0;
					rect.setArcWidth(newArcSize);
					rect.setArcHeight(newArcSize);
				});
			})).start();
		});
		changeStrokeButton.setOnAction(e -> model.getStrokePaint()
				.set(model.getStrokePaint().get().equals(Color.DARKGRAY) ? Color.BLACK : Color.DARKGRAY));
    }

	public Scene getScene() {
		return scene;
	}

}
