package paintexp;

import java.util.List;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import paintexp.tool.PaintModel;
import paintexp.tool.PaintTool;
import paintexp.tool.PaintTools;
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public final class PaintHelper {
    private PaintHelper() {
    }

    public static VBox buildColorGrid(PaintModel paintModel, PaintController controller2) {
        final int gap = 50;
        HBox hBox = new HBox(gap, paintModel.getToolSize(), paintModel.getMousePosition(), paintModel.getImageSize());
        hBox.getChildren().forEach(e -> e.prefHeight(hBox.getPrefWidth() / hBox.getChildren().size()));
        BorderPane.setAlignment(hBox, Pos.CENTER);
        hBox.setStyle("-fx-effect: innershadow(gaussian,gray,10,0.5,10,10);");
        GridPane gridPane = new GridPane();
        StackPane st = new StackPane(PaintHelper.pickedColor(paintModel.backColorProperty(), 20),
                PaintHelper.pickedColor(paintModel.frontColorProperty(), 10));
        List<Color> colors = PaintController.getColors();
        int maxSize = colors.size() / 2;
        gridPane.addRow(0, colors.stream().limit(maxSize).map(controller2::newRectangle).toArray(Rectangle[]::new));
        gridPane.addRow(1,
                colors.stream().skip(maxSize).limit(maxSize).map(controller2::newRectangle).toArray(Rectangle[]::new));
        gridPane.setId("colorGrid");
        return new VBox(30, new HBox(gap, st, gridPane), hBox);
    }

    public static GridPane buildToolBar(PaintController controller) {
        SimpleToggleGroupBuilder toolGroup = new SimpleToggleGroupBuilder();
        Stream.of(PaintTools.values()).forEach(e -> toolGroup.addToggleTooltip(e.getTool(), e.getTooltip()));
        List<Node> paintTools = toolGroup
                .onChange((ov, oldValue, newValue) -> controller
                        .changeTool(newValue == null ? null : (PaintTool) newValue.getUserData()))
                .getTogglesAs(Node.class);
        ToggleGroup toggleGroup = toolGroup.build();
        controller.toolProperty().addListener((ob, old, newV) -> toggleGroup.selectToggle(
                toggleGroup.getToggles().stream().filter(e -> e.getUserData().equals(newV)).findFirst().orElse(null)));

        GridPane toolbar = new GridPane();
        toolbar.addColumn(0, paintTools.stream().limit(paintTools.size() / 2).toArray(Node[]::new));
        toolbar.addColumn(1, paintTools.stream().skip(paintTools.size() / 2).toArray(Node[]::new));

        toolbar.getChildren().forEach(e -> GridPane.setHalignment(e, HPos.CENTER));
        return toolbar;
    }

    public static TableView<WritableImage> displayImageVersions(final PaintModel paintModel) {
        final int tablePrefWidth = 100;
        ObservableList<WritableImage> imageVersions = paintModel.getImageVersions();
        TableView<WritableImage> tableView = new SimpleTableViewBuilder<WritableImage>()
                .addColumn("Image", (p, cell) -> cell.setGraphic(PaintHelper.imageView(tablePrefWidth, p)))
                .items(imageVersions).prefWidth(tablePrefWidth).equalColumns().scrollTo(tablePrefWidth).cache(false)
                .build();
        imageVersions.addListener((Change<? extends WritableImage> e) -> tableView.scrollTo(e.getList().size() - 1));
        return tableView;
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
