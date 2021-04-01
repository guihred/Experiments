package fxtests;

import ethical.hacker.*;
import extract.web.HashVerifier;
import extract.web.InstallCert;
import extract.web.VirusTotalApi;
import extract.web.WebScanner;
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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import kibana.CredentialInvestigator;
import ml.data.DataframeML;
import ml.data.DataframeUtils;
import ml.graph.ExplorerHelper;
import org.junit.Test;
import utils.*;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class FXHackTest extends AbstractTestExecution {
    @Test
    public void takeScreenshots() {
        List<String> asList = Arrays.asList("consultaprocessos.inss.gov.br");
        // List<String> asList = Arrays.asList("caged.maisemprego.mte.gov.br",
        // "www3.dataprev.gov.br", "dataprev.gov.br",
        // "ppfacil.dataprev.gov.br", "caged.maisemprego.mte.gov.br",
        // "vip-pgerid01.dataprev.gov.br",
        // "geriddtp.dataprev.gov.br", "saa.previdencia.gov.br",
        // "psdcwlg.dataprev.gov.br",
        // "captcha.dataprev.gov.br", "geridinss.dataprev.gov.br",
        // "pssomteapr01.dataprev.gov.br",
        // "tarefas.inss.gov.br", "www11.dataprev.gov.br", "meu.inss.gov.br",
        // "vip-agendamentoapr01.inss.gov.br",
        // "pservicoexternoapr01.dataprev.gov.br", "vip-pmeuinssprxr.inss.gov.br",
        // "vip-psat.inss.gov.br",
        // "vip-auxilioemergencial.dataprev.gov.br", "portal.dataprev.gov.br",
        // "vip-ppmf.inss.gov.br",
        // "mobdigital.inss.gov.br", "vip-ppmfapr03.dataprev.gov.br",
        // "consultacadastral.inss.gov.br",
        // "www5.dataprev.gov.br", "www2.dataprev.gov.br", "www9.dataprev.gov.br",
        // "pcnisweb01.dataprev.gov.br",
        // "pcnisappweb01.inss.gov.br", "pesocialweb01.dataprev.gov.br",
        // "b2b.dataprev.gov.br",
        // "www8.dataprev.gov.br", "www6.dataprev.gov.br",
        // "vip-pcomprevohs.inss.gov.br",
        // "vip-pcomprevapacheinter.inss.gov.br",
        // "vip-auxilioemergencial.dataprev.gov.br",
        // "portal.dataprev.gov.br", "vip-auxilio-emergencial-gerencia.dataprev.gov.br",
        // "extratoir-weblog-prod.inss.gov.br", "psispagbenapr.dataprev.gov.br",
        // "vip-sisgpep-prod.inss.gov.br",
        // "vip-psisrec.inss.gov.br", "www99.dataprev.gov.br",
        // "vip-pcniswebapr02.inss.gov.br",
        // "vip-pedocapr01.dataprev.gov.br", "dadosabertos.dataprev.gov.br",
        // "edoc.inss.gov.br",
        // "ppfacil.dataprev.gov.br", "edoc-mobile.dataprev.gov.br",
        // "vip-pmoodle.dataprev.gov.br",
        // "edoc4.inss.gov.br", "www-ohsrevartrecben.dataprev.gov.br",
        // "vip-pcoaf.dataprev.gov.br",
        // "gru.inss.gov.br", "rppss.cnis.gov.br", "vip-psiacwebapr01.dataprev.gov.br",
        // "vip-psiacproxyrev.dataprev.gov.br", "homol-store.dataprev.gov.br",
        // "store.dataprev.gov.br",
        // "degustacao.dataprev.gov.br", "vip-psicapweb.dataprev.gov.br",
        // "sinpat.dataprev.gov.br",
        // "pportalmaisemprego.dataprev.gov.br", "vip-psineaberto.dataprev.gov.br",
        // "mte-auto-atendimento.dataprev.gov.br",
        // "mte-posto-atendimento.dataprev.gov.br");
        WebScanner whoIsScanner = new WebScanner();
        String url = randomItem(asList);
        measureTime("HashVerifier.renderPage", () -> {
            RunnableEx.run(() -> whoIsScanner.name(url).waitStr("Please wait...")
                    .subFolder("#gradeA", "#warningBox", "ratingTitle", "reportTitle").evaluateURL(
                            "https://www.ssllabs.com/ssltest/analyze.html?d=" + url + "&ignoreMismatch=on&latest"));
        });
    }

    @Test
    public void testAcesso() {

        measureTime("InstallCert.installCertificate", () -> InstallCert.installCertificate("www-acesso"));
        measureTime("Acesso", () -> {
            String credencial = "70812788176";
            return CredentialInvestigator.getCredentialInfo(credencial);

        });
    }

    @Test
    public void testCurl() {
        measureTime("CurlUtils", () -> {
            new CurlUtils().userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0")
                    .cookies(ResourceFXUtils.getOutFile("html/cookies.txt"))
                    .saveToFile(ResourceFXUtils.getOutFile("html/test.html"))
                    .url("https://www.google.com/").run()
                    .asDocument();
        });
    }

    @Test
    public void testFillIP() {
        File csvFile = new File(
                "C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\Downs\\[Acesso Web] Top Origens x URL Únicas acessadas.csv");
        measureTime("WhoIsScanner.fillIPInformation", () -> {
            DataframeML dataframe = ExplorerHelper.fillIPInformation(csvFile);
            String reorderAndLog =
                    ExplorerHelper.reorderAndLog(dataframe, ExplorerHelper.getLastNumberField(dataframe));
            getLogger().info("{}", reorderAndLog);
            DataframeUtils.save(dataframe, ResourceFXUtils.getOutFile("csv/" + csvFile.getName()));
        });

    }

    @Test
    public void testHashVerifier() {
        File userFolder = ResourceFXUtils.getUserFolder("Music");
        measureTime("HashVerifier.listNotRepeatedFiles", () -> HashVerifier
                .listNotRepeatedFiles(new File(userFolder, "PalavraCantada"), new File(userFolder, "Bita")));
        FXTesting.runInTime("HashVerifier.listRepeatedFiles",
                () -> HashVerifier.listRepeatedFiles(new File(userFolder, "Cellphone"), ".mp3"), 100_000);
        Path firstMp3 = FileTreeWalker.getFirstPathByExtension(userFolder, ".mp3");
        measureTime("HashVerifier.getMD5Hash", () -> HashVerifier.getMD5Hash(firstMp3));
        measureTime("HashVerifier.getSha1Hash", () -> HashVerifier.getSha1Hash(firstMp3));
        measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash(firstMp3));
        measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash("whatever"));
    }

    @Test
    public void testImageCracker() {
        measureTime("ImageCracker.readScreenshots",
                () -> ImageCracker.crackImages(ResourceFXUtils.getOutFile("screenshots")));
        measureTime("ImageCracker.crackImage", () -> ImageCracker.crackImage(ResourceFXUtils.toFile("CAPTCHA.jpg")));
        measureTime("ImageCracker.crackImage", () -> ImageCracker.crackImage(ResourceFXUtils.toFile("CAPTCHA2.jpg")));
        measureTime("ImageCracker.createSelectedImage", () -> ImageCracker.crackImage(
                ImageCracker.createSelectedImage(new Image(ResourceFXUtils.toExternalForm("CAPTCHA.jpg")))));
    }

    @Test
    public void testInstallCert() {
        String string = "www-acesso";
        measureTime("InstallCert.installCertificate", () -> InstallCert.installCertificate(string));
    }

    @Test
    public void testPageExtractor() {
        PageExtractor show = show(PageExtractor.class);
        File file = new File("C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\Referencias\\screenshots");
        interactNoWait(() -> show.loadHTMLFiles(file));
        lookup(TextField.class).stream().limit(2).forEach(e -> {
            tryClickOn(e);
            write("title");
        });
        lookup(".button").queryAll().forEach(t -> {
            tryClickOn(t);
            type(KeyCode.ESCAPE);
        });
    }

    @Test
    public void testPCapReader() {
        File file = new File("C:\\Users\\guigu\\Documents\\Dev\\Dataprev\\CiscoCNNA\\two.pcap");
        measureTime("PCapReader.readPCAPngFile", () -> PCapReader.readPCAPngFile(file));
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
        String randomIP = IntStream.range(0, 4).map(e -> nextInt(256)).mapToObj(Objects::toString)
                .collect(Collectors.joining("."));
        measureTime("PingTraceRoute.traceRoute", () -> PingTraceRoute.traceRoute(randomIP));
        measureTime("ProcessScan.scanProcesses", () -> ProcessScan.scanNetstats());

        measureTime("NetworkInformationScanner.displayNetworkInformation",
                () -> NetworkInformationScanner.displayNetworkInformation());
        measureTime("NetworkInformationScanner.displayNetworkInformation",
                () -> NetworkInformationScanner.displayNetworkInformation());
    }

    @Test
    public void testSonarApi() {
        ImageFXUtils.setShowImage(false);
        show(SonarApi.class);
        clickButtonsWait();
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
    public void testWebScannerApplication() {
        show(WebScannerApplication.class);
        lookup(TextField.class).stream().limit(1).forEach(e -> {
            tryClickOn(e);
            write("https://google.com/");
            type(KeyCode.ENTER);
        });

    }

    @Test
    public void verifyEthicalHack() {
        // EthicalHackController
        ImageFXUtils.setShowImage(false);
        show(EthicalHackController.class);
        lookup(".button").queryAllAs(Button.class).stream().filter(e -> !"Ips".equals(e.getText())).forEach(t -> {
            SupplierEx.getIgnore(() -> super.clickOn(t));
            sleep(WAIT_TIME);
            type(KeyCode.ESCAPE);
        });
        ConsoleUtils.waitAllProcesses();
        tryClickOn(lookupFirst(CheckBox.class));
    }

    @Test
    public void verifyHashDigest() {
        Path firstPathByExtension = measureTime("ResourceFXUtils.getFirstPathByExtension",
                () -> FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), ".exe"));
        measureTime("HashVerifier.getMD5Hash", () -> HashVerifier.getMD5Hash(firstPathByExtension));
        measureTime("HashVerifier.getSha1Hash", () -> HashVerifier.getSha1Hash(firstPathByExtension));
        measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash(firstPathByExtension));
        measureTime("HashVerifier.getSha256Hash", () -> HashVerifier.getSha256Hash(firstPathByExtension));
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
                "mail.google.com", "play.google.com");
        type(typeText(randomItem));
        clickOn(lookupList.get(1));
        File downloads = ResourceFXUtils.getUserFolder("Downloads");
        interactNoWait(() -> show.chooseDirectory(downloads));
        interactNoWait(RunnableEx.make(() -> {
            File userFolder = FileTreeWalker.getFirstPathByExtension(downloads, ".exe").toFile();
            show.chooseExeFile(userFolder);
        }));
    }

    @Test
    public void verifyVirusTotalApi() {
        Path firstPathByExtension = measureTime("ResourceFXUtils.getFirstPathByExtension",
                () -> FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), ".exe"));
        measureTime("VirusTotalApi.getFilesInformation", () -> VirusTotalApi.getFilesInformation(firstPathByExtension));
        measureTime("VirusTotalApi.getIpInformation", () -> VirusTotalApi.getIpInformation("111.229.255.22"));
        measureTime("VirusTotalApi.getIpTotalInfo", () -> VirusTotalApi.getIpTotalInfo("23.95.188.163"));
        String randomItem = randomItem("safebrowsing.googleapis.com", "tracking-protection.cdn.mozilla.net",
                "shavar.services.mozilla.com", "lh6.googleusercontent.com", "lh3.googleusercontent.com",
                "people-pa.clients6.google.com", "people-pa.clients6.google.com", "clients6.google.com",
                "mail.google.com", "play.google.com", "http://wwwcztapwlwk.net/plafgxc80333067532");
        measureTime("VirusTotalApi.getUrlInformation", () -> VirusTotalApi.getUrlInformation(randomItem));
    }

}
