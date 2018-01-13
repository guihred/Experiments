package election.experiment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import japstudy.db.HibernateUtil;

final class CrawlerCompleteCandidateTask extends CrawlerTask {

    private static final int STEP = 100;
    private CandidatoDAO candidatoDAO = new CandidatoDAO();
    private AtomicInteger completeCandidates = new AtomicInteger(0);

    @Override
    protected String task() {
        updateTitle("Example Task");
        updateMessage("Starting...");
        insertProxyConfig();
        long total = candidatoDAO.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (int i = 0; i < total; i += STEP) {
            if (isCancelled()) {
                return "Cancelled";
            }

            final int j = i;
            Thread thread = new Thread(() -> {
                List<Candidato> candidatos = candidatoDAO.list(j, STEP);
                for (Candidato candidato : candidatos) {
                    extractCandidateInfo(candidato);
                }
            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > 10) {
                count = ths.stream().filter(Thread::isAlive).count();
                updateAll(i, total);
            }
        }
        while (ths.stream().anyMatch(Thread::isAlive)) {
            updateAll(completeCandidates.get(), total);
        }
        updateAll(completeCandidates.get(), total);
        HibernateUtil.shutdown();
        return "Completed at " + LocalTime.now();
    }

    private void extractCandidateInfo(Candidato candidato) {
        String encoded = Base64.getEncoder().encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes());
        for (int tried = 0; tried < 10; tried++) {
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
            }
        }
    }




}