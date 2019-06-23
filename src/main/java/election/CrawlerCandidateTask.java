package election;

import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public final class CrawlerCandidateTask extends CommonCrawlerTask<Cidade> {

    private CidadeDAO cidadeDAO = new CidadeDAO();

    @Override
    protected List<Cidade> getList() {
        return cidadeDAO.list();
    }

    @Override
    protected void performTask(Cidade cidade) {
        int i = 1;
        while (true) {
            try {
                String url = "https://www.todapolitica.com" + cidade.getHref() + i + "/";
                Document parse = getDocument(url);

                Elements select = parse.select(".cr-js");
                for (Element element : select) {
                    Candidato candidato = new Candidato();
                    candidato.setCidade(cidade);
                    candidato.setEstado(cidade.getEstado());
                    candidato.setCargo(element.select(".cargo").text());
                    candidato.setFotoUrl(element.select("img").attr("src"));
                    candidato.setHref(element.select(".nome").attr("href"));
                    candidato.setNome(element.select(".nome").text());
                    String text = element.select(".nome span").text();
                    candidato.setNumero(convertNumerico(text));
                    candidato.setPartido(element.select(".partido").text());
                    candidato.setVotos(convertNumerico(element.select(".votos").first().text()));
                    candidato.setEleito(element.className().contains("eleito"));
                    cidadeDAO.saveOrUpdate(candidato);
                }
                if (select.isEmpty()) {
                    break;
                }
                i++;
            } catch (Exception e) {
                getLogger().error("ERRO cidade {}", cidade);
                getLogger().trace("ERRO cidade " + cidade, e);
            }
        }

    }
}