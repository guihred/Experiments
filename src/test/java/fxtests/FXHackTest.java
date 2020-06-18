package fxtests;

import static fxtests.FXTesting.measureTime;

import ethical.hacker.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import org.junit.Test;
import schema.sngpc.JsonViewer;
import utils.ConsoleUtils;
import utils.ExtractUtils;
import utils.ImageFXUtils;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class FXHackTest extends AbstractTestExecution {

    @Test
    public void testPortServices() {
        measureTime("PortServices.loadServiceNames", () -> PortServices.loadServiceNames());
        measureTime("PortServices.getServiceByPort", () -> PortServices.getServiceByPort(80));
        measureTime("PortScanner.isPortOpen", () -> ExtractUtils.isPortOpen(TracerouteScanner.IP_TO_SCAN, 80, 5000));
        measureTime("PortScanner.scanNetworkOpenPorts",
                () -> PortScanner.scanNetworkOpenPorts(TracerouteScanner.NETWORK_ADDRESS));
        measureTime("PortScanner.scanPortsHost", () -> PortScanner
                .scanNetworkOpenPorts(TracerouteScanner.NETWORK_ADDRESS, Arrays.asList(80, 9000, 8080)));
        measureTime("PortScanner.scanPortsHost", () -> PortScanner.scanPortsHost(TracerouteScanner.IP_TO_SCAN));
        measureTime("PortScanner.scanPossibleOSes",
                () -> PortScanner.scanPossibleOSes(TracerouteScanner.NETWORK_ADDRESS));
        measureTime("PingTraceRoute.traceRoute", () -> PingTraceRoute.traceRoute(TracerouteScanner.NETWORK_ADDRESS));
        String collect = IntStream.range(0, 4).map(e -> nextInt(256)).mapToObj(Objects::toString)
                .collect(Collectors.joining("."));
        measureTime("PingTraceRoute.traceRoute", () -> PingTraceRoute.traceRoute(collect));
        measureTime("ProcessScan.scanProcesses", () -> ProcessScan.scanNetstats());

        measureTime("NetworkInformationScanner.displayNetworkInformation",
                () -> NetworkInformationScanner.displayNetworkInformation());
        measureTime("NetworkInformationScanner.displayNetworkInformation",
                () -> NetworkInformationScanner.displayNetworkInformation());
    }

    @Test
    public void verifyEthicalHack() {
        // EthicalHackController
        ImageFXUtils.setShowImage(false);
        show(EthicalHackController.class);
        lookup(".button").queryAllAs(Button.class).stream().filter(e -> !"Ips".equals(e.getText()))
                .forEach(this::tryClickOn);
        ConsoleUtils.waitAllProcesses();
        tryClickOn(lookupFirst(CheckBox.class));
    }

    @Test
    public void verifyHashDigest() {
        Path firstPathByExtension = measureTime("ResourceFXUtils.getFirstPathByExtension",
                () -> ResourceFXUtils.getRandomPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), ".exe"));
        measureTime("HashVerifier.getMD5Hash", () -> HashVerifier.getMD5Hash(firstPathByExtension));
        String sha1Hash = measureTime("HashVerifier.getSha1Hash", () -> HashVerifier.getSha1Hash(firstPathByExtension));
        measureTime("HashVerifier.hashLookup", () -> HashVerifier.hashLookup(sha1Hash).html());
        String sha256Hash =
                measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash(firstPathByExtension));
        measureTime("HashVerifier.virusTotal", () -> HashVerifier.virusTotal(sha256Hash).html());
    }

    @Test
    public void verifyVirusTotalApi() {
        Path firstPathByExtension = measureTime("ResourceFXUtils.getFirstPathByExtension",
                () -> ResourceFXUtils.getRandomPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), ".exe"));
        File[] filesInformation =
                measureTime("VirusTotalApi.getFilesInformation",
                        () -> VirusTotalApi.getFilesInformation(firstPathByExtension));
        JsonViewer show = show(JsonViewer.class);
        show.addFile(filesInformation);
    }

}
