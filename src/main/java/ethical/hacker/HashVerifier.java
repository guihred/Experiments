package ethical.hacker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.ResourceFXUtils;

public final class HashVerifier {
    public static final Logger LOG = HasLogging.log();

    private static final Path PHANTOM_JS =
            ResourceFXUtils.getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "phantomjs.exe");

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

    public static Document hashLookup(String sha1Hash) {
        return renderPage("https://hashlookup.org/search.php?q=" + sha1Hash);
    }

    public static Document renderPage(String url) {
        DesiredCapabilities dcap = DesiredCapabilities.firefox();
        PhantomJSDriverService createDefaultService =
                new PhantomJSDriverService.Builder().usingPhantomJSExecutable(PHANTOM_JS.toFile()).usingAnyFreePort()
                        .withLogFile(ResourceFXUtils.getOutFile("phantomjsdriver.log")).build();
        PhantomJSDriver ghostDriver = new PhantomJSDriver(createDefaultService, dcap);
        try {
            ghostDriver.setLogLevel(Level.SEVERE);
            ghostDriver.manage().window().maximize();
            ghostDriver.get(url);
            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }

    public static Document virusTotal(String sha256Hash) {
        return renderPage("https://www.virustotal.com/old-browsers/file/" + sha256Hash);
    }
}