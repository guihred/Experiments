package election;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public final class CrawlerCompleteCandidateTask extends CommonCrawlerTask<Integer> {

    private static final int STEP = 50;
    private CandidatoDAO candidatoDAO = new CandidatoDAO();
    @Override
    public void performTask(Integer j) {
        List<Candidato> candidatos = candidatoDAO.list(j, STEP);
        for (Candidato candidato : candidatos) {
            extractCandidateInfo(candidato);
        }
    }

    @Override
    protected List<Integer> getList() {
        long total = candidatoDAO.size();
        return Stream.iterate(0, i -> i + STEP).limit(total / STEP).collect(Collectors.toList());
    }

    private void extractCandidateInfo(Candidato candidato) {
        for (int tried = 0; tried < 3; tried++) {
            try {
                Document parse = getDocument("https://www.todapolitica.com" + candidato.getHref());
                Elements select = parse.select(".info-candidato");
                Elements children = select.first().children();
                String nomeCompleto = children.get(0).child(1).text();
                candidato.setFotoUrl(parse.select("#topo-candidato .imagem img").attr("src"));
                candidato.setNomeCompleto(nomeCompleto);
                candidato.setNascimento(extractDate(children.get(2).child(1).text()));
                candidato.setNaturalidade(children.get(3).child(1).text());
                candidato.setOcupacao(children.get(5).child(1).text());
                candidato.setGrauInstrucao(children.get(6).child(1).text());
                candidatoDAO.saveOrUpdate(candidato);
                break;
            } catch (Exception e) {
                getLogger().trace("ERRO candidato {}", candidato);
                getLogger().trace("ERRO candidato " + candidato, e);
            }
        }
    }




}