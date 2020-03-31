package utils;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.concurrent.Task;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang3.time.DurationFormatUtils;

public abstract class CrawlerTask extends Task<String> {
    public static final String CERTIFICATION_FILE = ResourceFXUtils.toFullPath("cacerts");
    private static final String LOGIN = "guilherme.hmedeiros";
    private static final String PASS = "15-juuGO";
    private static final String PROXY_CONFIG = Stream.of("10", "70", "124", "16").collect(Collectors.joining("."));
    private static final String PROXY_CONFIG_2 = Stream.of("10", "31", "220", "23").collect(Collectors.joining("."));
    private static final boolean IS_PROXIED = isProxied();

    private Instant start;
    private boolean cancelled;

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

    protected abstract String task();

    protected void updateAll(final long i, final long total) {
        updateTitle("Processed " + i + " of " + total + " items.");
        if (i > 0 && total > i) {

            long between = ChronoUnit.MILLIS.between(start, Instant.now());
            String formatDuration = DurationFormatUtils.formatDuration(between * (total - i) / i, "H:mm:ss", true);
            updateMessage("Time estimated: " + formatDuration);
        } else {
            updateMessage("Time estimated unknown");
        }
        updateProgress(i, total);
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

        String proxyAddress = getProxyAddress();
        System.setProperty("http.proxyHost", proxyAddress);
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("https.proxyHost", proxyAddress);
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

    private static String getProxyAddress() {
        final int timeout = 5000;
        return Stream.of(PROXY_CONFIG, PROXY_CONFIG_2)
                .filter(PredicateEx.makeTest(s -> InetAddress.getByName(s).isReachable(timeout))).findFirst()
                .orElse(null);
    }

    private static boolean isProxied() {
        return getProxyAddress() != null;
    }

}
