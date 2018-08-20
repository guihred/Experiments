package election.experiment;

import japstudy.db.HibernateUtil;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

final class CrawlerCandidateTask extends CrawlerTask {
    private static final int MAX_THREAD_COUNT = 5;

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
            while (count > MAX_THREAD_COUNT) {
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
            try {
				Document parse = getDocument("https://www.eleicoes2016.com.br" + cidade.getHref() + i);

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