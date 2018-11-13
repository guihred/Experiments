package paintexp;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import simplebuilder.SimpleMenuBarBuilder;

public class PaintMain extends  Application{

    private Color backColor = Color.WHITE;
    private WritableImage image = new WritableImage(600, 600);

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
        StackPane value = new StackPane(new ImageView(image));
        value.setAlignment(Pos.TOP_LEFT);
        value.setMinWidth(200);
        value.setMinHeight(200);
        root.setCenter(value);

        List<Node> paintTools = Stream.of(PaintTools.values()).map(PaintTools::getIcon).collect(Collectors.toList());
        GridPane toolbar = new GridPane();
        toolbar.addColumn(0, paintTools.stream().limit(paintTools.size() / 2).toArray(Node[]::new));
        toolbar.addColumn(1, paintTools.stream().skip(paintTools.size() / 2).toArray(Node[]::new));
        paintTools.forEach(e -> GridPane.setHalignment(e, HPos.CENTER));

        root.setLeft(toolbar);

        primaryStage.setTitle("Paint");

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }


}
