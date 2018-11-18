package paintexp;

import graphs.entities.ZoomableScrollPane;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import simplebuilder.SimpleMenuBarBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CrawlerTask;

public class PaintMain extends  Application{

	private PaintController controller = new PaintController();
    @Override
    public void start(final Stage primaryStage) throws Exception {
		PaintModel paintModel = controller.getPaintModel();
        BorderPane root = new BorderPane();
        root.setTop(new SimpleMenuBarBuilder()
                .addMenu("_File")
				.addMenuItem("_New", e -> controller.newFile())
				.addMenuItem("_Open", e -> controller.openFile(primaryStage))
				.addMenuItem("_Save", e -> controller.saveFile(primaryStage))
                .addMenu("_Edit")
                .addMenu("_View")
                .addMenu("_Image")
                .addMenu("_Colors")
                .addMenu("_Help")
                .build());
        PixelReader reader = new SimplePixelReader(paintModel.getBackColor());
        paintModel.getImage().getPixelWriter().setPixels(0, 0, (int) paintModel.getImage().getWidth(),
                (int) paintModel.getImage().getHeight(), reader, 0, 0);
		paintModel.getImageStack().addEventHandler(MouseEvent.ANY, controller::handleMouse);
        root.setCenter(new ZoomableScrollPane(paintModel.getImageStack()));

        HBox hBox = new HBox(50, paintModel.getToolSize(), paintModel.getMousePosition(), paintModel.getImageSize());
        hBox.getChildren().forEach(e -> e.prefHeight(hBox.getPrefWidth() / hBox.getChildren().size()));

        hBox.setStyle("-fx-effect: innershadow(gaussian,gray,10,0.5,10,10);");
        GridPane gridPane = new GridPane();
		StackPane st = new StackPane(pickedColor(paintModel.backColorProperty(), 20),
				pickedColor(paintModel.frontColorProperty(), 10));
		List<Color> colors = controller.getColors();
		gridPane.addRow(0, colors.stream().limit(14).map(controller::newRectangle).toArray(Rectangle[]::new));
		gridPane.addRow(1, colors.stream().skip(14).limit(14).map(controller::newRectangle)
						.toArray(Rectangle[]::new));

		root.setBottom(new VBox(30, new HBox(50, st, gridPane), hBox));
        BorderPane.setAlignment(hBox, Pos.CENTER);
        SimpleToggleGroupBuilder toolGroup = new SimpleToggleGroupBuilder();
        Stream.of(PaintTools.values()).forEach(e -> toolGroup.addToggle(e.getTool()));
        List<Node> paintTools = toolGroup
				.onChange((ov, oldValue, newValue) -> controller.changeTool(newValue))
                .getTogglesAs(Node.class);
		ToggleGroup toggleGroup = toolGroup.build();
		paintModel.toolProperty().addListener((ob, old, newV) -> toggleGroup.selectToggle(
				toggleGroup.getToggles().stream().filter(e -> e.getUserData().equals(newV)).findFirst().orElse(null)));
                
        GridPane toolbar = new GridPane();
        toolbar.addColumn(0, paintTools.stream().limit(paintTools.size() / 2).toArray(Node[]::new));
        toolbar.addColumn(1, paintTools.stream().skip(paintTools.size() / 2).toArray(Node[]::new));
		StackPane child = new StackPane(paintModel.getToolOptions());
		child.setPadding(new Insets(10));
		toolbar.add(child, 0, paintTools.size(), 2, 2);
		toolbar.getChildren().forEach(e -> GridPane.setHalignment(e, HPos.CENTER));
        root.setLeft(toolbar);

        primaryStage.setTitle("Paint");
		Scene scene = new Scene(root, 600, 600);
		scene.addEventHandler(KeyEvent.ANY, controller::handleKeyBoard);
		primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Rectangle pickedColor(final ObjectProperty<Color> objectProperty, final int value) {

        return new SimpleRectangleBuilder()
                .layoutX(value).layoutY(value)
                .managed(false)
                .width(20).height(20)
                .stroke(Color.GRAY)
                .fill(objectProperty).build();
    }

    public static void main(final String[] args) {
		CrawlerTask.insertProxyConfig();
		System.setProperty("prism.lcdtext", "false");
        launch(args);
    }


}
