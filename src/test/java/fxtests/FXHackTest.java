package fxtests;

import static fxtests.FXTesting.measureTime;

import ethical.hacker.*;
import java.util.Arrays;
import javafx.scene.control.Button;
import org.junit.Test;
import utils.ConsoleUtils;
import utils.ConsumerEx;

public class FXHackTest extends AbstractTestExecution {
	@Test
	public void testPortServices() {
		measureTime("PortServices.loadServiceNames", () -> PortServices.loadServiceNames());
		measureTime("PortServices.getServiceByPort", () -> PortServices.getServiceByPort(80));
		measureTime("PortScanner.isPortOpen", () -> PortScanner.isPortOpen(TracerouteScanner.IP_TO_SCAN, 80, 5000));
		measureTime("PortScanner.scanNetworkOpenPorts",
				() -> PortScanner.scanNetworkOpenPorts(TracerouteScanner.NETWORK_ADDRESS));
		measureTime("PortScanner.scanPortsHost", () -> PortScanner
				.scanNetworkOpenPorts(TracerouteScanner.NETWORK_ADDRESS, Arrays.asList(80, 9000, 8080)));
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
    }

}
