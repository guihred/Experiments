package election.experiment;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import japstudy.db.HibernateUtil;
import simplebuilder.HasLogging;

public class ElectionCrawler implements HasLogging {

	private CidadeDAO cidadeDAO = new CidadeDAO();

	public static void main(String[] args) {

		new ElectionCrawler().migrateCities();

	}

	public void migrateCities() {
		List<String> asList = Arrays.asList("ap");
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		for (String estado : asList) {
			for (String letter : alphabet.split("")) {
				Connection connect = Jsoup.connect("http://www.eleicoes2016.com.br/" + estado + "/" + letter + "/");
				try {
					Document parse = connect
							.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0")
							.get();

					Elements select = parse.select(".lista-estados .custom li");
					for (Element element : select) {
						Element link = element.select("a").first();
						String href = link.attr("href");
						String eleitores = element.select("span").first().text().replaceAll("\\D", "");
						Cidade cidade = new Cidade();
						cidade.setHref(href);
						cidade.setEleitores(StringUtils.isNumeric(eleitores) ? Integer.valueOf(eleitores) : 0);
						cidade.setNome(link.text());
						cidade.setEstado(estado.toUpperCase());
						cidadeDAO.saveOrUpdate(cidade);
					}
				} catch (Exception e) {
					getLogger().error("ERRO cidade " + estado + " " + letter, e);
				}
			}
		}
		HibernateUtil.shutdown();
	}
}
