package election.experiment;

import japstudy.db.HibernateUtil;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerCitiesTask extends CrawlerTask {

    private CidadeDAO cidadeDAO = new CidadeDAO();

    @Override
    protected String task() {

        List<String> estados = Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es", "go", "ma", "mg", "ms", "mt",
                "pa", "pb", "pe", "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp", "to");
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        updateTitle("Example Task");
        updateMessage("Starting...");
        insertProxyConfig();
        final int total = estados.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (String estado : estados) {
            Thread thread = new Thread(() -> {
                for (String letter : alphabet.split("")) {
                    crawlThroughSite(estado, letter);
                }

            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > 5) {
                count = ths.stream().filter(Thread::isAlive).count();
                long i = ths.size() - count;
                updateAll(i, total);
            }
        }
        while (ths.stream().anyMatch(Thread::isAlive)) {
            long count = ths.stream().filter(Thread::isAlive).count();
            long i = ths.size() - count;
            updateAll(i, total);
        }
        updateAll(total, total);
        HibernateUtil.shutdown();

        return "Completed at " + LocalTime.now();
    }

    private void crawlThroughSite(String estado, String letter) {
        try {
        	Document parse = getDocument("https://www.eleicoes2016.com.br/" + estado + "/" + letter + "/");

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