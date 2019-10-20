package gaming.ex22;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class FreeCellActivity extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
		Canvas canvas = new Canvas();
		FreeCellView freeCellView = new FreeCellView();
		freeCellView.reset();
		freeCellView.onDraw(canvas);
		canvas.addEventHandler(MouseEvent.ANY, freeCellView::onTouchEvent);
		primaryStage.setScene(new Scene(new Group(canvas)));
		primaryStage.show();
    }

	public static void main(String[] args) {
		launch(args);
	}

}
