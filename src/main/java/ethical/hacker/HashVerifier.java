package ethical.hacker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class HashVerifier {
    public static final Logger LOG = HasLogging.log();

    private HashVerifier() {
    }

    public static String getMD5Hash(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.md5Hex(is);
        }
    }

    public static String getSha1Hash(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.sha1Hex(is);
        }
    }

    public static String getSha256Hash(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return DigestUtils.sha256Hex(is);
        }
    }


    public static Document renderPage(String url) {
        Path phantomJS =
                ResourceFXUtils.getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "phantomjs.exe");
        DesiredCapabilities dcap = DesiredCapabilities.firefox();
        Proxy extractFrom = Proxy.extractFrom(dcap);
        extractFrom.setHttpProxy(ExtractUtils.PROXY_CONFIG + ":" + ExtractUtils.PROXY_PORT);
        extractFrom.setProxyType(ProxyType.MANUAL);
        extractFrom.setSocksUsername(ExtractUtils.getHTTPUsername());
        extractFrom.setSocksPassword(ExtractUtils.getHTTPPassword());
        PhantomJSDriverService createDefaultService = new PhantomJSDriverService.Builder()
                .usingPhantomJSExecutable(phantomJS.toFile())
                .usingAnyFreePort()
                .withProxy(extractFrom)
                .withLogFile(ResourceFXUtils.getOutFile("phantomjsdriver.log")).build();
        PhantomJSDriver ghostDriver = new PhantomJSDriver(createDefaultService, dcap);
        try {
            ghostDriver.setLogLevel(Level.OFF);
            ghostDriver.manage().window().maximize();
            ghostDriver.get(url);
            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }
}