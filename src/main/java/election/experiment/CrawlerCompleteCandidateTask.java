package election.experiment;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import japstudy.db.HibernateUtil;
import javafx.concurrent.Task;
import simplebuilder.HasLogging;

final class CrawlerCompleteCandidateTask extends Task<String> implements HasLogging {

    private static final int STEP = 10;
    private CandidatoDAO candidatoDAO = new CandidatoDAO();
    private LocalTime start = LocalTime.now();
    private AtomicInteger completeCandidates = new AtomicInteger(0);

    @Override
    protected String call() throws Exception {

        updateTitle("Example Task");
        updateMessage("Starting...");
        insertProxyConfig();
        Long total = candidatoDAO.size();
        start = LocalTime.now();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        String encoded = Base64.getEncoder().encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes());
        stateProperty().addListener(observable -> {
            if (getState() == State.CANCELLED) {
                ths.stream().filter(Thread::isAlive).forEach(Thread::interrupt);
            }
        });
        for (int i = 0; i < total; i += STEP) {
            List<Candidato> candidatos = candidatoDAO.list(i, STEP);
            Thread thread = new Thread(() -> {
                for (Candidato candidato : candidatos) {
                    int tried = 0;
                    while (tried < 10) {
                        try {
                            Connection connect = Jsoup
                                    .connect("https://www.eleicoes2016.com.br/" + candidato.getHref() + "/");
                            connect.header("Proxy-Authorization", "Basic " + encoded);
                            Document parse = connect.userAgent(
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0")
                                    .get();
                            Elements select = parse.select(".info-candidato");
                            Elements children = select.first().children();
                            String nomeCompleto = children.get(0).child(1).text();
                            candidato.setNomeCompleto(nomeCompleto);
                            candidato.setNascimento(extractDate(children.get(2).child(1).text()));
                            candidato.setNaturalidade(children.get(3).child(1).text());
                            candidato.setOcupacao(children.get(5).child(1).text());
                            candidato.setGrauInstrucao(children.get(6).child(1).text());
                            candidatoDAO.saveOrUpdate(candidato);
                            completeCandidates.getAndIncrement();
                            break;
                        } catch (Exception e) {
                            getLogger().error("ERRO candidato " + candidato, e);
                            tried++;
                        }
                    }
                }
            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > 0) {
                count = ths.stream().filter(Thread::isAlive).count();
                updateAll(total);
            }
        }

        while (ths.stream().anyMatch(Thread::isAlive)) {
            updateAll(total);
        }
        updateAll(total);
        HibernateUtil.shutdown();
        return "Completed at " + LocalTime.now();
    }

    private LocalDate extractDate(String children) {
        try {
            return LocalDate.parse(children, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    private void updateAll(Long total) {
        int i = completeCandidates.get();
        updateTitle("Processed " + i + " of " + total + " items.");
        long between = ChronoUnit.MILLIS.between(start, LocalTime.now());
        updateMessage("Time estimated " + Duration.ofMillis(i / between * (total - i)));
        updateProgress(i, total);
    }

    private String getHTTPPassword() {
        return "13-juuSAN";
    }

    private String getHTTPUsername() {
        return "guilherme.hmedeiros";
    }

    private void insertProxyConfig() {
        System.setProperty("https.proxyHost", "10.70.124.16");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/guilherme.hmedeiros/Downloads/Instaladores/cacerts");

        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getHTTPUsername(), getHTTPPassword().toCharArray());
            }

        });
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}