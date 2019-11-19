package ethical.hacker;

import static utils.RunnableEx.make;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.RunnableEx;

/**
 * Run a simple task once every second, starting 3 seconds from now. Cancel the
 * task after 20 seconds.
 */
public final class AlarmClock {

    private static final Logger LOG = HasLogging.log();
    /**
     * If invocations might overlap, you can specify more than a single thread.
     */
    private static final List<LocalTime> SCHEDULED_TASKS = new LinkedList<>();
    private static final int NUM_THREADS = 1;

    private AlarmClock() {
    }

    public static void scheduleToRun(LocalTime time, RunnableEx run) {
        SCHEDULED_TASKS.add(time);
        long initialDelay = calculateDelay(time);
        if (initialDelay < 0) {
            LOG.info("{} skipped", time);
            return;
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        Runnable soundAlarmTask = () -> {
            make(run).run();
            LOG.info("Executed in {}", LocalTime.now());
            SCHEDULED_TASKS.remove(time);
            if (SCHEDULED_TASKS.isEmpty()) {
                scheduler.shutdown();
            }
        };
        scheduler.schedule(soundAlarmTask, initialDelay, TimeUnit.SECONDS);
    }

    private static long calculateDelay(LocalTime of) {
        return ChronoUnit.SECONDS.between(LocalTime.now(), of);
    }

}
