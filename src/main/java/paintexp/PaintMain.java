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
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import paintexp.tool.PaintTool;
import simplebuilder.SimpleMenuBarBuilder;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CrawlerTask;

public class PaintMain extends Application {

	private PaintController controller = new PaintController();
    @Override
	public void start(final Stage stage) throws Exception {
		PaintModel paintModel = controller.getPaintModel();
        BorderPane root = new BorderPane();
        root.setTop(new SimpleMenuBarBuilder()
                .addMenu("_File")
                .addMenuItem("_New", "Ctrl+N", controller::newFile)
                .addMenuItem("_Open", "Ctrl+O", () -> controller.openFile(stage))
                .addMenuItem("_Save", "Ctrl+S", () -> controller.saveFile(stage))
                .addMenuItem("Save _As", "Ctrl+Shift+S", () -> controller.saveAsFile(stage))
                .addMenu("_Edit")
                .addMenuItem("Select _All", "Ctrl+A", controller::selectAll)
                .addMenuItem("C_opy", "Ctrl+C", e -> controller.copy(), controller.containsSelectedArea().not())
                .addMenuItem("_Paste", "Ctrl+V", controller::paste)
                .addMenuItem("_Cut", "Ctrl+X", e -> controller.cut(), controller.containsSelectedArea().not())
                .addMenuItem("Undo", "Ctrl+Z", controller::undo)
				.addMenu("_View")
                .addMenuItem("Resize/Ske_w", "Ctrl+W", controller::resize)
                .addMenuItem("_Flip/Rotate", "Ctrl+R", controller::flipRotate)
                .addMenuItem("_Crop", e -> controller.crop(), controller.containsSelectedArea().not())
                .addMenu("_Image")
                .addMenuItem("_Invert Colors", "Ctrl+I", controller::invertColors)
                .addMenuItem("_Adjust", "Ctrl+J", controller::adjustColors)
                .addMenuItem("Mirror _Horizontally", "Ctrl+H", controller::mirrorHorizontally)
                .addMenuItem("Mirror _Vertically", "Ctrl+M", controller::mirrorVertically)
                .addMenu("_Colors")
                .addMenu("_Help")
                .build());

        PixelReader reader = new SimplePixelReader(paintModel.getBackColor());
        paintModel.getImage().getPixelWriter().setPixels(0, 0, (int) paintModel.getImage().getWidth(),
                (int) paintModel.getImage().getHeight(), reader, 0, 0);
        paintModel.getImageStack().addEventHandler(MouseEvent.ANY, controller::handleMouse);
        paintModel.createImageVersion();
        root.setCenter(new ZoomableScrollPane(paintModel.getImageStack()));

        final int gap = 50;
        HBox hBox = new HBox(gap, paintModel.getToolSize(), paintModel.getMousePosition(), paintModel.getImageSize());
        hBox.getChildren().forEach(e -> e.prefHeight(hBox.getPrefWidth() / hBox.getChildren().size()));

        hBox.setStyle("-fx-effect: innershadow(gaussian,gray,10,0.5,10,10);");
        GridPane gridPane = new GridPane();
		StackPane st = new StackPane(pickedColor(paintModel.backColorProperty(), 20),
				pickedColor(paintModel.frontColorProperty(), 10));
		List<Color> colors = controller.getColors();
        int maxSize = colors.size() / 2;
        gridPane.addRow(0, colors.stream().limit(maxSize).map(controller::newRectangle).toArray(Rectangle[]::new));
        gridPane.addRow(1,
                colors.stream().skip(maxSize).limit(maxSize).map(controller::newRectangle)
						.toArray(Rectangle[]::new));
		gridPane.setId("colorGrid");
        root.setBottom(new VBox(30, new HBox(gap, st, gridPane), hBox));
        BorderPane.setAlignment(hBox, Pos.CENTER);
        SimpleToggleGroupBuilder toolGroup = new SimpleToggleGroupBuilder();
        Stream.of(PaintTools.values()).forEach(e -> toolGroup.addToggleTooltip(e.getTool(), e.getTooltip()));
        List<Node> paintTools = toolGroup
                .onChange((ov, oldValue, newValue) -> paintModel
                        .changeTool(newValue == null ? null : (PaintTool) newValue.getUserData()))
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
		stage.setX(0);
		stage.setTitle("Paint");
        final int width = 800;
        Scene scene = new Scene(root, width, width);
		scene.addEventHandler(KeyEvent.ANY, controller::handleKeyBoard);
		stage.setScene(scene);
		stage.show();
        
        // displayImageVersions(paintModel)

    }

	protected void displayImageVersions(final PaintModel paintModel) {
        int tablePrefWidth = 600;
        TableView<WritableImage> root2 = new SimpleTableViewBuilder<WritableImage>()
				.addColumn("Image", (p, cell) -> cell.setGraphic(new ImageView(p)))
				.items(paintModel.getImageVersions())
                .prefWidth(tablePrefWidth)
				.equalColumns().build();
        Stage stage = new Stage();
		Scene value = new Scene(root2);

		stage.setScene(value);
        stage.setX(1000);
        root2.scrollTo(tablePrefWidth);
		stage.show();
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
