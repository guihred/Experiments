package election.experiment;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

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

        new ElectionCrawler().migrateCandidates();

	}

	public void migrateCities() {
        insertProxyConfig();
        String encoded = Base64.getEncoder().encodeToString((getHTTPUsername() + ":" + getHTTPPassword()).getBytes());
		List<String> asList = Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es", "go", "ma", "mg", "ms", "mt",
				"pa", "pb", "pe", "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp", "to");
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		for (String estado : asList) {
			for (String letter : alphabet.split("")) {
                Connection connect = Jsoup.connect("https://www.eleicoes2016.com.br/" + estado + "/" + letter + "/");
                connect.header("Proxy-Authorization", "Basic " + encoded);
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
						cidade.setEleitores(convertNumerico(eleitores));
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

    private void insertProxyConfig() {
        System.setProperty("https.proxyHost", "10.70.124.16");
        System.setProperty("https.proxyPort", "3128");
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/guilherme.hmedeiros/Downloads/Instaladores/cacerts");

        Authenticator.setDefault(new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getHTTPUsername(), getHTTPPassword().toCharArray());
            }

        });
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    private String getHTTPPassword() {
        return "13-juuSAN";
    }

    private String getHTTPUsername() {
        return "guilherme.hmedeiros";
    }

    private Integer convertNumerico(String eleitores) {
		String replaceAll = eleitores.replaceAll("\\D", "");
        return StringUtils.isNumeric(replaceAll) ? Long.valueOf(replaceAll).intValue() : 0;
	}

	public void migrateCandidates() {
        insertProxyConfig();
		List<Cidade> cidades = cidadeDAO.list();
		for (Cidade cidade : cidades) {
			int i = 2;
			while (true) {

                Connection connect = Jsoup.connect("https://www.eleicoes2016.com.br" + cidade.getHref() + i);
				try {
					Document parse = connect
							.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0")
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
                    break;
				}
			}
		}
		HibernateUtil.shutdown();
	}
}
