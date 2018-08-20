package election.experiment;

import japstudy.db.HibernateUtil;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
                updateAll(completeCandidates.get(), total);
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
        for (int tried = 0; tried < 10; tried++) {
            try {
				Document parse = getDocument("https://www.eleicoes2016.com.br/" + candidato.getHref() + "/");
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