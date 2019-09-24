package paintexp;

import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import paintexp.tool.PaintModel;
import paintexp.tool.PaintTool;
import paintexp.tool.PaintTools;
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
				.addMenuItem("_New File", "Ctrl+N", e -> PaintFileUtils.newFile(paintModel))
				.addMenuItem("_Open", "Ctrl+O", e -> PaintFileUtils.openFile(stage, paintModel))
				.addMenuItem("_Save", "Ctrl+S", e -> PaintFileUtils.saveFile(stage, paintModel))
				.addMenuItem("Save _As", "Ctrl+Shift+S", e -> PaintFileUtils.saveAsFile(stage, paintModel))
				.addMenu("_Edit")
				.addMenuItem("Select _All", "Ctrl+A",
						e -> PaintEditUtils.selectAll(paintModel, controller.getCurrentSelectTool(), controller))
				.addMenuItem("C_opy", "Ctrl+C", e -> PaintEditUtils.copy(paintModel, controller.getCurrentSelectTool()),
						controller.containsSelectedArea().not())
				.addMenuItem("_Paste", "Ctrl+V",
						e -> PaintEditUtils.paste(paintModel, controller.getCurrentSelectTool(), controller))
				.addMenuItem("_Cut", "Ctrl+X", e -> PaintEditUtils.cut(paintModel, controller.getCurrentSelectTool()),
						controller.containsSelectedArea().not())
				.addMenuItem("Undo", "Ctrl+Z", e -> PaintEditUtils.undo(paintModel))
				.addMenu("_View")
				.addMenuItem("Resize/Ske_w", "Ctrl+W",
						e -> PaintViewUtils.resize(paintModel, controller.getSelectedImage(), controller))
				.addMenuItem("_Flip/Rotate", "Ctrl+R", e -> PaintViewUtils.flipRotate(paintModel, controller))
				.addMenuItem("_Crop",
						e -> PaintViewUtils.crop(paintModel, controller.getSelectedImage(),
								controller.getCurrentSelectTool()),
						controller.containsSelectedArea().not())
				.addMenu("_Image")
				.addMenuItem("_Adjust", "Ctrl+J", e -> PaintImageUtils.adjustColors(paintModel, controller))
				.addMenuItem("Mirror _Horizontally", "Ctrl+H",
						e -> PaintImageUtils.mirrorHorizontally(paintModel, controller))
				.addMenuItem("Mirror _Vertically", "Ctrl+M",
						e -> PaintImageUtils.mirrorVertically(paintModel, controller))
				.addMenu("_Colors")
				.addMenuItem("_Invert Colors", "Ctrl+I", e -> PaintImageUtils.invertColors(paintModel, controller))
				.addMenu("_Help").build());
		SimplePixelReader.paintColor(paintModel.getImage(), paintModel.getBackColor());
		paintModel.getImageStack().addEventHandler(MouseEvent.ANY, controller::handleMouse);
		paintModel.createImageVersion();
		root.setCenter(paintModel.getScrollPane());

		VBox value = buildColorGrid(paintModel, controller);
		root.setBottom(value);
		GridPane toolbar = buildToolBar(paintModel, controller);
		root.setLeft(toolbar);
		root.setRight(displayImageVersions(paintModel));
		stage.setX(0);
		stage.setTitle("Paint");
		final int width = 800;
		Scene scene = new Scene(root, width, width);
		scene.addEventHandler(KeyEvent.ANY, controller::handleKeyBoard);
		stage.setScene(scene);
		stage.show();

	}

	public static void main(final String[] args) {
		CrawlerTask.insertProxyConfig();
		System.setProperty("prism.lcdtext", "false");
		launch(args);
	}

	protected static TableView<WritableImage> displayImageVersions(final PaintModel paintModel) {
		final int tablePrefWidth = 100;
		ObservableList<WritableImage> imageVersions = paintModel.getImageVersions();
		TableView<WritableImage> tableView = new SimpleTableViewBuilder<WritableImage>()
				.addColumn("Image", (p, cell) -> cell.setGraphic(imageView(tablePrefWidth, p))).items(imageVersions)
				.prefWidth(tablePrefWidth).equalColumns().scrollTo(tablePrefWidth).cache(false).build();
		imageVersions.addListener((Change<? extends WritableImage> e) -> tableView.scrollTo(e.getList().size() - 1));
		return tableView;
	}

	private static VBox buildColorGrid(PaintModel paintModel, PaintController controller2) {
		final int gap = 50;
		HBox hBox = new HBox(gap, paintModel.getToolSize(), paintModel.getMousePosition(), paintModel.getImageSize());
		hBox.getChildren().forEach(e -> e.prefHeight(hBox.getPrefWidth() / hBox.getChildren().size()));
		BorderPane.setAlignment(hBox, Pos.CENTER);
		hBox.setStyle("-fx-effect: innershadow(gaussian,gray,10,0.5,10,10);");
		GridPane gridPane = new GridPane();
		StackPane st = new StackPane(pickedColor(paintModel.backColorProperty(), 20),
				pickedColor(paintModel.frontColorProperty(), 10));
		List<Color> colors = PaintController.getColors();
		int maxSize = colors.size() / 2;
		gridPane.addRow(0, colors.stream().limit(maxSize).map(controller2::newRectangle).toArray(Rectangle[]::new));
		gridPane.addRow(1,
				colors.stream().skip(maxSize).limit(maxSize).map(controller2::newRectangle).toArray(Rectangle[]::new));
		gridPane.setId("colorGrid");
		return new VBox(30, new HBox(gap, st, gridPane), hBox);
	}

	private static GridPane buildToolBar(PaintModel paintModel, PaintController controller2) {
		SimpleToggleGroupBuilder toolGroup = new SimpleToggleGroupBuilder();
		Stream.of(PaintTools.values()).forEach(e -> toolGroup.addToggleTooltip(e.getTool(), e.getTooltip()));
		List<Node> paintTools = toolGroup
				.onChange((ov, oldValue, newValue) -> controller2
						.changeTool(newValue == null ? null : (PaintTool) newValue.getUserData()))
				.getTogglesAs(Node.class);
		ToggleGroup toggleGroup = toolGroup.build();
		controller2.toolProperty().addListener((ob, old, newV) -> toggleGroup.selectToggle(
				toggleGroup.getToggles().stream().filter(e -> e.getUserData().equals(newV)).findFirst().orElse(null)));

		GridPane toolbar = new GridPane();
		toolbar.addColumn(0, paintTools.stream().limit(paintTools.size() / 2).toArray(Node[]::new));
		toolbar.addColumn(1, paintTools.stream().skip(paintTools.size() / 2).toArray(Node[]::new));
		StackPane child = new StackPane(paintModel.getToolOptions());
		child.setPadding(new Insets(10));
		toolbar.add(child, 0, paintTools.size(), 2, 2);
		toolbar.getChildren().forEach(e -> GridPane.setHalignment(e, HPos.CENTER));
		return toolbar;
	}

	private static ImageView imageView(final int tablePrefWidth, WritableImage p) {
		ImageView value = new ImageView(p);
		value.setPreserveRatio(true);
		value.setFitWidth(tablePrefWidth);
		return value;
	}

	private static Rectangle pickedColor(final ObjectProperty<Color> objectProperty, final int value) {
		return new SimpleRectangleBuilder().layoutX(value).layoutY(value).managed(false).width(20).height(20)
				.stroke(Color.GRAY).fill(objectProperty).build();
	}
}
