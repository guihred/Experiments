package fxtests;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.apache.commons.lang.SystemUtils;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.testfx.api.FxRobotInterface;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import utils.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractTestExecution extends ApplicationTest implements HasLogging {
    protected static final int WAIT_TIME = 1000;
    protected Stage currentStage;
    protected boolean isLinux = SystemUtils.IS_OS_LINUX;

    private final Logger logger = HasLogging.super.getLogger();

    protected Random random = new Random();

    @Override
    public FxRobotInterface clickOn(Node node, MouseButton... buttons) {
        return SupplierEx.get(() -> super.clickOn(node, buttons));
    }

    @Override
    public FxRobotInterface clickOn(String node, MouseButton... buttons) {
        return SupplierEx.get(() -> super.clickOn(node, buttons));
    }

    @Override
    public FxRobotInterface doubleClickOn(Node node, MouseButton... buttons) {
        return SupplierEx.getIgnore(() ->super.doubleClickOn(node, buttons));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public FxRobotInterface moveTo(Node bounds) {
        return SupplierEx.get(()->super.moveTo(bounds));
    }
    @Override
    public void start(Stage stage) throws Exception {
        ResourceFXUtils.initializeFX();
        currentStage = stage != null ? stage : new Stage();
        currentStage.setX(0);
        currentStage.setY(0);
    }

    @Override
    public void stop() {
        interact(() -> currentStage.close());
    }

    public FxRobotInterface tryClickOn(Node node, MouseButton... buttons) {
        return SupplierEx.getIgnore(() -> super.clickOn(node, buttons));
    }

    protected void clickButtonsWait() {
        clickButtonsWait(WAIT_TIME);
    }

    protected void clickButtonsWait(int waitTime) {
        for (Node e : lookup(Button.class)) {
            clickOn(e);
            sleep(waitTime);
        }
    }

    protected MouseButton getRandMouseButton(int bound) {
        return random.nextInt(bound) != 0 ? MouseButton.PRIMARY : MouseButton.SECONDARY;
    }

    protected String getRandomString() {
        return Long.toString(Math.abs(random.nextLong()) + 1000, Character.MAX_RADIX).substring(0, 4);
    }

    protected <M extends Node> Set<M> lookup(Class<M> cl) {
        return lookup(cl::isInstance).queryAllAs(cl);
    }

    protected <M extends Node> M lookupFirst(Class<M> cl) {
        return lookup(cl::isInstance).queryAs(cl);
    }

    protected <M extends Node> List<M> lookupList(Class<M> cl) {
        return lookup(cl::isInstance).queryAllAs(cl).stream().collect(Collectors.toList());
    }

    protected <M extends Node> List<M> lookupList(Class<M> cl, Predicate<? super M> predicate) {
        return lookup(cl::isInstance).queryAllAs(cl).stream().filter(predicate).collect(Collectors.toList());
    }

    protected void measureTime(String name, RunnableEx runnable) {
        FXTesting.measureTime(getLogger(), name, runnable);
    }

    protected <T> T measureTime(String name, SupplierEx<T> runnable) {
        return FXTesting.measureTime(getLogger(), name, runnable);
    }

    protected void moveRandom(int bound) {
        moveBy(randomNumber(bound), randomNumber(bound));
    }

    protected void moveSliders(int bound) {
        for (Node m : lookup(Slider.class)) {
            randomDrag(m, bound);
        }
    }

    protected int nextInt(int bound) {
        return random.nextInt(bound);
    }

    protected void randomDrag(Node cube, int bound) {
        RunnableEx.ignore(() -> drag(cube, MouseButton.PRIMARY));
        moveRandom(bound);
        drop();
    }

    protected <T extends Enum<?>> T randomEnum(Class<T> cl) {
        T[] values = cl.getEnumConstants();
        return values[random.nextInt(values.length)];
    }

    protected <T> T randomItem(Collection<T> bound) {
        return randomItem(bound.stream().collect(Collectors.toList()));
    }

    protected <T> T randomItem(List<T> bound) {
        return bound.get(random.nextInt(bound.size()));
    }

    protected <T> T randomItem(@SuppressWarnings("unchecked") T... bound) {
        return bound[random.nextInt(bound.length)];
    }

    protected int randomNumber(int bound) {
        return random.nextInt(bound) - bound / 2;
    }

    protected <T> T randomRemoveItem(List<T> bound) {
        return bound.remove(random.nextInt(bound.size()));
    }

    protected void selectComboItems(ComboBox<?> e, int max) {
        for (int i = 0; i < max && i < e.getItems().size(); i++) {
            int j = i;
            interact(() -> e.getSelectionModel().select(j));
        }
    }

    protected <T extends Application> T show(Class<T> c) {
        return SupplierEx.remap(() -> {
            resetStage();
            logger.info("SHOWING {}", c.getSimpleName());
            T newInstance = c.newInstance();
            interactNoWait(RunnableEx.make(() -> newInstance.start(currentStage)));
            return newInstance;
        }, String.format("ERRO IN %s", c));
    }

    protected <T extends Application> void show(T application) {
        RunnableEx.remap(() -> {
            resetStage();
            logger.info("SHOWING {}", application.getClass().getSimpleName());
            interactNoWait(RunnableEx.make(() -> {
                application.start(currentStage);
                currentStage.toFront();
            }));
        }, String.format("ERRO IN %s", application));
    }

    protected <T extends Application> T showNewStage(Class<T> c, ConsumerEx<T> run) {
        return SupplierEx.remap(() -> {
            logger.info("SHOWING {}", c.getSimpleName());
            T newInstance = c.newInstance();
            Stage primaryStage = WaitForAsyncUtils.asyncFx(() -> new Stage()).get();
            interactNoWait(RunnableEx.make(() -> newInstance.start(primaryStage)));
            run.accept(newInstance);
            interactNoWait(primaryStage::close);
            return newInstance;
        }, String.format("ERRO IN %s", c));
    }

    protected <T extends Application> T showNewStage(Class<T> c, RunnableEx run) {
        return showNewStage(c, s -> run.run());
    }

    protected boolean tryClickButtons() {
        Set<Node> queryAll = lookup(".button").queryAll();
        queryAll.forEach(this::tryClickOn);
        return !queryAll.isEmpty();
    }

    @SuppressWarnings("deprecation")
    protected  KeyCode[] typeText(String txt) {
        KeyCode[] values = KeyCode.values();
        KeyCode[] array = txt.chars().mapToObj(e -> Objects.toString((char) e).toUpperCase())
            .flatMap(s -> Stream.of(values).filter(v -> v.impl_getChar().equals(s))).toArray(KeyCode[]::new);
        String string = Arrays.toString(array);
        logger.info("TYPING {}", string);
        return array;
    }

    protected void verifyAndRun(Runnable consumer, List<Class<? extends Application>> applicationClasses) {
        for (Class<? extends Application> class1 : applicationClasses) {
            show(class1);
            RunnableEx.make(() -> {
                interactNoWait(currentStage::toFront);
                consumer.run();
                resetStage();
            }, e -> getLogger().error(" ERROR RUN " + class1.getSimpleName(), e)).run();
        }
        interactNoWait(currentStage::close);
    }

    private void resetStage() {
        Platform.runLater(() -> {
            currentStage.setResizable(true);
            currentStage.setMaximized(false);
            currentStage.setAlwaysOnTop(false);
            currentStage.setMinWidth(0);
            currentStage.setFullScreen(false);
            currentStage.setMinHeight(0);
            currentStage.setIconified(false);
            currentStage.setOpacity(1);
            currentStage.close();
        });
    }

    @SafeVarargs
    public static void testApps(Class<? extends Application>... applicationClasses) {
        new FXTesting().testApplications(Arrays.asList(applicationClasses));
    }

    public static void testApps(List<Class<? extends Application>> applicationClasses) {
        new FXTesting().testApplications(applicationClasses);
    }

    protected static <T> void runReversed(List<T> list, Consumer<T> consu) {
        for (int i = list.size() - 1; i >= 0; i--) {
            T t = list.get(i);
            consu.accept(t);
        }
    }

}
