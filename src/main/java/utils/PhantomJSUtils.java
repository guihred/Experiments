package utils;

import gui.ava.html.image.generator.HtmlImageGenerator;
import java.awt.Dimension;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import utils.ex.ConsumerEx;
import utils.ex.RunnableEx;

public final class PhantomJSUtils {
    private PhantomJSUtils() {
    }

    @SafeVarargs
    public static Document renderPage(String url, Map<String, String> cookies, String loadingStr,
            ConsumerEx<PhantomJSDriver>... onload) {
        PhantomJSDriverService createDefaultService =
                new PhantomJSDriverService.Builder().usingPhantomJSExecutable(ExtractUtils.PHANTOM_JS.toFile())
                        .usingAnyFreePort().withLogFile(ResourceFXUtils.getOutFile("log/phantomjsdriver.log")).build();
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

    public static void saveHtmlImage(String html, File file) {
        HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
        Dimension dim = imageGenerator.getDefaultSize();
        dim.setSize(Math.min(800, dim.getWidth()), dim.getHeight());
        imageGenerator.setSize(dim);
        imageGenerator.loadHtml(html);
        imageGenerator.saveAsImage(file);

    }

}
