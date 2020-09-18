
package utils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.Property;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;
import utils.ex.PredicateEx;
import utils.ex.SupplierEx;

public final class ExtractUtils {
    public static final String PROXY_PORT = "3128";
    public static final int HUNDRED_SECONDS = 100_000;
    private static final Logger LOG = HasLogging.log();

    public static final String CERTIFICATION_FILE = ResourceFXUtils.toFullPath("cacerts");
    public static final String PROXY_CONFIG = Stream.of("10", "70", "124", "16").collect(Collectors.joining("."));
    private static final String LOGIN = "guilherme.hmedeiros";
    private static final String PASS = "15-juuGO";
    private static final String PROXY_ADDRESS = getProxyAddress();
    private static final boolean IS_PROXIED = PROXY_ADDRESS != null;
    public static final Path PHANTOM_JS =
            FileTreeWalker.getFirstPathByExtension(ResourceFXUtils.getUserFolder("Downloads"), "phantomjs.exe");

    private ExtractUtils() {
    }

    public static String addDomain(Property<String> domain, String url) {
        String value = domain.getValue();
        return addDomain(value, url);
    }

    public static String addDomain(String domain, String url) {
        if (url.startsWith("http") || url.startsWith("data:image")) {
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
        HasLogging.log(1).info("COPYING {}->{}", input, outFile);
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
        System.setProperty("javax.net.ssl.trustStore", CERTIFICATION_FILE);
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        boolean b = true;
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> b);
        if (isNotProxied()) {
            return;
        }
        System.setProperty("http.proxyHost", PROXY_ADDRESS);
        System.setProperty("http.proxyPort", PROXY_PORT);
        System.setProperty("https.proxyHost", PROXY_ADDRESS);
        System.setProperty("https.proxyPort", PROXY_PORT);
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");

        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getHTTPUsername(), getHTTPPassword().toCharArray());
            }

        });
    }

    public static boolean isNotProxied() {
        return !IS_PROXIED;
    }

    public static boolean isPortOpen(String ip0, int porta, int timeout) {
        return PredicateEx.test((String ip) -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, porta), timeout);
                return true;
            }
        }, ip0);

    }

    private static void addBasicAuthorization(HttpURLConnection con) {
        con.addRequestProperty("Proxy-Authorization", "Basic " + getEncodedAuthorization());
    }


    private static String getProxyAddress() {
        final int timeout = 5000;
        return Stream.of(PROXY_CONFIG).filter(PredicateEx.makeTest(s -> isPortOpen(s, 3128, timeout))).findFirst()
                .orElse(null);
    }

}
