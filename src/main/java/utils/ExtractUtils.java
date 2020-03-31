package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import javafx.beans.property.Property;
import org.apache.commons.io.IOUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

public final class ExtractUtils {
    private static final int HUNDRED_SECONDS = 100000;
    private static final Logger LOG = HasLogging.log();

    private ExtractUtils() {
    }

    public static String addDomain(Property<String> domain, String l) {
        if (l.startsWith("http")) {
            return l;
        }
        if (l.startsWith("//") && domain.getValue().contains("://")) {
            return domain.getValue().split("//")[0] + l;
        }
        return domain.getValue() + (!l.startsWith("/") ? "/" + l : l);
    }

    public static void copy(File input, File outFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(input)) {
            ExtractUtils.copy(inputStream, outFile);
        }
    }

    public static void copy(InputStream inputStream, File outFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {
            IOUtils.copy(inputStream, fileOutputStream);
        }
    }

    public static void copy(Path input, File outFile) throws IOException {
        copy(input.toFile(), outFile);
    }

    public static void copy(String url, File outFile) throws IOException {
        copy(new URL(url).openConnection().getInputStream(), outFile);
    }

    public static void copy(String src, String dest) throws IOException {
        copy(new File(src), new File(dest));
    }

    public static Response executeRequest(String url, Map<String, String> cookies) throws IOException {
        Connection connect = HttpConnection.connect(url);
        if (!CrawlerTask.isNotProxied()) {
            addProxyAuthorization(connect);
        }
        connect.timeout(HUNDRED_SECONDS);
        connect.cookies(cookies);
        connect.ignoreContentType(true);
        connect.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0");
        return connect.execute();
    }

    public static File extractURL(String url) {
        return SupplierEx.get(() -> {
            String file = new URL(url).getFile();
            String[] split = file.split("/");
            String out = split[split.length - 1];
            return extractURL(out, url);
        });
    }

    public static File extractURL(String name, String url) {
        return SupplierEx.get(() -> {
            File outFile = ResourceFXUtils.getOutFile(name);
            ExtractUtils.copy(url, outFile);
            if (url.endsWith(".zip")) {
                UnZip.extractZippedFiles(outFile);
            }
            if (url.endsWith(".rar")) {
                UnRar.extractRarFiles(outFile);
            }
            LOG.info("FILE {} SAVED", name);
            return outFile;
        });
    }


    public static Document getDocument(final String url) throws IOException {
        Connection connect = Jsoup.connect(url);
        if (!CrawlerTask.isNotProxied()) {
            addProxyAuthorization(connect);
        }
        return connect
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0").get();
    }

    public static Document getDocument(String url, Map<String, String> cookies) throws IOException {
        Response execute = executeRequest(url, cookies);
        Map<String, String> cookies2 = execute.cookies();
        cookies.putAll(cookies2);
        return execute.parse();
    }

    public static File getFile(String key, String url1) throws IOException {
        File outFile = ResourceFXUtils.getOutFile(key);
        URL url2 = new URL(url1);
        HttpURLConnection con = (HttpURLConnection) url2.openConnection();
        if (!CrawlerTask.isNotProxied()) {
            addBasicAuthorization(con);
        }

        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setRequestProperty("Accept",
            "text/html,application/xhtml+xml,application/xml,application/pdf;q=0.9,*/*;q=0.8");
        con.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Connection", "keep-alive");
        con.setConnectTimeout(HUNDRED_SECONDS);
        con.setReadTimeout(HUNDRED_SECONDS);
        copy(con.getInputStream(), outFile);
        if (url1.endsWith(".zip") || key.endsWith(".zip")) {
            UnZip.extractZippedFiles(outFile);
        }
        if (url1.endsWith(".rar") || key.endsWith(".rar")) {
            UnRar.extractRarFiles(outFile);
        }
        LOG.info("FILE {} SAVED", key);
        return outFile;
    }

    private static void addBasicAuthorization(HttpURLConnection con) {
        con.addRequestProperty("Proxy-Authorization", "Basic " + CrawlerTask.getEncodedAuthorization());
    }

    private static void addProxyAuthorization(Connection connect) {
        connect.header("Proxy-Authorization",
            "Basic " + CrawlerTask.getEncodedAuthorization());
    }

}
