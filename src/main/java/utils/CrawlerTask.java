package utils;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;

public abstract class CrawlerTask extends Task<String> {

    static final Logger LOG = HasLogging.log();
    public static final String CERTIFICATION_FILE = ResourceFXUtils.toFullPath("cacerts");
    private static final String LOGIN = "guilherme.hmedeiros";
    private static final String PASS = "14-juuYON";
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

    protected static Integer convertNumerico(final String eleitores) {
        String replaceAll = eleitores.replaceAll("\\D", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
    }

    private static Predicate<Integer> isProxied() {
        return PredicateEx.makeTest(timeout -> InetAddress.getByName(PROXY_CONFIG).isReachable(timeout));
    }

}
