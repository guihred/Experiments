package utils;

import extract.UnRar;
import extract.UnZip;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

public abstract class CrawlerTask extends Task<String> {
    private static final Logger LOG = HasLogging.log();
    public static final String CERTIFICATION_FILE = ResourceFXUtils.toFullPath("cacerts");
    private static final String LOGIN = "guilherme.hmedeiros";
    private static final String PASS = "15-juuGO";
    private static final String PROXY_CONFIG = Stream.of("10", "70", "124", "16").collect(Collectors.joining("."));
    private static final boolean IS_PROXIED = isProxied().test(5000);

    private Instant start;
    private boolean cancelled;
    private String encoded = getEncodedAuthorization();

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        boolean cancel = super.cancel(mayInterruptIfRunning);
        setCancelled(true);
        return cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    protected String call() throws Exception {
        start = Instant.now();
        return task();
    }

    protected Document getDocument(final String url) throws IOException {
        return getDocument(url, encoded);
    }

    protected abstract String task();

    protected void updateAll(final long i, final long total) {
        updateTitle("Processed " + i + " of " + total + " items.");
        if (i > 0) {

            long between = ChronoUnit.MILLIS.between(start, Instant.now());
            String formatDuration = DurationFormatUtils.formatDuration(between * (total - i) / i, "H:mm:ss", true);
            updateMessage("Time estimated: " + formatDuration);
        } else {
            updateMessage("Time estimated unknown");
        }
        updateProgress(i, total);
    }

    public static void copy(File input, File outFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(input)) {
            copy(inputStream, outFile);
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
        InputStream input = new URL(url).openConnection().getInputStream();
        copy(input, outFile);
    }

    public static void copy(String src, String dest) throws IOException {
        copy(new File(src), new File(dest));
    }

    public static Response executeRequest(String url, Map<String, String> cookies) throws IOException {
        Connection connect = HttpConnection.connect(url);
        if (!isNotProxied()) {
            connect.header("Proxy-Authorization",
                "Basic " + getEncodedAuthorization());
        }
        connect.timeout(100000);
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
            copy(url, outFile);
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

    public static Document getDocument(String url, Map<String, String> cookies) throws IOException {
        Response execute = executeRequest(url, cookies);
        Map<String, String> cookies2 = execute.cookies();
        cookies.putAll(cookies2);
        return execute.parse();
    }

    public static Document getDocument(final String url, String encoded) throws IOException {
        Connection connect = Jsoup.connect(url);
        if (!isNotProxied()) {
            connect.header("Proxy-Authorization", "Basic " + encoded);
        }
        return connect
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0").get();
    }

    public static String getEncodedAuthorization() {
        return Base64.getEncoder()
            .encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes(StandardCharsets.UTF_8));
    }

    public static File getFile(String key, String url1) throws IOException {
        File outFile = ResourceFXUtils.getOutFile(key);
        URL url2 = new URL(url1);
        HttpURLConnection con = (HttpURLConnection) url2.openConnection();
        if (!isNotProxied()) {
            con.addRequestProperty("Proxy-Authorization", "Basic " + getEncodedAuthorization());
        }
    
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setRequestProperty("Accept",
            "text/html,application/xhtml+xml,application/xml,application/pdf;q=0.9,*/*;q=0.8");
        con.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Connection", "keep-alive");
        con.setConnectTimeout(100000);
        con.setReadTimeout(100000);
        InputStream input = con.getInputStream();
        copy(input, outFile);
        if (url1.endsWith(".zip") || key.endsWith(".zip")) {
            UnZip.extractZippedFiles(outFile);
        }
        if (url1.endsWith(".rar") || key.endsWith(".rar")) {
            UnRar.extractRarFiles(outFile);
        }
        LOG.info("FILE {} SAVED", key);
        return outFile;
    }

    public static String getHTTPPassword() {
        return PASS;
    }

    public static String getHTTPUsername() {
        return LOGIN;
    }

    public static void insertProxyConfig() {
        if (isNotProxied()) {
            return;
        }

        System.setProperty("http.proxyHost", PROXY_CONFIG);
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("https.proxyHost", PROXY_CONFIG);
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
        System.setProperty("javax.net.ssl.trustStore", CERTIFICATION_FILE);

        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getHTTPUsername(), getHTTPPassword().toCharArray());
            }

        });
        boolean b = true;
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> b);
    }

    public static boolean isNotProxied() {
        return !IS_PROXIED;
    }

    public static Predicate<Integer> isProxied() {
        return PredicateEx.makeTest(timeout -> InetAddress.getByName(PROXY_CONFIG).isReachable(timeout));
    }

}
