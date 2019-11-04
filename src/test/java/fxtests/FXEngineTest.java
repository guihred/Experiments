package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.RunnableEx.ignore;

import ethical.hacker.EthicalHackApp;
import ethical.hacker.ImageCrackerApp;
import ex.j8.Chapter4;
import extract.FileAttrApp;
import fractal.LeafFractalApp;
import fxpro.ch06.ResponsiveUIApp;
import fxpro.ch06.ThreadInformationApp;
import fxpro.ch07.Chart3dDemo;
import fxsamples.AnchorCircle;
import fxsamples.InlineModelViewer;
import fxsamples.LineManipulator;
import fxsamples.LookNFeelChooser;
import fxsamples.bounds.BoundsPlayground;
import fxsamples.person.FormValidation;
import fxsamples.person.PersonTableController;
import fxsamples.person.WorkingWithTableView;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.SVGPath;
import ml.*;
import ml.graph.Chart3dGraph;
import ml.graph.MapGraph;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import paintexp.ColorChooser;
import pdfreader.PdfReader;
import schema.sngpc.SngpcViewer;
import utils.ConsoleUtils;
import utils.ConsumerEx;

public class FXEngineTest extends AbstractTestExecution {

    @Test
    public void verifyBoundsPlayground() {
        show(BoundsPlayground.class);
        lookup(CheckBox.class).forEach(this::clickOn);
        lookup(RadioButton.class).forEach(this::clickOn);
    }

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
            randomDrag(m, 10);

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
    public void verifyFileAttrApp() {
        show(FileAttrApp.class);
        sleep(500);
        targetPos(Pos.TOP_CENTER);
        clickOn(lookupFirst(TreeView.class));
        targetPos(Pos.CENTER);
        type(KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN, KeyCode.RIGHT, KeyCode.DOWN);
    }

    @Test
    public void verifyFormValidation() {
        showNewStage(FormValidation.class);
        clickOn(lookupFirst(PasswordField.class));
        type(typeText(getRandomString()));
        type(KeyCode.ENTER);
        eraseText(4);
        type(typeText(getRandomString()));
        type(KeyCode.ENTER);
        eraseText(4);
        type(typeText("senha"));
        type(KeyCode.ENTER);
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
        lookup(CheckBox.class).forEach(this::clickOn);
    }

    @Test
    public void verifyLeafFractalApp() {
        show(LeafFractalApp.class);
        moveSliders(50);
    }

    @Test
    public void verifyLineManipulator() {
        show(LineManipulator.class);
        lookup(AnchorCircle.class).forEach(e -> randomDrag(e, 50));
    }

    @Test
    public void verifyLookNFeelChooser() {
        show(LookNFeelChooser.class);
        List<MenuButton> node = lookup(MenuButton.class).stream().collect(Collectors.toList());
        for (int i = node.size() - 1; i >= 0; i--) {
            MenuButton menuButton = node.get(i);
            ObservableList<MenuItem> items = menuButton.getItems();
            for (int j = 0; j < items.size(); j++) {
                MenuItem menuItem = items.get(j);
                interact(menuItem::fire);
            }
        }
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
    public void verifyResponsiveUIApp() {
        show(ResponsiveUIApp.class);
        tryClickButtons();
    }

    @Test
    public void verifyScrollChart() {
        measureTime("Test.verifyScroll", () -> FXTesting.verifyAndRun(this, currentStage, () -> {
            lookup(".root").queryAll().forEach(t -> {
                moveTo(t);
                scroll(2, VerticalDirection.DOWN);
                scroll(2, VerticalDirection.UP);
                randomDrag(t, 50);
            });
            scroll(2, VerticalDirection.DOWN);
            scroll(2, VerticalDirection.UP);
        }, Chart3dDemo.class, Chart3dGraph.class));
    }

    @Test
    public void verifyScrollWorldMaps() {
        measureTime("Test.verifyScroll", () -> FXTesting.verifyAndRun(this, currentStage, () -> {
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
        sleep(500);
        clickOn(randomItem(lookup(ListCell.class)));
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

    @Test
    public void verifyWordSuggetion() {
        show(WordSuggetionApp.class);
        lookup(".text-field").queryAll().forEach(ConsumerEx.makeConsumer(t -> {
            clickOn(t);
            write("new york ");
        }));
    }

    @Test
    public void verifyWorkingWithTableView() {
        show(WorkingWithTableView.class);
        @SuppressWarnings("rawtypes")
        List<ListCell> listCells = lookup(ListCell.class).stream().filter(c -> StringUtils.isNotBlank(c.getText()))
            .collect(Collectors.toList());
        if (!listCells.isEmpty()) {
            clickOn(randomItem(listCells));
        }

    }

}
