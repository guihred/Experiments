package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.RunnableEx.ignore;

import ethical.hacker.ImageCrackerApp;
import ex.j8.Chapter4;
import extract.FileAttrApp;
import fractal.LeafFractalApp;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.SVGPath;
import ml.*;
import ml.graph.Chart3dGraph;
import ml.graph.MapGraph;
import org.junit.Test;
import schema.sngpc.SngpcViewer;
import utils.ConsumerEx;
import utils.ImageFXUtils;

public class FXEngineTest extends AbstractTestExecution {

    @Test
    public void verifyButtons() {
        List<Class<? extends Application>> applications = Arrays.asList(Chapter4.Ex5.class, Chapter4.Ex9.class,
            Chapter4.Ex10.class);
        for (Class<? extends Application> class1 : applications) {
            show(class1);
            clickButtonsWait(WAIT_TIME);
            moveSliders(100);
        }
    }


    @Test
    public void verifyFileAttrApp() {
        show(FileAttrApp.class);
        sleep(500);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
    }

    @Test
    public void verifyHistogramExample() {
        show(HistogramExample.class);
        lookup(CheckBox.class).forEach(ConsumerEx.ignore(this::clickOn));
    }

    @Test
    public void verifyImageCracker() {
        ImageCrackerApp show = show(ImageCrackerApp.class);
        show.setClickable(false);
        Platform.runLater(show::loadURL);
        ImageCrackerApp.waitABit();
    }

    @Test
    public void verifyLeafFractalApp() {
        show(LeafFractalApp.class);
        moveSliders(50);
        clickOn(randomItem(lookup(ToggleButton.class)));
        moveSliders(50);
    }

    @Test
    public void verifyMapGraph() {
        show(MapGraph.class);
        SVGPath randomItem = randomItem(lookup(SVGPath.class));
        ignore(() -> moveTo(randomItem));
    }

    @Test
    public void verifyScrollChart() {
        show(Chart3dGraph.class);
        lookup(".root").queryAll().forEach(t -> {
            ignore(() -> moveTo(t));
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
            randomDrag(t, 50);
        });
        scroll(2, VerticalDirection.DOWN);
        scroll(2, VerticalDirection.UP);
    }

    @Test
    public void verifyScrollWorldMaps() {
        ImageFXUtils.setShowImage(false);
        measureTime("Test.verifyScroll", () -> verifyAndRun(() -> {
            lookup(".root").queryAll().forEach(t -> {
                moveTo(t);
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
                randomDrag(t, 50);
            });
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
            lookup(CheckBox.class).forEach(this::clickOn);
            lookup(ComboBox.class).forEach(e -> selectComboItems(e, 5));
            tryClickButtons();
        }, Arrays.asList(WorldMapExample.class, WorldMapExample2.class, WorldMapExample3.class,
            PopulacionalPyramidExample.class)));
    }

    @Test
    public void verifySngpcViewer() {
        show(SngpcViewer.class);
        sleep(500);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
    }

    @Test
    public void verifyWordSearchApp() {
        show(WordSearchApp.class);
        for (int i = 0; i < 2; i++) {
            for (ComboBox<?> e : lookup(ComboBox.class)) {
                selectComboItems(e, 5);
            }
            tryClickButtons();
        }
        for (int i = 0; i < 2; i++) {
            for (ComboBox<?> e : lookup(ComboBox.class)) {
                selectComboItems(e, 5);
            }
            tryClickButtons();
        }
    }
}
