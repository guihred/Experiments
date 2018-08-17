package election.experiment;

import japstudy.db.HibernateUtil;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import simplebuilder.HasLogging;

public class ElectionCrawler implements HasLogging {

	private CidadeDAO cidadeDAO = new CidadeDAO();

	public static void main(String[] args) {
        new ElectionCrawler().migrateCandidates();
	}

	public void migrateCities() {
		List<String> asList = Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es", "go", "ma", "mg", "ms", "mt",
				"pa", "pb", "pe", "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp", "to");
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		for (String estado : asList) {
			for (String letter : alphabet.split("")) {
                crawlThroughSite(estado, letter);
			}
		}
		HibernateUtil.shutdown();
	}

    private void crawlThroughSite(String estado, String letter) {
        try {
            Connection connect = Jsoup.connect("https://www.eleicoes2016.com.br/" + estado + "/" + letter + "/");
            Document parse = connect
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0").get();

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

    private Integer convertNumerico(String eleitores) {
        String replaceAll = eleitores.replaceAll("[^0-9]", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
	}

	public void migrateCandidates() {
		List<Cidade> cidades = cidadeDAO.list();
        boolean hasCandidate = true;
		for (Cidade cidade : cidades) {
            int i = 2;

            while (hasCandidate) {

                hasCandidate = crawlCitySite(cidade, i);
                i++;
			}
		}
		HibernateUtil.shutdown();
	}

    private boolean crawlCitySite(Cidade cidade, int i) {
        Connection connect = Jsoup.connect("https://www.eleicoes2016.com.br" + cidade.getHref() + i);
        try {
            Document parse = connect
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0").get();
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
                return false;
            }
        } catch (Exception e) {
            getLogger().error("ERRO cidade " + cidade, e);
        }
        return true;
    }
}
