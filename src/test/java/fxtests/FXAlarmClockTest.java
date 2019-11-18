package fxtests;

import static fxtests.FXTesting.measureTime;
import static utils.RunnableEx.make;

import ethical.hacker.AlarmClock;
import ethical.hacker.ImageCrackerApp;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.stage.Stage;
import org.junit.Test;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class FXAlarmClockTest extends AbstractTestExecution {
    @Test
    public void testAlarmClock() {
        Logger logger = getLogger();
        measureTime("AlarmClock.activateAlarmThenStop", () -> AlarmClock.scheduleToRun(LocalTime.now().plusMinutes(1),
            () -> logger.info("RUN AT {}", LocalTime.now())));
        measureTime("AlarmClock.activateAlarmThenStop", () -> AlarmClock.scheduleToRun(LocalTime.now().minusMinutes(1),
            () -> logger.info("RUN AT {}", LocalTime.now())));
    }

    @Test
    public void testImageCracker() {
        measureTime("AlarmClock.runImageCracker", () -> {
            getLogger().info("Main started.");
            RunnableEx run = () -> {
                ResourceFXUtils.initializeFX();
                Platform.runLater(make(() -> {
                    ImageCrackerApp imageCrackerApp = new ImageCrackerApp();
                    Stage stage = new Stage();
                    imageCrackerApp.start(stage);
                    BooleanProperty loadURL = imageCrackerApp.loadURL();
                    loadURL.addListener((ob, before, after) -> {
                        if (after) {
                            ImageCrackerApp.waitABit();
                            Platform.runLater(stage::close);
                        }
                    });
                }));
            };
            AlarmClock.scheduleToRun(LocalTime.of(9, 30), run);
            AlarmClock.scheduleToRun(LocalTime.of(9, 50), run);
            AlarmClock.scheduleToRun(LocalTime.of(12, 0), run);
            getLogger().info("Main ended.");
        });

    }
}
