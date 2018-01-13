package election.experiment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import japstudy.db.HibernateUtil;

final class CrawlerCandidateTask extends CrawlerTask {

    private CidadeDAO cidadeDAO = new CidadeDAO();
    @Override
    protected String task() {
        updateTitle("Example Task");
        updateMessage("Starting...");
        insertProxyConfig();
        List<Cidade> cidades = cidadeDAO.list();
        final int total = cidades.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (Cidade cidade : cidades) {
            if (isCancelled()) {
                return "Cancelled";
            }
            Thread thread = new Thread(() -> extractCityContent(cidade));
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

    private void extractCityContent(Cidade cidade) {
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
    }
}