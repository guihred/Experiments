package election.experiment;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.LocalTime;
import java.util.ArrayList;
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

final class CrawlerCandidateTask extends Task<String> implements HasLogging {

    private CidadeDAO cidadeDAO = new CidadeDAO();
    @Override
    protected String call() throws Exception {
        updateTitle("Example Task");
        updateMessage("Starting...");
        insertProxyConfig();
        List<Cidade> cidades = cidadeDAO.list();
        final int total = cidades.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (Cidade cidade : cidades) {
            Thread thread = new Thread(() -> {
                int i = 2;
                while (true) {
                    Connection connect = Jsoup.connect("https://www.eleicoes2016.com.br" + cidade.getHref() + i);
                    try {
                        Document parse = connect.userAgent(
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0")
                                .get();

                        Elements select = parse.select(".candidato-js");
                        for (Element element : select) {
                            Candidato candidato = new Candidato();
                            candidato.setCidade(cidade);
                            candidato.setCargo(element.select(".cargo").text());
                            candidato.setFotoUrl(element.select(".foto img").attr("src"));
                            candidato.setHref(element.attr("data-url"));
                            candidato.setNome(element.select(".nome b").text());
                            String text = element.select(".nome span").text();
                            candidato.setNumero(convertNumerico(text));
                            candidato.setPartido(element.select(".partido").text());
                            candidato.setVotos(convertNumerico(element.select(".votos").first().text()));
                            candidato.setEleito("Eleito".equalsIgnoreCase(element.select(".info .badge").text()));
                            cidadeDAO.saveOrUpdate(candidato);
                        }
                        if (select.isEmpty()) {
                            break;
                        }
                        i++;
                    } catch (Exception e) {
                        getLogger().error("ERRO cidade " + cidade, e);
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
}