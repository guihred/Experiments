package election;

import japstudy.db.HibernateUtil;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import utils.CrawlerTask;

public abstract class CommonCrawlerTask<T> extends CrawlerTask {
    private static final int MAX_THREAD_COUNT = 5;

    protected abstract List<T> getList();

    protected abstract void performTask(T cidade);

    @Override
    protected String task() {
        updateTitle("Example Task " + getClass().getSimpleName());
        updateMessage("Starting...");
        insertProxyConfig();
        List<T> cidades = getList();
        final int total = cidades.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (T cidade : cidades) {
            if (isCancelled()) {
                return "Cancelled";
            }
            Thread thread = new Thread(() -> performTask(cidade));
            ths.add(thread);
            thread.start();
            long count = ths.stream().filter(Thread::isAlive).count();
            while (count > MAX_THREAD_COUNT) {
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