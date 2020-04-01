package paintexp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import paintexp.tool.PaintModel;
import simplebuilder.SimpleMenuBarBuilder;
import utils.CrawlerTask;

public class PaintMain extends Application {

    @Override
    public void start(final Stage stage) {
        CrawlerTask.insertProxyConfig();
        PaintController controller = new PaintController();
        PaintModel paintModel = controller.getPaintModel();
        BorderPane root = new BorderPane();
        MenuBar menuBar = new SimpleMenuBarBuilder()
            .addMenu("_File")
            .addMenuItem("_New File", "Ctrl+N", e -> PaintFileUtils.newFile(paintModel))
            .addMenuItem("_Open", "Ctrl+O", e -> PaintFileUtils.openFile(stage, paintModel))
            .addMenuItem("_Save", "Ctrl+S", e -> PaintFileUtils.saveFile(stage, paintModel))
            .addMenuItem("Save _As", "Ctrl+Shift+S", e -> PaintFileUtils.saveAsFile(stage, paintModel))
            .addMenu("_Edit")
            .addMenuItem("Select _All", "Ctrl+A", e -> PaintEditUtils.selectAll(paintModel, controller))
            .addMenuItem("C_opy", "Ctrl+C", e -> PaintEditUtils.copy(paintModel, controller),
                controller.containsSelectedArea().not())
            .addMenuItem("_Paste", "Ctrl+V", e -> PaintEditUtils.paste(paintModel, controller))
            .addMenuItem("_Cut", "Ctrl+X", e -> PaintEditUtils.cut(paintModel, controller),
                controller.containsSelectedArea().not())
            .addMenuItem("Undo", "Ctrl+Z", e -> PaintEditUtils.undo(paintModel, controller))
            .addMenuItem("Redo", "Ctrl+Y", e -> PaintEditUtils.redo(paintModel, controller))
            .addMenu("_View")
            .addMenuItem("Resize/Ske_w", "Ctrl+W", e -> PaintViewUtils.resize(paintModel, controller))
            .addMenuItem("_Flip/Rotate", "Ctrl+R", e -> PaintViewUtils.flipRotate(paintModel, controller))
            .addMenuItem("_Crop", e -> PaintViewUtils.crop(paintModel, controller),
                controller.containsSelectedArea().not())
            .addMenu("_Image")
            .addMenuItem("_Adjust", "Ctrl+J", e -> PaintImageUtils.adjustColors(paintModel, controller))
            .addMenuItem("Mirror _Horizontally", "Ctrl+H",
                e -> PaintImageUtils.mirrorHorizontally(paintModel, controller))
            .addMenuItem("Mirror _Vertically", "Ctrl+M", e -> PaintImageUtils.mirrorVertically(paintModel, controller))
            .addMenu("_Colors")
            .addMenuItem("_Invert Colors", "Ctrl+I", e -> PaintImageUtils.invertColors(paintModel, controller))
            .addMenu("_Help").build();
        SimplePixelReader.paintColor(paintModel.getImage(), paintModel.getBackColor());
        paintModel.getImageStack().addEventHandler(MouseEvent.ANY, controller::handleMouse);
        paintModel.createImageVersion();
        root.setTop(menuBar);
        root.setCenter(paintModel.getScrollPane());
        root.setBottom(PaintHelper.buildColorGrid(paintModel, controller));
        root.setLeft(PaintHelper.buildToolBar(controller));
        StackPane child = new StackPane(paintModel.getToolOptions());
        child.setPadding(new Insets(10));
        root.setRight(new HBox(child, PaintHelper.displayImageVersions(paintModel)));
        paintModel.bindTitle(stage.titleProperty());
        stage.setX(0);
        final int width = 900;

        Scene scene = new Scene(root, width, width);
        scene.addEventHandler(KeyEvent.ANY, controller::handleKeyBoard);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(final String[] args) {

        System.setProperty("prism.lcdtext", "false");
        launch(args);
    }
}
