package fxtests;

import static javafx.scene.input.KeyCode.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.junit.Test;
import paintexp.PaintFileUtils;
import paintexp.PaintMain;
import paintexp.tool.AreaTool;
import utils.ConsumerEx;
import utils.ResourceFXUtils;
import utils.ZoomableScrollPane;

public class FXEnginePaintTest extends AbstractTestExecution {

    private static final String TEST_FILE = "test.png";

    private Random random = new Random();

    @Test
    public void testaToolsVerify() throws Exception {
        show(PaintMain.class);
        Node stack = lookup(e -> e instanceof ZoomableScrollPane).queryAs(ZoomableScrollPane.class).getContent();
        testTools(stack);
        testMenus(stack);
    }

    protected void testTools(final Node stack) {
        List<Node> queryAll = lookup(e -> e instanceof ToggleButton).queryAll().stream().collect(Collectors.toList());
        Collections.shuffle(queryAll);
        List<Node> colors = lookup("#colorGrid").queryAllAs(GridPane.class).stream()
            .flatMap(e -> e.getChildren().stream()).collect(Collectors.toList());
        int bound = (int) (stack.getBoundsInParent().getWidth() / 3);
        List<KeyCode> testCodes = Arrays.asList(DELETE, V, C, X, A, RIGHT, LEFT, DOWN, UP);

        for (Node next : queryAll) {
            Object userData = next.getUserData();
            if (userData != null) {
                getLogger().info("Testing {} ", userData.getClass().getSimpleName());
            }
            if (!colors.isEmpty()) {
                clickOn(colors.remove(random.nextInt(colors.size())),
                    random.nextInt(5) != 0 ? MouseButton.PRIMARY : MouseButton.SECONDARY);
            }
            clickOn(next);
            lookup("#tools .slider").queryAll().forEach(f -> {
                drag(f, MouseButton.PRIMARY);
                moveBy(randomMove(50), 0);
                drop();
            });
            moveTo(stack);
            drag(MouseButton.PRIMARY);
            moveBy(randomMove(bound), randomMove(bound));
            drop();

			if (userData instanceof AreaTool) {
                if (random.nextBoolean()) {
                    press(KeyCode.CONTROL);
                }
                type(testCodes.get(random.nextInt(testCodes.size())));
                type(testCodes.get(random.nextInt(testCodes.size())));
                type(testCodes.get(random.nextInt(testCodes.size())));
                type(testCodes.get(random.nextInt(testCodes.size())));
                release(KeyCode.CONTROL);
            }

            lookup(".text-area").queryAll().forEach(e -> write(getRandomString()));
            moveBy(randomMove(bound), randomMove(bound));
            drag(MouseButton.PRIMARY);
            moveBy(randomMove(bound), randomMove(bound));
            drop();
            Set<Node> queryAll2 = lookup("#tools .toggle-button").queryAll();
            queryAll2.forEach(e -> {
                ConsumerEx.ignore((Node f) -> clickOn(f)).accept(e);
                scroll(1, VerticalDirection.DOWN);
                moveTo(stack);
                moveBy(randomMove(bound), randomMove(bound));
                drag(MouseButton.PRIMARY);
                moveBy(randomMove(bound), randomMove(bound));
                moveBy(randomMove(bound), randomMove(bound));
                drop();
                drag(MouseButton.PRIMARY);
                moveBy(randomMove(bound), randomMove(bound));
                drop();
                lookup(".text-area").queryAll().forEach(f -> {
                    interactNoWait(() -> f.requestFocus());
                    write(getRandomString());
                });
                type(KeyCode.ESCAPE);
            });
        }
    }

    private String getRandomString() {
        return Long.toString(Math.abs(random.nextLong()) + 1000, Character.MAX_RADIX).substring(0, 4);
    }

    private int randomMove(int bound) {
        return random.nextInt(bound) - bound / 2;
    }

    private void testMenus(final Node stack) {
        List<MenuButton> node = lookup(MenuButton.class::isInstance).queryAllAs(MenuButton.class).stream()
            .collect(Collectors.toList());
        File defaultFile = ResourceFXUtils.toFile("out");
        PaintFileUtils.setDefaultFile(defaultFile);
        File file = new File(defaultFile, TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
        lookup("#SelectRectTool").queryAll().forEach(this::clickOn);
        for (int i = 0; i < node.size(); i++) {
            ObservableList<MenuItem> items = node.get(i).getItems();
            for (int j = items.size() - 1; j >= 0; j--) {
                if (i == 0 && j > 0 && items.size() != j + 1) {
                    continue;
                }
                MenuItem menu = items.get(j);
                moveTo(stack);
                double bound2 = stack.getBoundsInParent().getWidth();
                moveBy(-bound2 / 4, -bound2 / 4);
                drag(MouseButton.PRIMARY);
                moveBy(bound2 / 2, bound2 / 2);
                drop();
                if (i == 0 && items.size() == j + 1) {
                    new Thread(() -> typeInParallel()).start();
                }
                interact(menu::fire);
                lookup(".text-field").queryAll().forEach(e -> {
                    clickOn(e);
                    eraseText(3);
                    write("" + (random.nextInt(120) + 20));
                });
                lookup("Resize").queryAll().forEach(this::clickOn);
                lookup(".slider").queryAll().forEach(m -> {
                    drag(m, MouseButton.PRIMARY);
                    moveBy(randomMove(50), 0);
                    drop();
                });
                lookup("Adjust").queryAll().forEach(t -> {
                    clickOn(t);
                    lookup("#SelectRectTool").queryAll().forEach(this::clickOn);
                });
                type(KeyCode.ESCAPE);
            }
        }
    }

    private void typeInParallel() {
        sleep(500);
		type(typeText(TEST_FILE));
        type(KeyCode.ENTER);
    }

}
