package fxtests;

import static javafx.scene.input.KeyCode.*;

import extract.PrintConfig;
import java.io.File;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import ml.data.CoverageUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import paintexp.ColorChooser;
import paintexp.PaintFileUtils;
import paintexp.PaintMain;
import paintexp.SimplePixelReader;
import paintexp.tool.AreaTool;
import paintexp.tool.PaintTool;
import paintexp.tool.PaintTools;
import utils.FileTreeWalker;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;
import utils.ex.ConsumerEx;
import utils.ex.RunnableEx;
import utils.fx.ZoomableScrollPane;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FXEnginePaintTest extends AbstractTestExecution {

    private static final String TEST_FILE = "test.png";

    @Test
    public void testColorChooser() {
        show(ColorChooser.class);
        Set<Slider> queryAll = lookup(Slider.class);
        int i = 0;
        for (Node m : queryAll) {
            if (i++ == 3) {
                lookup(".tab").queryAll().forEach(ConsumerEx.ignore(this::clickOn));
            }
            randomDrag(m, 10);
        }
        tryClickButtons();
    }
    @Test
    public void testLinePencilTool() {
        show(PaintMain.class);
        Node stack = lookupFirst(ZoomableScrollPane.class).getContent();
        List<PaintTool> asList =
                Arrays.asList(PaintTools.LINE.getTool(), PaintTools.PENCIL.getTool(), PaintTools.PICTURE.getTool());

        Map<String, Double> coverageMap = CoverageUtils.buildDataframe().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), Entry<Object, Double>::getValue));
        for (Node next : asList) {
            if (coverageMap.getOrDefault(next.getClass().getSimpleName(), 0.) >= 95) {
                continue;
            }
            clickOn(next);
            moveTo(stack);
            drag(MouseButton.PRIMARY);
            moveRandom(2);
            drop();
            holding(KeyCode.CONTROL, () -> type(KeyCode.ADD));
            type(typeText("M0,0L10,10"));
            type(KeyCode.ENTER);
            holding(KeyCode.SHIFT, () -> {
                drag(MouseButton.PRIMARY);
                moveRandom(1000);
                drop();
                drag(MouseButton.PRIMARY);
                moveTo(stack);
                drop();
            });
        }
    }

    @Test
    public void testMenus() {
        show(PaintMain.class);
        Node stack = lookupFirst(ZoomableScrollPane.class).getContent();
        ImageFXUtils.setShowImage(false);
        testMenus(stack);
    }

    @Test
    public void testPrintConfig() {
        show(PrintConfig.class);
        lookupList(Control.class, e -> !(e instanceof Button)).forEach(t -> {
            tryClickOn(t);
            sleep(500);
            type(KeyCode.ENTER);
        });
    }

    @Test
    public void testPrintConfig2() {
        PrintConfig show = show(PrintConfig.class);
        show.addFiles(FileTreeWalker.getPathByExtension(ResourceFXUtils.getOutFile(), ".png", "jpg", ".jpeg").stream()
                .map(e -> e.toFile()).collect(Collectors.toList()));
    }

    @Test
    public void testSimplePixelReader() {
        measureTime("SimplePixelReader.test", () -> {
            SimplePixelReader.paintColor(new WritableImage(10, 10), Color.BLACK);
            SimplePixelReader reader = new SimplePixelReader(Color.BLACK);
            reader.getArgb(0, 0);
            reader.getColor(0, 0);
            reader.setColor(Color.WHITE);
            WritablePixelFormat<IntBuffer> pixelFormat = reader.getPixelFormat();
            reader.getPixels(0, 0, 1, 1, pixelFormat, new int[] { 0, 0 }, 0, 0);
            reader.getPixels(0, 0, 1, 1, PixelFormat.getByteBgraInstance(), new byte[] { 0, 0, 0, 0 }, 0, 0);
        });
    }

    @Test
    public void testTools() {
        show(PaintMain.class);
        Node stack = lookupFirst(ZoomableScrollPane.class).getContent();
        testTools(stack);
    }

    protected void testTools(final Node stack) {
        List<ToggleButton> queryAll = lookupList(ToggleButton.class);
        Collections.shuffle(queryAll);
        List<Node> colors = lookup("#colorGrid").queryAllAs(GridPane.class).stream()
                .flatMap(e -> e.getChildren().stream()).collect(Collectors.toList());
        int bound = (int) (stack.getBoundsInParent().getWidth() / 3);
        List<KeyCode> testCodes = Arrays.asList(DELETE, V, C, X, A, RIGHT, LEFT, DOWN, UP, ADD, SUBTRACT);
        if (!colors.isEmpty()) {
            RunnableEx.ignore(() -> doubleClickOn(colors.remove(nextInt(colors.size())), MouseButton.PRIMARY));
            tryClickButtons();
        }
        List<String> collect = queryAll.stream().map(e -> e.getUserData()).map(e -> e.getClass().getSimpleName())
                .collect(Collectors.toList());

        Map<String, Double> coverageMap =
                CoverageUtils.buildDataframe().stream().filter(e -> collect.contains(e.getKey()))
                        .collect(Collectors.toMap(e -> e.getKey().toString(), Entry<Object, Double>::getValue));
        for (Node next : queryAll) {
            Object userData = next.getUserData();
            String simpleName = userData.getClass().getSimpleName();
            if (coverageMap.getOrDefault(simpleName, 0.) >= 95) {
                continue;
            }
            getLogger().info("Testing {} ", simpleName);
            if (!colors.isEmpty()) {
                tryClickOn(colors.remove(nextInt(colors.size())), getRandMouseButton(5));
            }
            clickOn(next);
            lookup("#tools .slider").queryAll().forEach(f -> {
                drag(f, MouseButton.PRIMARY);
                moveBy(randomNumber(50), 0);
                drop();
            });
            moveTo(stack);
            drag(getRandMouseButton(5));
            moveRandom(bound);
            drop();
            if (userData instanceof AreaTool) {
                testAreaTools(testCodes);
            }
            lookup(".text-area").queryAll().forEach(e -> write(getRandomString()));
            moveRandom(bound);
            drag(getRandMouseButton(5));
            moveRandom(bound);
            drop();
            for (ComboBox<?> node : lookup("#tools .combo-box").queryAllAs(ComboBox.class)) {
                selectComboItems(node, 10);
            }
            Set<Node> queryAll2 = lookup("#tools .toggle-button").queryAll();
            queryAll2.forEach(e -> {
                tryClickOn(e);
                scroll(1, VerticalDirection.DOWN);
                moveTo(stack);
                moveRandom(bound);
                drag(getRandMouseButton(5));
                moveRandom(bound);
                moveRandom(bound);
                drop();
                drag(getRandMouseButton(5));
                moveRandom(bound);
                drop();
                lookup(".text-area").queryAll().forEach(f -> {
                    interactNoWait(() -> f.requestFocus());
                    write(getRandomString());
                });
                type(KeyCode.ESCAPE);
            });
        }
    }

    private void addClipboarImage() {
        interact(() -> {
            List<Path> pathByExtension =
                    FileTreeWalker.getPathByExtension(ResourceFXUtils.getOutFile(), ".png");
            if (!pathByExtension.isEmpty()) {
                Map<DataFormat, Object> content = FXCollections.observableHashMap();
                Path path = randomItem(pathByExtension);
                content.put(DataFormat.FILES, Arrays.asList(path.toFile()));
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
    }

    private void testAreaTools(List<KeyCode> testCodes) {
        boolean nextBoolean = nextBoolean();
        if (nextBoolean) {
            press(KeyCode.CONTROL);
        }
        typingTest(nextBoolean, testCodes);
        typingTest(nextBoolean, testCodes);
        typingTest(nextBoolean, testCodes);
        typingTest(nextBoolean, testCodes);
        if (nextBoolean) {
            release(KeyCode.CONTROL);
        }
    }

    private void testMenus(final Node stack) {
        File defaultFile = ResourceFXUtils.getOutFile();
        PaintFileUtils.setDefaultFile(defaultFile);
        File file = new File(defaultFile, TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
        List<PaintTool> areaTools =
                Stream.of(PaintTools.values()).map(PaintTools::getTool).collect(Collectors.toList());

        List<MenuButton> node = lookupList(MenuButton.class);
        addClipboarImage();
        for (int i = 0; i < node.size(); i++) {
            MenuButton menuButton = node.get(i);
            ObservableList<MenuItem> items = menuButton.getItems();
            for (int j = items.size() - 1; j >= 0; j--) {
                clickOn(randomItem(areaTools));
                MenuItem menu = items.get(j);
                moveTo(stack);
                if (nextBoolean()) {
                    double bound2 = stack.getBoundsInParent().getWidth();
                    moveBy(-bound2 / 4, -bound2 / 4);
                    drag(MouseButton.PRIMARY);
                    moveBy(bound2 / 2, bound2 / 2);
                    drop();
                }

                getLogger().info("FIRING {}", menu.getId());
                if (i == 0 && j > 0 && items.size() != j + 1) {
                    // OPEN and save as
                    RunnableEx.runNewThread(() -> typeInParallel(TEST_FILE));
                }
                if (i == 1 && j == 2) {
                    interact(menu::fire);
                }
                interactNoWait(RunnableEx.make(menu::fire));
                if (i == 0 && items.size() == j + 1) {
                    // PRINT
                    tryClickOn(lookupFirst(Button.class));
                    typeInParallel("test.pdf");
                    type(KeyCode.ESCAPE);
                }
                if (i == 0 && j == 4) {
                    lookup(Button.class).forEach(this::tryClickOn);
                    lookup(ComboBox.class).forEach(ConsumerEx.ignore(
                            m -> interactNoWait(() -> m.getSelectionModel().select(nextInt(m.getItems().size())))));
                    lookup(Button.class).forEach(this::tryClickOn);
                }

                lookup(".text-field").queryAll().forEach(e -> {
                    clickOn(e);
                    eraseText(3);
                    write("" + (nextInt(120) + 20));
                });
                lookup("Resize").queryAll().forEach(this::clickOn);
                lookup(".slider").queryAll().forEach(ConsumerEx.ignore(m -> {
                    drag(m, MouseButton.PRIMARY);
                    moveBy(randomNumber(50), 0);
                    drop();
                }));

                lookup("Adjust").queryAll().forEach(t -> {
                    tryClickOn(t);
                    tryClickOn(randomItem(areaTools));
                });
                type(KeyCode.ESCAPE);
            }
        }
    }

    private void typeInParallel(String testFile) {
        sleep(3500);
        type(typeText(testFile));
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
    }

    private void typingTest(boolean ctrlDown, List<KeyCode> testCodes) {
        KeyCode randomItem = randomItem(testCodes);
        getLogger().info("TYPING {} ", ctrlDown ? "CTRL+" + randomItem : randomItem);
        type(randomItem);
    }

}
