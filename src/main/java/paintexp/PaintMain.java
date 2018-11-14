package paintexp;

import graphs.entities.ZoomableScrollPane;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import simplebuilder.SimpleMenuBarBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CrawlerTask;
import utils.HasLogging;

public class PaintMain extends  Application{

	private static final Logger LOG = HasLogging.log();
    private PaintModel paintModel = new PaintModel();
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        root.setTop(new SimpleMenuBarBuilder()
                .addMenu("_File")
				.addMenuItem("_New", e -> newFile())
				.addMenuItem("_Open", e -> openFile(primaryStage))
                .addMenu("_Edit")
                .addMenu("_View")
                .addMenu("_Image")
                .addMenu("_Colors")
                .addMenu("_Help")
                .build());
        PixelReader reader = new SimplePixelReader(paintModel.getBackColor());
        paintModel.getImage().getPixelWriter().setPixels(0, 0, (int) paintModel.getImage().getWidth(),
                (int) paintModel.getImage().getHeight(), reader, 0, 0);
        paintModel.getImageStack().addEventHandler(MouseEvent.ANY, e -> {
            double x = e.getX();
            double y = e.getY();
            paintModel.getMousePosition().setText(x > 0 && y > 0 ? String.format("%.0fx%.0f", x, y) : "");
            paintModel.getImageSize()
                    .setText(String.format("%.0fx%.0f", paintModel.getImage().getWidth(), paintModel.getImage().getHeight()));

            PaintTool paintTool = paintModel.getTool().get();
			if (paintTool != null) {
                paintModel.getImageStack().setCursor(paintTool.getMouseCursor());
                paintTool.handleEvent(e, paintModel);
            } else {
                paintModel.getImageStack().setCursor(Cursor.DEFAULT);
            }
        });
        root.setCenter(new ZoomableScrollPane(paintModel.getImageStack()));

        HBox hBox = new HBox(50, paintModel.getToolSize(), paintModel.getMousePosition(), paintModel.getImageSize());
        hBox.getChildren().forEach(e -> e.prefHeight(hBox.getPrefWidth() / hBox.getChildren().size()));

        hBox.setStyle("-fx-effect: innershadow(gaussian,gray,10,0.5,10,10);");
        root.setBottom(hBox);
        BorderPane.setAlignment(hBox, Pos.CENTER);
        SimpleToggleGroupBuilder toolGroup = new SimpleToggleGroupBuilder();
        Stream.of(PaintTools.values()).forEach(e -> toolGroup.addToggle(e.getTool()));
                
        List<Node> paintTools = toolGroup
                .onChange((ov, oldValue, newValue) -> changeTool(newValue))
                .getTogglesAs(Node.class);
                
        GridPane toolbar = new GridPane();
        toolbar.addColumn(0, paintTools.stream().limit(paintTools.size() / 2).toArray(Node[]::new));
        toolbar.addColumn(1, paintTools.stream().skip(paintTools.size() / 2).toArray(Node[]::new));
        paintTools.forEach(e -> GridPane.setHalignment(e, HPos.CENTER));

        root.setLeft(toolbar);

        primaryStage.setTitle("Paint");

        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();

    }

    private void changeTool(Toggle newValue) {
        paintModel.getImageStack().getChildren().clear();
        paintModel.getImageStack().getChildren().add(new ImageView(paintModel.getImage()));
        paintModel.getTool().set((PaintTool) newValue.getUserData());
    }

	private void newFile() {
        paintModel.setImage(new WritableImage(500, 500));
        int w = (int) paintModel.getImage().getWidth();
        int h = (int) paintModel.getImage().getHeight();
        paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, new SimplePixelReader(paintModel.getBackColor()), 0, 0);
        paintModel.getImageStack().getChildren().clear();
        paintModel.getImageStack().getChildren().add(new ImageView(paintModel.getImage()));

	}

	private void openFile(Window ownerWindow) {
		FileChooser fileChooser2 = new FileChooser();
		fileChooser2.setTitle("Open File");
		fileChooser2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg"));
		File showOpenDialog = fileChooser2.showOpenDialog(ownerWindow);
		if (showOpenDialog != null) {
			try {
				Image image2 = new Image(new FileInputStream(showOpenDialog));
				int w = (int) image2.getWidth();
				int h = (int) image2.getHeight();
                paintModel.setImage(new WritableImage(w, h));
                paintModel.getImage().getPixelWriter().setPixels(0, 0, w, h, image2.getPixelReader(), 0, 0);
                paintModel.getImageStack().getChildren().clear();
                paintModel.getImageStack().getChildren().add(new ImageView(paintModel.getImage()));
            } catch (Exception e) {
				LOG.error("", e);
			}
		}

	}

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }


}
