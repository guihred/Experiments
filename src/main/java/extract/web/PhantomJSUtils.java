package extract.web;

import gui.ava.html.image.generator.HtmlImageGenerator;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class PhantomJSUtils {
    private static final Path PHANTOM_JS =
            FileTreeWalker.getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "phantomjs.exe");

    private static final Logger LOG = HasLogging.log();

    private static final int WAIT_TIME = 100_000;

    private PhantomJSDriver ghostDriver;

    public PhantomJSUtils() {
        this(false);
    }

    public PhantomJSUtils(boolean withProxy) {
        ghostDriver = getGhostDriver(withProxy);
    }

    public Document load(String url) {
        ghostDriver.manage().window().maximize();
        ghostDriver.get(url);
        return Jsoup.parse(ghostDriver.getPageSource());
    }

    public void quit() {
        ghostDriver.quit();
    }

    public Document render(String url, Map<String, String> cookies) {
        return render(url, cookies, 0);
    }

    public Document render(String url, Map<String, String> cookies, double delay) {
        ghostDriver.setLogLevel(Level.FINE);
        ghostDriver.manage().window().maximize();
        Set<Cookie> cookies2 = ghostDriver.manage().getCookies();
        ghostDriver.get(url);
        cookies.forEach((k, v) -> {
            if (cookies2.stream().noneMatch(c -> c.getName().equals(k))) {
                RunnableEx.run(() -> ghostDriver.manage().addCookie(new Cookie(k, v)));
            }
        });
        ghostDriver.get(url);
        if (delay > 0) {
            RunnableEx.sleepSeconds(delay);
        }
        return Jsoup.parse(ghostDriver.getPageSource());
    }

    public void screenshot(File outFile) throws IOException {
        ExtractUtils.copy(ghostDriver.getScreenshotAs(OutputType.FILE), outFile);
    }

    public static List<String> makeGet(String url, Map<String, String> headers) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet post = new HttpGet(url);
        headers.forEach(post::addHeader);
        HttpResponse response = SupplierEx.getFirst(() -> client.execute(post), () -> {
            InstallCert.installCertificate(url);
            return client.execute(post);
        });
        HttpEntity entity = response.getEntity();
        try (BufferedReader rd =
                new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
            return rd.lines().collect(Collectors.toList());
        }
    }

    public static List<String> makeGet(String url, Map<String, String> headers, File outFile) throws IOException {
        List<String> allLines = makeGet(url, headers);
        Files.write(outFile.toPath(), allLines);
        return allLines;
    }

    public static Map<String, String> postContent(String url, String content, ContentType applicationJson,
            Map<String, String> headers, File outFile) throws IOException {
        HttpClient client = HttpClientBuilder.create().setHostnameVerifier(new AllowAllHostnameVerifier()).build();
        HttpPost get = new HttpPost(url);
        get.setConfig(RequestConfig.custom().setSocketTimeout(WAIT_TIME).build());
        get.setEntity(new StringEntity(content, applicationJson));
        headers.forEach(get::addHeader);
        LOG.info("Request \n\t{} \n{} \n\t{} ", url, content, outFile.getName());
        HttpResponse response = SupplierEx.getFirst(() -> client.execute(get), () -> {
            InstallCert.installCertificate(url);
            return client.execute(get);
        });
        Map<String, String> d = Stream.of(response.getAllHeaders()).collect(
                Collectors.groupingBy(Header::getName, Collectors.mapping(Header::getValue, Collectors.joining("; "))));
        HttpEntity entity = response.getEntity();
        try (BufferedReader rd =
                new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
            ExtractUtils.copy(rd, outFile);
            return d;
        }
    }

    public static void postJson(String url, String content, Map<String, String> headers, File outFile)
            throws IOException {
        postContent(url, content, ContentType.APPLICATION_JSON, headers, outFile);
    }

    public static void postNdJson(String url, String cont, Map<String, String> headers, File outFile)
            throws IOException {

        HttpClient client = HttpClientBuilder.create().setHostnameVerifier(new AllowAllHostnameVerifier()).build();
        HttpPost get = new HttpPost(url);
        String content = cont.replaceAll("[\n\t]+", "").replaceFirst("\\}\\{", "}\n{") + "\n";
        get.setConfig(RequestConfig.custom().setSocketTimeout(WAIT_TIME).build());
        get.setEntity(new StringEntity(
                StringSigaUtils.fixEncoding(content, StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1),
                ContentType.create("application/x-ndjson")));
        headers.forEach(get::addHeader);
        String headersString = headers.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
        LOG.info("Request \n\t{} \n\t{} \n\t{}", url, headersString, content);
        HttpResponse response = SupplierEx.getFirst(() -> client.execute(get), () -> {
            InstallCert.installCertificate(url);
            return client.execute(get);
        });
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        ExtractUtils.copy(rd, outFile);
    }

    public static Document renderPage(String url, Map<String, String> cookies, String loadingStr, File outFile) {
        return renderPage(url, cookies, loadingStr,
                driver -> ExtractUtils.copy(driver.getScreenshotAs(OutputType.FILE), outFile));
    }

    public static Image saveHtmlImage(String html, File file) {
        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
        Dimension dim = imageGenerator.getDefaultSize();
        final int preferredWidth = 800;
        dim.setSize(Math.min(preferredWidth, dim.getWidth()), dim.getHeight());
        imageGenerator.setSize(dim);
        imageGenerator.loadHtml(html);
        imageGenerator.saveAsImage(file);
        return new Image(ResourceFXUtils.convertToURL(file).toExternalForm());
    }

    public static Image textToImage(String s, String highlight) {
        String textParagraphs = Stream.of(s.split("\n")).filter(StringUtils::isNotBlank)
                .map(str -> "<p>" + str.replaceAll(highlight, "<font>$1</font>") + "</p>")
                .collect(Collectors.joining("\n"));
        String format =
                String.format("<!DOCTYPE html>\n<html>\n<head>\n<style>\nfont {background-color: yellow;}</style>\n"
                        + "</head><body>%s</body>\n</html>", textParagraphs);
        return saveHtmlImage(format);
    }

    private static PhantomJSDriver getGhostDriver() {
        return getGhostDriver(false);
    }

    private static PhantomJSDriver getGhostDriver(boolean withProxy) {

        PhantomJSDriverService createDefaultService = new PhantomJSDriverService.Builder()
                .usingPhantomJSExecutable(PHANTOM_JS.toFile()).usingAnyFreePort()
                .withLogFile(ResourceFXUtils.getOutFile("log/phantomjsdriver.log"))
                .usingGhostDriverCommandLineArguments(new String[] { "examples/responsive-screenshot.js" }).build();
        DesiredCapabilities firefox = DesiredCapabilities.firefox();
        String[] value = withProxy
                ? new String[] { "--webdriver-loglevel=NONE",
                        "--proxy=" + ExtractUtils.PROXY_ADDRESS + ":" + ExtractUtils.PROXY_PORT }
                : new String[] { "--webdriver-loglevel=NONE" };
        firefox.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, value);
        return new PhantomJSDriver(createDefaultService, firefox);
    }

    @SafeVarargs
    private static Document renderPage(String url, Map<String, String> cookies, String loadingStr,
            ConsumerEx<PhantomJSDriver>... onload) {
        PhantomJSDriver ghostDriver = getGhostDriver();
        try {
            ghostDriver.setLogLevel(Level.OFF);
            ghostDriver.manage().window().maximize();
            ghostDriver.get(url);
            cookies.forEach((k, v) -> RunnableEx.ignore(() -> ghostDriver.manage().addCookie(new Cookie(k, v))));
            ghostDriver.get(url);
            Set<Cookie> cookies2 = ghostDriver.manage().getCookies();
            for (Cookie cookie : cookies2) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
            RunnableEx.ignore(() -> cookies.forEach((k, v) -> ghostDriver.manage().addCookie(new Cookie(k, v))));
            RunnableEx.sleepSeconds(1. / 2);
            String pageSource = ghostDriver.getPageSource();
            for (int i = 0; StringUtils.isNotBlank(loadingStr) && pageSource.contains(loadingStr) && i < 20; i++) {
                RunnableEx.sleepSeconds(5);
                pageSource = ghostDriver.getPageSource();
            }
            for (ConsumerEx<PhantomJSDriver> consumerEx : onload) {
                ConsumerEx.accept(consumerEx, ghostDriver);
            }
            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }

    private static Image saveHtmlImage(String html) {
        return SupplierEx.get(() -> saveHtmlImage(html,
                ResourceFXUtils.getOutFile("print/oi" + HashVerifier.getSha256Hash(html) + ".png")));
    }

}
