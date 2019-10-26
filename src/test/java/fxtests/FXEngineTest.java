package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.RunnableEx.ignore;

import ethical.hacker.EthicalHackApp;
import ethical.hacker.ImageCrackerApp;
import ex.j8.Chapter4;
import fxpro.ch06.ResponsiveUIApp;
import fxpro.ch06.ThreadInformationApp;
import fxsamples.*;
import fxsamples.person.PersonTableController;
import fxsamples.person.WorkingWithTableView;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.SVGPath;
import ml.*;
import ml.graph.MapGraph;
import org.junit.Test;
import paintexp.ColorChooser;
import pdfreader.PdfReader;
import schema.sngpc.SngpcViewer;
import utils.ConsoleUtils;
import utils.ConsumerEx;

public class FXEngineTest extends AbstractTestExecution {

    @Test
    public void verifyButtons() {
        measureTime("Test.testButtons",
            () -> FXTesting.verifyAndRun(this, currentStage, () -> lookup(".button").queryAll().forEach(t -> {
                sleep(1000);
                ignore(() -> clickOn(t));
                type(KeyCode.ESCAPE);
            }), Chapter4.Ex9.class, Chapter4.Ex10.class, PdfReader.class));

    }

    @Test
    public void verifyColorChooser() {
        show(ColorChooser.class);

        List<Node> queryAll = lookup(".slider").queryAll().stream().collect(Collectors.toList());
        for (int i = 0; i < queryAll.size(); i++) {
            if (i == 3) {
                lookup(".tab").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
            }
            Node m = queryAll.get(i);
            ignore(() -> drag(m, MouseButton.PRIMARY));
            moveRandom(10);
            drop();
        }
        tryClickButtons();
    }

    @Test
    public void verifyEthicalHack() {
        show(EthicalHackApp.class);
        lookup(".button").queryAllAs(Button.class).stream().filter(e -> !"Ips".equals(e.getText()))
            .forEach(ConsumerEx.ignore(this::clickOn));
        ConsoleUtils.waitAllProcesses();
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
    public void verifyInlineModelViewer() {
        show(InlineModelViewer.class);
        lookup(CheckBox.class).forEach(this::clickOn);
    }

    @Test
    public void verifyLineManipulator() {
        show(LineManipulator.class);
        lookup(AnchorCircle.class).forEach(e -> {
            drag(e, MouseButton.PRIMARY);
            moveRandom(50);
            drag(e, MouseButton.PRIMARY);
        });
    }

    @Test
    public void verifyMapGraph() {
        show(MapGraph.class);
        SVGPath randomItem = randomItem(lookup(SVGPath.class));
        ignore(() -> moveTo(randomItem));
    }

    @Test
    public void verifyPersonTableController() {
        show(PersonTableController.class);
        clickOn(lookupFirst(TextField.class));
        type(typeText(getRandomString()));
    }

    @Test
    public void verifyPhotoViewer() {
        show(PhotoViewer.class);
        tryClickButtons();
    }

    @Test
    public void verifyResponsiveUIApp() {
        show(ResponsiveUIApp.class);
        tryClickButtons();
    }

    @Test
    public void verifyScroll() {
        measureTime("Test.verifyScroll", () -> FXTesting.verifyAndRun(this, currentStage, () -> {
            lookup(Canvas.class).forEach(t -> {
                moveTo(t);
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
            });
            lookup(CheckBox.class).forEach(this::clickOn);
            lookup(ComboBox.class).forEach(e -> selectComboItems(e, 5));
        }, WorldMapExample.class, WorldMapExample2.class, WorldMapExample3.class, PopulacionalPyramidExample.class));

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
    public void verifyThreadInformationApp() {
        show(ThreadInformationApp.class);
        tryClickButtons();
        clickOn(randomItem(lookup(ListCell.class)));
    }

    @Test
    public void verifyWordSearchApp () {
        show(WordSearchApp.class);
        for (ComboBox<?> e : lookup(ComboBox.class)) {
            selectComboItems(e, 5);
        }
        tryClickButtons();
    }

    @Test
    public void verifyWordSuggetion() {
        show(WordSuggetionApp.class);
        lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
            clickOn(t);
            write("new york ");
        }));
    }

    @Test
    public void verifyWorkingListsViews() {
        show(WorkingListsViews.class);
        List<Node> lookup = lookup(ListView.class).stream().collect(Collectors.toList());
        List<Button> buttons = lookup(Button.class).stream().collect(Collectors.toList());
        for (int i = 0; i < lookup.size(); i++) {
            Node queryAs = from(lookup.get(i)).lookup(ListCell.class::isInstance).query();
            ignore(() -> clickOn(queryAs));
            clickOn(buttons.get(i));
        }
    }

    @Test
    public void verifyWorkingWithTableView() {
        show(WorkingWithTableView.class);
        clickOn(randomItem(lookup(ListCell.class)));

    }

}
