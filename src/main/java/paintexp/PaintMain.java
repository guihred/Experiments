package paintexp;

import graphs.entities.ZoomableScrollPane;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import simplebuilder.SimpleMenuBarBuilder;
import simplebuilder.SimpleTextBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.HasLogging;

public class PaintMain extends  Application{

	private static final Logger LOG = HasLogging.log();
	private Color backColor = Color.WHITE;
    private WritableImage image = new WritableImage(500, 500);
	private StackPane imageStack = new StackPane(new ImageView(image));
    private ObjectProperty<PaintTool> tool = new SimpleObjectProperty<>();
    private Text imageSize = new SimpleTextBuilder()
            .build();
    private Text toolSize = new SimpleTextBuilder().build();
    private Text mousePosition = new SimpleTextBuilder().build();
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
        PixelReader reader = new SimplePixelReader(backColor);
        image.getPixelWriter().setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), reader, 0, 0);
		imageStack.addEventHandler(MouseEvent.ANY, e -> {
            double x = e.getX();
            double y = e.getY();
            mousePosition.setText(x > 0 && y > 0 ? String.format("%.0fx%.0f", x, y) : "");
            imageSize.setText(String.format("%.0fx%.0f", image.getWidth(), image.getHeight()));

			PaintTool paintTool = tool.get();
			if (paintTool != null) {
				paintTool.handleEvent(e,image,imageStack);
			}
        });
		imageStack.setAlignment(Pos.TOP_LEFT);
		imageStack.setMinWidth(200);
		imageStack.setMinHeight(200);
		root.setCenter(new ZoomableScrollPane(imageStack));

        HBox hBox = new HBox(50, toolSize, mousePosition, imageSize);
        hBox.getChildren().forEach(e -> e.prefHeight(hBox.getPrefWidth() / hBox.getChildren().size()));

        hBox.setStyle("-fx-effect: innershadow(gaussian,gray,10,0.5,10,10);");
        root.setBottom(hBox);
        BorderPane.setAlignment(hBox, Pos.CENTER);
        SimpleToggleGroupBuilder toolGroup = new SimpleToggleGroupBuilder();
        Stream.of(PaintTools.values()).forEach(e -> toolGroup.addToggle(e.getTool()));
                
        List<Node> paintTools = toolGroup
                .onChange((ov, oldValue, newValue) -> tool.set((PaintTool) newValue.getUserData()))
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
				image = new WritableImage(w, h);
				image.getPixelWriter().setPixels(0, 0, w, h, image2.getPixelReader(), 0, 0);
				imageStack.getChildren().clear();
				imageStack.getChildren().add(new ImageView(image));
			} catch (FileNotFoundException e) {

				LOG.error("", e);
			}
		}

	}

	private void newFile() {
		image = new WritableImage(500, 500);
		int w = (int) image.getWidth();
		int h = (int) image.getHeight();
		image.getPixelWriter().setPixels(0, 0, w, h, new SimplePixelReader(backColor), 0, 0);
		imageStack.getChildren().clear();
		imageStack.getChildren().add(new ImageView(image));

	}

    public static void main(String[] args) {
        launch(args);
    }


}
