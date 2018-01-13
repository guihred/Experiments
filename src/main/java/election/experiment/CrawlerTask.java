package election.experiment;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javafx.concurrent.Task;
import simplebuilder.HasLogging;

public abstract class CrawlerTask extends Task<String> implements HasLogging {

    private Instant start;
    private boolean cancelled = false;

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

    protected String getHTTPPassword() {
        return "";
    }

    protected String getHTTPUsername() {
        return "";
    }

    protected void insertProxyConfig() {
        System.setProperty("https.proxyHost", "");
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

    abstract String task();
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
