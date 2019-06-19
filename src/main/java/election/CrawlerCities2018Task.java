package election;

import java.util.Arrays;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerCities2018Task extends CommonCrawlerTask<String> {

    private CidadeDAO cidadeDAO = new CidadeDAO();

    @Override
    protected List<String> getList() {

        List<String> estados = Arrays.asList("acre", "alagoas", "amazonas", "amapa", "bahia", "ceara", "espirito-santo",
            "goias", "maranhao", "minas-gerais", "mato-grosso-do-sul", "mato-grosso", "para", "paraiba", "pernambuco",
            "piaui", "parana", "rio-de-janeiro", "rio-grande-do-norte", "rondonia", "roraima", "rio-grande-do-sul",
            "santa-catarina", "sergipe", "sao-paulo", "tocantins");
        Arrays.asList("");
        return estados;
    }

    @Override
    protected void performTask(String estado) {
        crawlThroughSite(estado);

    }

    private void crawlThroughSite(String estado) {
        int i = 1;
        while (true) {
            try {
                Document parse = getDocument("https://www.todapolitica.com/eleicoes-2018/" + estado + "/" + i + "/");

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
                getLogger().trace("ERRO cidade " + estado, e);
            }
        }
    }


}