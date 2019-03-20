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