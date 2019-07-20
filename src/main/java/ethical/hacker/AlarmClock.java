package ethical.hacker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

/**
 * Run a simple task once every second, starting 3 seconds from now. Cancel the
 * task after 20 seconds.
 */
public final class AlarmClock {

    private static final Logger LOG = HasLogging.log();
    /**
     * If invocations might overlap, you can specify more than a single thread.
     */
    private static final int NUM_THREADS = 1;

    /*
     * To start the alarm at a specific date in the future, the initial delay needs
     * to be calculated relative to the current time, as in : Date futureDate = ...
     * long startTime = futureDate.getTime() - System.currentTimeMillis();
     * AlarmClock alarm = new AlarmClock(startTime, 1, 20); This works only if the
     * system clock isn't reset.
     */
    public static void main(String... args) throws InterruptedException {
        log("Main started.");
        AlarmClock.activateAlarmThenStop(LocalTime.of(12, 0), () -> {
            ResourceFXUtils.initializeFX();
            Platform.runLater(() -> {
                try {
                    ImageCrackerApp imageCrackerApp = new ImageCrackerApp();
                    imageCrackerApp.start(new Stage());
                    imageCrackerApp.loadURL();
                } catch (Exception e) {
                    LOG.error("", e);
                }
            });
        });

        log("Main ended.");
    }

    static void activateAlarmThenStop(LocalTime Delay, Runnable run) {
        long initialDelay = calculateDelay(Delay);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        Runnable soundAlarmTask = () -> {
            run.run();
            scheduler.shutdown();
        };
        scheduler.schedule(soundAlarmTask, initialDelay, TimeUnit.SECONDS);
    }

    private static long calculateDelay(LocalTime of) {
        Instant instant = LocalDate.now().atTime(of).atZone(ZoneId.systemDefault()).toInstant();
        long epochSecond = instant.getEpochSecond();
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        return epochSecond - currentTimeMillis;
    }

    private static void log(String msg) {
        LOG.info(msg);
    }

}
