package election.experiment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

final class CrawlerCompleteCandidateTask extends CommonCrawlerTask<Integer> {

    private static final int STEP = 50;
    private CandidatoDAO candidatoDAO = new CandidatoDAO();
    @Override
    protected List<Integer> getList() {
        long total = candidatoDAO.size();
        return Stream.iterate(0, i -> i + STEP).limit(total / STEP).collect(Collectors.toList());
    }

    @Override
    public void performTask(Integer j) {
        List<Candidato> candidatos = candidatoDAO.list(j, STEP);
        for (Candidato candidato : candidatos) {
            extractCandidateInfo(candidato);
        }
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
                break;
            } catch (Exception e) {
                getLogger().error("ERRO candidato " + candidato, e);
            }
        }
    }




}