package election;

import extract.web.JsoupUtils;
import java.util.Arrays;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.StringSigaUtils;
import utils.ex.HasLogging;

public class CrawlerCitiesTask extends CommonCrawlerTask<String> {

    private static final Logger LOG = HasLogging.log();

    private final CidadeDAO cidadeDAO = new CidadeDAO();

    @Override
    protected List<String> getList() {
        return Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es", "go", "ma", "mg", "ms", "mt", "pa", "pb", "pe",
                "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp", "to");
    }

    @Override
    protected void performTask(String estado) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        for (String letter : alphabet.split("")) {
            crawlThroughSite(estado, letter);
        }

    }

    private void crawlThroughSite(String estado, String letter) {
        try {
            String format =
                    String.format("%s/eleicoes-2016/%s/%s/", CrawlerCandidateTask.ELEICOES_2018_URL, estado, letter);
            Document parse = JsoupUtils.getDocument(format);

            Elements select = parse.select(".lista-estados .custom li");
            for (Element element : select) {
                Element link = element.select("a").first();
                String href = link.attr("abs:href");
                String eleitores = element.select("span").first().text().replaceAll("\\D", "");
                Cidade cidade = new Cidade();
                cidade.setHref(href);
                cidade.setEleitores(StringSigaUtils.convertNumerico(eleitores));
                cidade.setNome(link.text());
                cidade.setEstado(estado.toUpperCase());
                cidadeDAO.saveOrUpdate(cidade);
            }
        } catch (Exception e) {
            LOG.trace("ERRO cidade " + estado + " " + letter, e);
        }
    }

}