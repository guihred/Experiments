package election.experiment;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javafx.concurrent.Task;
import simplebuilder.HasLogging;

public abstract class CrawlerTask extends Task<String> implements HasLogging {

    private Instant start;
    private boolean cancelled = false;
	String encoded = Base64.getEncoder().encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes());
	protected Document getDocument(String url) throws IOException {
		Connection connect = Jsoup.connect(url);
		connect.header("Proxy-Authorization", "Basic " + encoded);
		Document parse = connect
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0")
				.get();
		return parse;
	}
    protected void updateAll(long i, long total) {
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

    protected Integer convertNumerico(String eleitores) {
        String replaceAll = eleitores.replaceAll("\\D", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
    }

    protected static String getHTTPPassword() {
        return "13-juuSAN";
    }

    protected static String getHTTPUsername() {
        return "guilherme.hmedeiros";
    }

    protected static void insertProxyConfig() {
        System.setProperty("http.proxyHost", "10.70.124.16");
        System.setProperty("http.proxyPort", "3128");
        System.setProperty("https.proxyHost", "10.70.124.16");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("javax.net.ssl.trustStore",
                "C:/Users/guilherme.hmedeiros/Downloads/Instaladores/cacerts");
    
        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getHTTPUsername(), getHTTPPassword().toCharArray());
            }
    
        });
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancel = super.cancel(mayInterruptIfRunning);
        setCancelled(true);
        return cancel;
    }

    @Override
    protected String call() throws Exception {
        start = Instant.now();
        return task();
    }

    protected abstract String task();
    protected LocalDate extractDate(String children) {
        try {
            return LocalDate.parse(children, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
