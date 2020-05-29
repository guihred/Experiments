package utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javax.net.ssl.HttpsURLConnection;
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

    public static final String CERTIFICATION_FILE = ResourceFXUtils.toFullPath("cacerts");
    private static final String LOGIN = "guilherme.hmedeiros";
    private static final String PASS = "15-juuGO";
    private static final String PROXY_CONFIG = Stream.of("10", "70", "124", "16").collect(Collectors.joining("."));
    private static final String PROXY_ADDRESS = getProxyAddress();
    private static final boolean IS_PROXIED = PROXY_ADDRESS != null;

    private ExtractUtils() {
    }

    public static String addDomain(Property<String> domain, String l) {
        if (l.startsWith("http") || l.startsWith("data:image") || l.startsWith(domain.getValue())) {
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
        if (!isNotProxied()) {
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
        if (!isNotProxied()) {
            addProxyAuthorization(connect);
        }
        return connect
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0")
                .get();
    }

    public static Document getDocument(String url, Map<String, String> cookies) throws IOException {
        Response execute = executeRequest(url, cookies);
        Map<String, String> cookies2 = execute.cookies();
        cookies.putAll(cookies2);
        return execute.parse();
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
        if (isNotProxied()) {
            return;
        }
        System.setProperty("http.proxyHost", PROXY_ADDRESS);
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("https.proxyHost", PROXY_ADDRESS);
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

    public static boolean isPortOpen(String ip0, int porta, int timeout) {
        return PredicateEx.makeTest((String ip) -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, porta), timeout);
                return true;
            }
        }).test(ip0);

    }

    private static void addBasicAuthorization(HttpURLConnection con) {
        con.addRequestProperty("Proxy-Authorization", "Basic " + getEncodedAuthorization());
    }

    private static void addProxyAuthorization(Connection connect) {
        connect.header("Proxy-Authorization", "Basic " + getEncodedAuthorization());
    }

    private static String getProxyAddress() {
        final int timeout = 5000;
        return Stream.of(PROXY_CONFIG).filter(PredicateEx.makeTest(s -> isPortOpen(s, 3128, timeout))).findFirst()
                .orElse(null);
    }


}
