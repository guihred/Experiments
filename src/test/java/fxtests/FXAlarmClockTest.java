package fxtests;

import ethical.hacker.AlarmClock;
import java.time.LocalTime;
import org.junit.Test;
import org.slf4j.Logger;

public class FXAlarmClockTest extends AbstractTestExecution {
    @Test
    public void testAlarmClock() {
        Logger logger = getLogger();
        measureTime("AlarmClock.activateAlarmThenStop", () -> AlarmClock.scheduleToRun(LocalTime.now().plusMinutes(1),
            () -> logger.info("RUN AT {}", LocalTime.now())));
        measureTime("AlarmClock.activateAlarmThenStop", () -> AlarmClock.scheduleToRun(LocalTime.now().minusMinutes(1),
            () -> logger.info("RUN AT {}", LocalTime.now())));
    }


}
