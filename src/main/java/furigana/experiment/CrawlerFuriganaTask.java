package furigana.experiment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import election.experiment.CrawlerTask;
import japstudy.db.HibernateUtil;

public class CrawlerFuriganaTask extends CrawlerTask {

	// private CidadeDAO cidadeDAO = new CidadeDAO();

	private static final int NUMBER_THREADS = 1;

	@Override
	protected String task() {

        List<String> estados = Arrays.asList("ac", "al", "am", "ap", "ba", "ce", "es", "go", "ma", "mg", "ms", "mt",
                "pa", "pb", "pe", "pi", "pr", "rj", "rn", "ro", "rr", "rs", "sc", "se", "sp", "to");
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        updateTitle("Example Task");
        updateMessage("Starting...");
        final int total = estados.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (String estado : estados) {
            Thread thread = new Thread(() -> {
                for (String letter : alphabet.split("")) {
                    Connection connect = Jsoup
							.connect("http://jisho.org/search/" + estado + "/" + letter + "/");
                    try {
                        Document parse = connect.userAgent(
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0")
                                .get();

                        Elements select = parse.select(".lista-estados .custom li");
                        for (Element element : select) {
                            Element link = element.select("a").first();
                            link.attr("href");
                            element.select("span").first().text().replaceAll("\\D", "");
                        }
                    } catch (Exception e) {
                        getLogger().error("ERRO cidade " + estado + " " + letter, e);
                    }
                }

            });
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
			while (count > NUMBER_THREADS) {
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


}