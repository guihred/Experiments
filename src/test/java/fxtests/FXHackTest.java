package fxtests;

import ethical.hacker.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import utils.*;

public class FXHackTest extends AbstractTestExecution {
    @Test
    public void testHashVerifier() {
        File userFolder = ResourceFXUtils.getUserFolder("Music");
        measureTime("HashVerifier.listNotRepeatedFiles", () -> HashVerifier
                .listNotRepeatedFiles(new File(userFolder, "PalavraCantada"), new File(userFolder, "Bita")));
        Path firstMp3 = ResourceFXUtils.getFirstPathByExtension(userFolder, ".mp3");
        measureTime("HashVerifier.getMD5Hash", () -> HashVerifier.getMD5Hash(firstMp3));
        measureTime("HashVerifier.getSha1Hash", () -> HashVerifier.getSha1Hash(firstMp3));
        measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash(firstMp3));
        measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash("whatever"));
    }

    @Test
    public void testInstallCert() {
        String string = "correiov3.dataprev.gov.br";
        measureTime("InstallCert.installCertificate", () -> InstallCert.installCertificate(string));
    }

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
    public void testWebBrowserApplication() {
        show(WebBrowserApplication.class);
        clickOn(lookupFirst(TextField.class));
        type(typeText("correiov3.dataprev.gov.br"));
        type(KeyCode.ENTER);
        sleep(2000);
        clickOn(lookupFirst(Button.class));
        sleep(1000);
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
    public void verifyMalwareInvestigator() {
        MalwareInvestigator show = show(MalwareInvestigator.class);
        List<TextField> fields = lookupList(TextField.class);
        List<Button> lookupList = lookupList(Button.class);
        this.clickOn(fields.get(0));
        type(typeText("111.229.255.22"));
        clickOn(lookupList.get(0));
        this.clickOn(fields.get(1));
        String randomItem = randomItem("safebrowsing.googleapis.com", "tracking-protection.cdn.mozilla.net",
                "shavar.services.mozilla.com", "lh6.googleusercontent.com", "lh3.googleusercontent.com",
                "people-pa.clients6.google.com", "people-pa.clients6.google.com", "clients6.google.com",
                "mail.google.com", "play.google.com", "http://wwwcztapwlwk.net/plafgxc80333067532");
        type(typeText(randomItem));
        clickOn(lookupList.get(1));
        File downloads = ResourceFXUtils.getUserFolder("Downloads");
        interactNoWait(() -> show.chooseDirectory(downloads));
        interactNoWait(RunnableEx.make(() -> {
            File userFolder = ResourceFXUtils.getFirstPathByExtension(downloads, ".exe").toFile();
            show.chooseExeFile(userFolder);
        }));
    }

    @Test
    public void verifyVirusTotalApi() {
        Path firstPathByExtension = measureTime("ResourceFXUtils.getFirstPathByExtension",
                () -> ResourceFXUtils.getRandomPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), ".exe"));
        measureTime("VirusTotalApi.getFilesInformation", () -> VirusTotalApi.getFilesInformation(firstPathByExtension));
        measureTime("VirusTotalApi.getIpInformation", () -> VirusTotalApi.getIpInformation("111.229.255.22"));
        String randomItem = randomItem("safebrowsing.googleapis.com", "tracking-protection.cdn.mozilla.net",
                "shavar.services.mozilla.com", "lh6.googleusercontent.com", "lh3.googleusercontent.com",
                "people-pa.clients6.google.com", "people-pa.clients6.google.com", "clients6.google.com",
                "mail.google.com", "play.google.com", "http://wwwcztapwlwk.net/plafgxc80333067532");
        measureTime("VirusTotalApi.getUrlInformation", () -> VirusTotalApi.getUrlInformation(randomItem));
    }

}
