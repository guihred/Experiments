package paintexp;

import java.util.List;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import simplebuilder.SimpleMenuBarBuilder;
import simplebuilder.SimpleTextBuilder;
import simplebuilder.SimpleToggleGroupBuilder;

public class PaintMain extends  Application{

    private Color backColor = Color.WHITE;
    private WritableImage image = new WritableImage(500, 500);
    private ImageView canvas = new ImageView(image);
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
                .addMenuItem("New", e -> {
                })
                .addMenu("_Edit")
                .addMenu("_View")
                .addMenu("_Image")
                .addMenu("_Colors")
                .addMenu("_Help")
                .build());
        PixelReader reader = new SimplePixelReader(backColor);
        image.getPixelWriter().setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), reader, 0, 0);
        StackPane value = new StackPane(canvas);
        value.addEventHandler(MouseEvent.ANY, e -> {
            double x = e.getX();
            double y = e.getY();
            mousePosition.setText(x > 0 && y > 0 ? String.format("%.0fx%.0f", x, y) : "");
            imageSize.setText(String.format("%.0fx%.0f", image.getWidth(), image.getHeight()));
        });
        value.setAlignment(Pos.TOP_LEFT);
        value.setMinWidth(200);
        value.setMinHeight(200);
        root.setCenter(value);

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

    public static void main(String[] args) {
        launch(args);
    }


}
