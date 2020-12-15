package extract;

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
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
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
    public static final Path PHANTOM_JS =
            FileTreeWalker.getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "phantomjs.exe");

    private static final Logger LOG = HasLogging.log();

    private static final int WAIT_TIME = 100_000;

    private PhantomJSUtils() {
    }


    public static List<String> makeGet(String url, Map<String, String> headers, File outFile) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet post = new HttpGet(url);
        headers.forEach(post::addHeader);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        List<String> collect = rd.lines().collect(Collectors.toList());
        Files.write(outFile.toPath(), collect);
        return collect;
    }

    public static void postJson(String url, String content, Map<String, String> headers, File outFile)
            throws IOException {
        ExtractUtils.insertProxyConfig();
        HttpClient client = HttpClientBuilder.create().setHostnameVerifier(new AllowAllHostnameVerifier()).build();
        HttpPost get = new HttpPost(url);
        get.setConfig(RequestConfig.custom().setSocketTimeout(WAIT_TIME).build());
        get.setEntity(new StringEntity(content, ContentType.APPLICATION_JSON));
        headers.forEach(get::addHeader);
        LOG.info("Request \n\t{} \n{} \n\t{} ", url, content, outFile.getName());
        HttpResponse response = SupplierEx.getFirst(() -> client.execute(get), () -> {
            InstallCert.installCertificate(url);
            return client.execute(get);
        });
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        ExtractUtils.copy(rd, outFile);
    }

    public static void postNdJson(String url, String cont, Map<String, String> headers, File outFile)
            throws IOException {
        ExtractUtils.insertProxyConfig();
        HttpClient client = HttpClientBuilder.create().setHostnameVerifier(new AllowAllHostnameVerifier()).build();
        HttpPost get = new HttpPost(url);
        String content = cont.replaceAll("[\n\t]+", "").replaceFirst("\\}\\{", "}\n{") + "\n";
        get.setConfig(RequestConfig.custom().setSocketTimeout(WAIT_TIME).build());
        get.setEntity(new StringEntity(
                StringSigaUtils.fixEncoding(content, StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1),
                ContentType.create("application/x-ndjson")));
        headers.forEach(get::addHeader);
        LOG.info("Request \n\t{} \n\t{} ", url, outFile.getName());
        HttpResponse response = SupplierEx.getFirst(() -> client.execute(get), () -> {
            InstallCert.installCertificate(url);
            return client.execute(get);
        });
        HttpEntity entity = response.getEntity();
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
        ExtractUtils.copy(rd, outFile);
    }

    @SafeVarargs
    public static Document renderPage(String url, Map<String, String> cookies, String loadingStr,
            ConsumerEx<PhantomJSDriver>... onload) {
        PhantomJSDriverService createDefaultService =
                new PhantomJSDriverService.Builder().usingPhantomJSExecutable(PHANTOM_JS.toFile()).usingAnyFreePort()
                        .withLogFile(ResourceFXUtils.getOutFile("log/phantomjsdriver.log")).build();
        PhantomJSDriver ghostDriver = new PhantomJSDriver(createDefaultService, DesiredCapabilities.firefox());
        try {
            ghostDriver.setLogLevel(Level.OFF);
            ghostDriver.manage().window().maximize();
            ghostDriver.get(url);
            RunnableEx.run(() -> cookies.forEach((k, v) -> ghostDriver.manage().addCookie(new Cookie(k, v))));
            RunnableEx.sleepSeconds(.5);
            String pageSource = ghostDriver.getPageSource();
            for (int i = 0; StringUtils.isNotBlank(loadingStr) && pageSource.contains(loadingStr) && i < 10; i++) {
                RunnableEx.sleepSeconds(5);
                pageSource = ghostDriver.getPageSource();
            }
            for (ConsumerEx<PhantomJSDriver> consumerEx : onload) {
                ConsumerEx.accept(consumerEx, ghostDriver);
            }
            Set<Cookie> cookies2 = ghostDriver.manage().getCookies();
            for (Cookie cookie : cookies2) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
            return Jsoup.parse(ghostDriver.getPageSource());
        } finally {
            ghostDriver.quit();
        }
    }

    public static Document renderPage(String url, Map<String, String> cookies, String loadingStr, File outFile) {
        return renderPage(url, cookies, loadingStr,
                driver -> ExtractUtils.copy(driver.getScreenshotAs(OutputType.FILE), outFile));
    }

    public static Image saveHtmlImage(String html) {
        return SupplierEx.get(() -> saveHtmlImage(html,
                ResourceFXUtils.getOutFile("print/oi" + HashVerifier.getSha256Hash(html) + ".png")));
    }

    public static Image saveHtmlImage(String html, File file) {
        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
        Dimension dim = imageGenerator.getDefaultSize();
        dim.setSize(Math.min(800, dim.getWidth()), dim.getHeight());
        imageGenerator.setSize(dim);
        imageGenerator.loadHtml(html);
        imageGenerator.saveAsImage(file);
        return new Image(ResourceFXUtils.convertToURL(file).toExternalForm());
    }

}
