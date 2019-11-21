package fxtests;

import static javafx.scene.input.KeyCode.*;

import fxpro.ch02.PathTransitionExample;
import fxpro.ch02.PendulumAnimationLauncher;
import fxpro.ch02.PongLauncher;
import fxpro.ch02.RandomPathExample;
import fxpro.ch04.ReversiMain;
import fxpro.ch04.ReversiSquare;
import fxpro.ch04.ReversiSquareApp;
import fxpro.ch06.ResponsiveUIApp;
import fxpro.ch06.ThreadInformationApp;
import fxpro.ch07.Chart3dSampleApp;
import fxsamples.*;
import fxsamples.bounds.BoundsPlayground;
import fxsamples.person.PersonTableController;
import fxsamples.person.WorkingWithTableView;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import utils.RunnableEx;

public class FXEngineSampleTest extends AbstractTestExecution {
    @Test
    public void verifyBackgroundProcesses() {
        show(BackgroundProcesses.class);
        clickButtonsWait(WAIT_TIME * nextInt(10));
    }

    @Test
    public void verifyBoundsPlayground() {
        show(BoundsPlayground.class);
        lookup(CheckBox.class).forEach(this::clickOn);
        lookup(RadioButton.class).forEach(this::clickOn);
        lookup(Circle.class).forEach(s -> randomDrag(s, 100));
    }

    @Test
    public void verifyInlineModelViewer() {
        show(InlineModelViewer.class);
        lookup(CheckBox.class).forEach(this::clickOn);
        lookup(CheckBox.class).forEach(this::clickOn);
    }

    @Test
    public void verifyLineManipulator() {
        show(LineManipulator.class);
        lookup(AnchorCircle.class).forEach(e -> randomDrag(e, 300));
    }

    @Test
    public void verifyLookNFeelChooser() {
        show(LookNFeelChooser.class);
        List<MenuButton> node = lookupList(MenuButton.class);
        runReversed(node, menuButton -> {
            ObservableList<MenuItem> items = menuButton.getItems();
            for (int j = 0; j < items.size(); j++) {
                MenuItem menuItem = items.get(j);
                interact(menuItem::fire);
            }
        });
    }

    @Test
    public void verifyMouseMovements() {
        verifyAndRun(() -> {
            moveTo(200, 200);
            moveBy(-1000, 0);
            moveBy(1000, 0);
            type(W, 20);
            Node query = lookup(".root").query();
            randomDrag(query, 200);
            for (KeyCode keyCode : Arrays.asList(W, S, A, DOWN, D, UP, R, L, U, D, B, F, Z, X, LEFT, RIGHT, SPACE)) {
                press(keyCode).release(keyCode);
                press(CONTROL, keyCode).release(keyCode);
                press(ALT, keyCode).release(keyCode);
                press(SHIFT, keyCode).release(keyCode);
                release(CONTROL, ALT, SHIFT);
            }
        }, Arrays.asList(JewelViewer.class, MoleculeSampleApp.class, Chart3dSampleApp.class, SimpleScene3D.class));
    }

    @Test
    public void verifyPendulums() {
        verifyAndRun(this::tryClickButtons,
            Arrays.asList(PendulumAnimationLauncher.class, PathTransitionExample.class, RandomPathExample.class));
    }

    @Test
    public void verifyPersonTableController() {
        show(PersonTableController.class);
        clickOn(lookupFirst(TextField.class));
        String randomString = getRandomString();
        type(typeText(randomString));
        eraseText(randomString.length());
        randomString = getRandomString();
        type(typeText(randomString));
        eraseText(randomString.length());
    }

    @Test
    public void verifyPlatformMain() {
        show(RaspiCycle.class);
        for (KeyCode keyCode : Arrays.asList(DOWN, UP, LEFT, RIGHT, SPACE, ESCAPE, DIGIT1, DIGIT2, DIGIT3)) {
            type(keyCode, nextInt(5));
        }
    }

    @Test
    public void verifyPong() {
        show(PongLauncher.class);
        tryClickButtons();
        for (Node next : lookupList(Rectangle.class, e -> e.isVisible())) {
            drag(next, MouseButton.PRIMARY);
            moveBy(0, 40);
            moveBy(0, -40);
            drop();
        }
        type(KeyCode.A, KeyCode.COMMA, KeyCode.Z, KeyCode.L, KeyCode.F);
        sleep(1000);
        type(KeyCode.A, KeyCode.COMMA, KeyCode.Z, KeyCode.L, KeyCode.F);
    }

    @Test
    public void verifyResponsiveUIApp() {
        show(ResponsiveUIApp.class);
        tryClickButtons();
        tryClickButtons();
    }

    @Test
    public void verifyReversiMain() {
        show(ReversiMain.class);
        ReversiSquare lookupFirst = lookupFirst(ReversiSquare.class);
        clickOn(lookupFirst);
    }

    @Test
    public void verifyReversiSquareApp() {
        show(ReversiSquareApp.class);
        for (ReversiSquare reversiSquare : lookup(ReversiSquare .class)) {
            tryClickOn(reversiSquare);
        }
    }

    @Test
    public void verifyScrollChart() {
        show(Chart3dSampleApp.class);
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
    public void verifySimpleScene3D() {
        show(SimpleScene3D.class);
        Node query = lookup(".root").query();
        randomDrag(query, 100);
    }

    @Test
    public void verifyThreadInformationApp() {
        show(ThreadInformationApp.class);
        tryClickButtons();
        sleep(500);
        ListCell<?> randomItem = randomItem(lookup(ListCell.class));
        clickOn(randomItem);
        clickOn(randomItem);
    }

    @Test
    public void verifyWorkingWithTableView() {
        show(WorkingWithTableView.class);
        @SuppressWarnings("rawtypes")
        List<ListCell> listCells = lookupList(ListCell.class, c -> StringUtils.isNotBlank(c.getText()));
        if (!listCells.isEmpty()) {
            clickOn(randomItem(listCells));
        }
    }
}
