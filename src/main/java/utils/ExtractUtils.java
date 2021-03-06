
package utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.ex.PredicateEx;
import utils.ex.SupplierEx;

public final class ExtractUtils {
    public static final String EXTRACT_FOLDER = ProjectProperties.getField();
    public static final String PROXY_PORT = "3128";
    public static final int HUNDRED_SECONDS = 100_000;
    private static final Logger LOG = HasLogging.log();

    private static final String CERTIFICATION_FILE = ResourceFXUtils.toFullPath(ProjectProperties.getField());
    private static final String LOGIN = ProjectProperties.getField();
    private static final String PROXY_CONFIG = ProjectProperties.getField();
    private static final String PROXY_CONFIG2 = ProjectProperties.getField();
    private static final String PASS = ProjectProperties.getField();
    public static final String PROXY_ADDRESS = getProxyAddress();
    private static final boolean IS_PROXIED = PROXY_ADDRESS != null;

    private ExtractUtils() {
    }

    public static void addAuthorizationConfig() {
        System.setProperty("javax.net.ssl.trustStore", CERTIFICATION_FILE);
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        boolean b = true;
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> b);
        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getHTTPUsername(), getHTTPPassword().toCharArray());
            }

        });
    }

    public static String addDomain(Property<String> domain, String url) {
        String value = domain.getValue();
        return addDomain(value, url);
    }

    public static String addDomain(String domain, String url) {
        if (url.startsWith("http") || url.startsWith("data:image") || url.startsWith("file:")) {
            return url;
        }
        if (url.startsWith(domain)) {
            return "http://" + url;
        }
        if (url.startsWith("//")) {
            if (domain.contains("://")) {
                return domain.split("//")[0] + url;
            }
            return "http:" + url;
        }
        return domain + (!url.startsWith("/") ? "/" + url : url);
    }

    public static void copy(File input, File outFile) throws IOException {
        if (!Objects.equals(outFile, input)) {
            HasLogging.log(1).info("COPYING {}->{}", input, outFile);
            try (FileInputStream inputStream = new FileInputStream(input)) {
                ExtractUtils.copy(inputStream, outFile);
            }
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

    public static void copy(Reader inputStream, File outFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {
            IOUtils.copy(inputStream, fileOutputStream, StandardCharsets.UTF_8);
        }
    }

    public static void copy(String url, File outFile) throws IOException {
        copy(new URL(url).openConnection().getInputStream(), outFile);
    }

    public static void copy(String src, String dest) throws IOException {
        copy(new File(src), new File(dest));
    }

    public static File extractURL(String url) {
        return SupplierEx.get(() -> {
            String file = new URL(url).getFile();
            String[] urlParts = file.split("/");
            String out = urlParts[urlParts.length - 1];
            return extractURL(out, url);
        });
    }

    public static File extractURL(String name, String url) {
        return SupplierEx.get(() -> {
            File outFile = ResourceFXUtils.getOutFile(EXTRACT_FOLDER + name);
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

    public static Proxy findProxy(URI uri) {
        return SupplierEx.get(() -> {
            insertProxyConfig();
            return ProxySelector.getDefault().select(uri).get(0);
        }, Proxy.NO_PROXY);
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

    public static String getHTTPPassword() {
        return PASS;
    }

    public static String getHTTPUsername() {
        return LOGIN;
    }

    public static void insertProxyConfig() {
        addAuthorizationConfig();
        if (isNotProxied()) {
            return;
        }
        System.setProperty("http.proxyHost", PROXY_ADDRESS);
        System.setProperty("http.proxyPort", PROXY_PORT);
        System.setProperty("https.proxyHost", PROXY_ADDRESS);
        System.setProperty("https.proxyPort", PROXY_PORT);
        // System.setProperty("http.proxyUser", getHTTPUsername());
        // System.setProperty("http.proxyPassword", getHTTPPassword());
        // System.setProperty("https.proxyUser", getHTTPUsername());
        // System.setProperty("https.proxyPassword", getHTTPPassword());
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|n321p000124.fast.prevnet");
        System.setProperty("https.nonProxyHosts", "localhost|127.0.0.1|n321p000124.fast.prevnet");

        // System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }

    public static boolean isNotProxied() {
        return !IS_PROXIED;
    }

    public static boolean isPortOpen(String url) {
        URL url2 = SupplierEx.get(() -> new URL(url));
        return isPortOpen(url2.getHost(), url2.getPort());
    }

    public static boolean isPortOpen(String ip0, int porta, int timeout) {
        return PredicateEx.test(ip -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, porta), timeout);
                return true;
            }
        }, ip0);

    }

    public static boolean isProxySet() {
        return StringUtils.isNotBlank(System.getProperty("https.proxyHost"));
    }

    public static void removeProxyConfig() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("http.nonProxyHosts");
        System.clearProperty("https.nonProxyHosts");
    }

    private static void addBasicAuthorization(HttpURLConnection con) {
        con.addRequestProperty("Proxy-Authorization", "Basic " + getEncodedAuthorization());
    }

    private static String getProxyAddress() {
        final int timeout = 5000;
        return Stream.of(PROXY_CONFIG2, PROXY_CONFIG).filter(PredicateEx.makeTest(s -> isPortOpen(s, 3128, timeout)))
                .findFirst().orElse(null);
    }

    private static boolean isPortOpen(String ip0, int porta) {
        final int timeout = 5000;
        return isPortOpen(ip0, porta, timeout);
    }

}
