package fxtests;

import static fxtests.FXTesting.measureTime;

import ethical.hacker.EthicalHackApp;
import ethical.hacker.ImageCrackerApp;
import ex.j8.Chapter4;
import fxsamples.AnchorCircle;
import fxsamples.LineManipulator;
import fxsamples.PhotoViewer;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.SVGPath;
import ml.WordSuggetionApp;
import ml.WorldMapExample;
import ml.WorldMapExample2;
import ml.graph.MapGraph;
import org.junit.Test;
import paintexp.ColorChooser;
import pdfreader.PdfReader;
import schema.sngpc.SngpcViewer;
import utils.ConsoleUtils;
import utils.ConsumerEx;
import utils.RunnableEx;

public class FXEngineTest extends AbstractTestExecution {

    @Test
    public void verifyButtons() throws Exception {
        measureTime("Test.testButtons",
            () -> FXTesting.verifyAndRun(this, currentStage, () -> lookup(".button").queryAll().forEach(t -> {
                sleep(1000);
                RunnableEx.ignore(() -> clickOn(t));
                type(KeyCode.ESCAPE);
            }), Chapter4.Ex9.class, Chapter4.Ex10.class, PdfReader.class));

    }

    @Test
    public void verifyColorChooser() throws Exception {
        show(ColorChooser.class);

        List<Node> queryAll = lookup(".slider").queryAll().stream().collect(Collectors.toList());
        for (int i = 0; i < queryAll.size(); i++) {
            if (i == 3) {
                lookup(".tab").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
            }
            Node m = queryAll.get(i);
            RunnableEx.ignore(() -> drag(m, MouseButton.PRIMARY));
            moveBy(Math.random() * 10 - 5, 0);
            drop();
        }
        tryClickButtons();
    }

    @Test
    public void verifyEthicalHack() throws Exception {
        show(EthicalHackApp.class);
        lookup(".button").queryAllAs(Button.class).stream().filter(e -> !"Ips".equals(e.getText()))
            .forEach(ConsumerEx.ignore(this::clickOn));
        ConsoleUtils.waitAllProcesses();
    }

    @Test
    public void verifyImageCracker() throws Exception {
        ImageCrackerApp show = show(ImageCrackerApp.class);
        show.setClickable(false);
        Platform.runLater(show::loadURL);
        ImageCrackerApp.waitABit();
    }

    @Test
    public void verifyLineManipulator() throws Exception {
        show(LineManipulator.class);
        lookup(AnchorCircle.class).forEach(e -> {
            drag(e, MouseButton.PRIMARY);
            moveRandom(50);
            drag(e, MouseButton.PRIMARY);
        });
    }

    @Test
    public void verifyMapGraph() throws Exception {
        show(MapGraph.class);
        SVGPath randomItem = randomItem(lookup(SVGPath.class));
        RunnableEx.run(() -> moveTo(randomItem));
    }

    @Test
    public void verifyPhotoViewer() throws Exception {
        show(PhotoViewer.class);
        tryClickButtons();
    }

    @Test
    public void verifyScroll() throws Exception {
        measureTime("Test.verifyScroll",
            () -> FXTesting.verifyAndRun(this, currentStage, () -> {
                lookup(".button").queryAll().forEach(t -> {
                    scroll(2, VerticalDirection.DOWN);
                    scroll(2, VerticalDirection.UP);
                });
                lookup(CheckBox.class).forEach(this::clickOn);
                lookup(ComboBox.class).forEach(e -> {
                    ObservableList<?> items = e.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        int j = i;
                        interact(() -> e.getSelectionModel().select(j));
                    }
                });
            }, WorldMapExample.class, WorldMapExample2.class));

    }

    @Test
    public void verifySngpcViewer() throws Exception {
        show(SngpcViewer.class);
        sleep(500);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
    }

    @Test
    public void verifyWordSuggetion() throws Exception {
        show(WordSuggetionApp.class);
        lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
            clickOn(t);
            write("new york ");
        }));
    }

}
