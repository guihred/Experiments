package fxtests;

import static fxtests.FXTesting.measureTime;

import ethical.hacker.*;
import java.time.LocalTime;
import java.util.Arrays;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import org.junit.Test;
import org.slf4j.Logger;
import utils.ConsoleUtils;
import utils.ConsumerEx;
import utils.RunnableEx;

public class FXHackTest extends AbstractTestExecution {
    @Test
    public void testAlarmClock() {
        Logger logger = getLogger();
        measureTime("AlarmClock.activateAlarmThenStop", () -> AlarmClock.scheduleToRun(LocalTime.now().plusMinutes(1),
            () -> logger.info("RUN AT {}", LocalTime.now())));
        measureTime("AlarmClock.activateAlarmThenStop", () -> AlarmClock.scheduleToRun(LocalTime.now().minusMinutes(1),
            () -> logger.info("RUN AT {}", LocalTime.now())));
        measureTime("AlarmClock.runImageCracker", () -> AlarmClock.runImageCracker());
    }

    @Test
    @SuppressWarnings("static-method")
    public void testPortServices() {
        measureTime("PortServices.loadServiceNames", () -> PortServices.loadServiceNames());
        measureTime("PortServices.getServiceByPort", () -> PortServices.getServiceByPort(80));
        measureTime("PortScanner.isPortOpen", () -> PortScanner.isPortOpen(TracerouteScanner.IP_TO_SCAN, 80, 5000));
        measureTime("PortScanner.scanNetworkOpenPorts",
            () -> PortScanner.scanNetworkOpenPorts(TracerouteScanner.NETWORK_ADDRESS));
        measureTime("PortScanner.scanPortsHost",
            () -> PortScanner.scanNetworkOpenPorts(TracerouteScanner.NETWORK_ADDRESS, Arrays.asList(80, 9000, 8080)));
        measureTime("PortScanner.scanPortsHost", () -> PortScanner.scanPortsHost(TracerouteScanner.IP_TO_SCAN));
        measureTime("PortScanner.scanPossibleOSes",
            () -> PortScanner.scanPossibleOSes(TracerouteScanner.NETWORK_ADDRESS));
        measureTime("ProcessScan.scanProcesses", () -> ProcessScan.scanProcesses());
    }

    @Test
    public void verifyEthicalHack() {
        show(EthicalHackApp.class);
        lookup(".button").queryAllAs(Button.class).stream().filter(e -> !"Ips".equals(e.getText()))
            .forEach(ConsumerEx.ignore(this::clickOn));
        ConsoleUtils.waitAllProcesses();
        RunnableEx.ignore(() -> clickOn(lookupFirst(CheckBox.class)));
    }

}
