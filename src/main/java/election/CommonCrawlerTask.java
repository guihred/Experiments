package election;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import utils.ExtractUtils;
import utils.HibernateUtil;
import utils.ex.RunnableEx;
import utils.fx.CrawlerTask;

public abstract class CommonCrawlerTask<T> extends CrawlerTask {
    private static final int MAX_THREAD_COUNT = 10;

    /**
     * @param lines  
     */
    @SuppressWarnings("unused")
    protected void endTask(List<T> lines) {
        HibernateUtil.shutdown();
    }

    protected abstract List<T> getList();

    protected abstract void performTask(T cidade);

    @Override
    protected String task() {
        updateTitle("Example Task " + getClass().getSimpleName());
        updateMessage("Starting...");
        ExtractUtils.insertProxyConfig();
        List<T> cidades = getList();
        final int total = cidades.size();
        updateProgress(0, total);
        List<Thread> ths = new ArrayList<>();
        for (T cidade : cidades) {
            if (isCancelled()) {
                return "Cancelled";
            }
            ths.add(RunnableEx.runNewThread(() -> performTask(cidade)));
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
        endTask(cidades) ;
        return "Completed at " + LocalTime.now();
    }

}