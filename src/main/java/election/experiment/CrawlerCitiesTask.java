package election.experiment;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import japstudy.db.HibernateUtil;
import javafx.concurrent.Task;
import simplebuilder.HasLogging;

public class CrawlerCitiesTask extends Task<String> implements HasLogging {

    private CidadeDAO cidadeDAO = new CidadeDAO();

    @Override
    protected String call() throws Exception {

        String encoded = Base64.getEncoder().encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes());
        List<String> estados = Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es", "go", "ma", "mg", "ms", "mt",
                "pa", "pb", "pe", "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp", "to");
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        updateTitle("Example Task");
        updateMessage("Starting...");
        insertProxyConfig();
        final int total = estados.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (String estado : estados) {
            Thread thread = new Thread(() -> {
                for (String letter : alphabet.split("")) {
                    Connection connect = Jsoup
                            .connect("https://www.eleicoes2016.com.br/" + estado + "/" + letter + "/");
                    connect.header("Proxy-Authorization", "Basic " + encoded);
                    try {
                        Document parse = connect.userAgent(
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0")
                                .get();

                        Elements select = parse.select(".lista-estados .custom li");
                        for (Element element : select) {
                            Element link = element.select("a").first();
                            String href = link.attr("href");
                            String eleitores = element.select("span").first().text().replaceAll("\\D", "");
                            Cidade cidade = new Cidade();
                            cidade.setHref(href);
                            cidade.setEleitores(convertNumerico(eleitores));
                            cidade.setNome(link.text());
                            cidade.setEstado(estado.toUpperCase());
                            cidadeDAO.saveOrUpdate(cidade);
                        }
                    } catch (Exception e) {
                        getLogger().error("ERRO cidade " + estado + " " + letter, e);
                    }
                }

            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > 5) {
                count = ths.stream().filter(Thread::isAlive).count();
                long i = ths.size() - count;
                updateAll(i, total);
            }
        }
        while (ths.stream().anyMatch(Thread::isAlive)) {
            long count = ths.stream().filter(Thread::isAlive).count();
            long i = ths.size() - count;
            updateAll(i, total);
        }
        updateAll(total, total);
        HibernateUtil.shutdown();

        return "Completed at " + LocalTime.now();
    }

    private void updateAll(long i, final int total) {
        updateTitle("Example Task (" + i + ")");
        updateMessage("Processed " + i + " of " + total + " items.");
        updateProgress(i, total);
    }

    private Integer convertNumerico(String eleitores) {
        String replaceAll = eleitores.replaceAll("\\D", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
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