package fxtests;

import ex.j8.Chapter4;
import extract.FileAttrApp;
import fractal.LeafFractalApp;
import fxml.utils.JsonViewer;
import fxml.utils.XmlViewer;
import graphs.app.AllApps;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.SVGPath;
import ml.*;
import ml.graph.Chart3dGraph;
import ml.graph.MapGraph;
import org.junit.Test;
import utils.FileTreeWalker;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;

public class FXEngineTest extends AbstractTestExecution {

    @Test
    public void verifyAllApps() {
        show(AllApps.class);
        doubleClickOn(randomItem(lookup(ListCell.class)));

    }

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
        Set<CheckBox> lookup = lookup(CheckBox.class).stream().limit(5).collect(Collectors.toSet());
        lookup.forEach(this::tryClickOn);
        lookup.forEach(this::tryClickOn);
    }


    @Test
    public void verifyJsonViewer() {
        JsonViewer show = show(JsonViewer.class);
        List<Path> pathByExtension = FileTreeWalker.getPathByExtension(ResourceFXUtils.getOutFile(), ".json").stream()
                .limit(10).collect(Collectors.toList());
        show.addFile(pathByExtension.stream().map(Path::toFile).toArray(File[]::new));

        sleep(500);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
        ImageFXUtils.setShowImage(false);
        List<Button> lookupList = lookupList(Button.class);
        runReversed(lookupList, this::tryClickOn);
        type(KeyCode.ESCAPE);
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
        RunnableEx.ignore(() -> moveTo(randomItem));
    }

    @Test
    public void verifyScrollChart() {
        show(Chart3dGraph.class);
        lookup(".root").queryAll().forEach(t -> {
            RunnableEx.ignore(() -> moveTo(t));
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
            randomDrag(t, 50);
        });
        scroll(2, VerticalDirection.DOWN);
        scroll(2, VerticalDirection.UP);
    }

    @Test
    public void verifyScrollWorldMaps() {
        measureTime("Test.verifyScroll", () -> verifyAndRun(() -> {
            ImageFXUtils.setShowImage(nextBoolean());
            lookup(".root").queryAll().forEach(t -> {
                moveTo(t);
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
                randomDrag(t, 50);
            });
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
            lookup(CheckBox.class).forEach(this::clickOn);
            lookup(ComboBox.class).forEach(e -> selectComboItems(e, 10));
            lookup(ComboBox.class).forEach(e -> selectComboItems(e, 10));
            tryClickButtons();
            ImageFXUtils.setShowImage(false);
        }, Arrays.asList(WorldMapExample.class, WorldMapExample2.class, WorldMapExample3.class,
            PopulacionalPyramidExample.class)));
    }

    @Test
    public void verifySngpcViewer() {
        show(XmlViewer.class);
        sleep(500);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
        ImageFXUtils.setShowImage(false);
        List<Button> lookupList = lookupList(Button.class);
        runReversed(lookupList, this::tryClickOn);
        type(KeyCode.ESCAPE);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void verifyTimelineExample() {
        show(TimelineExample.class);
        List<CheckBox> lookupList = lookupList(CheckBox.class);
        Collections.shuffle(lookupList);
        lookupList.stream().limit(5).forEach(this::tryClickOn);
        List<ComboBox> combos = lookupList(ComboBox.class);
        selectComboItems(randomItem(combos), 5);
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
