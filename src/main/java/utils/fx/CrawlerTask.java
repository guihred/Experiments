package utils.fx;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javafx.concurrent.Task;
import org.apache.commons.lang3.time.DurationFormatUtils;

public abstract class CrawlerTask extends Task<String> {

    private Instant start;

    private boolean cancelled;

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        boolean cancel = super.cancel(mayInterruptIfRunning);
        setCancelled(true);
        return cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    protected String call() throws Exception {
        start = Instant.now();
        String task = task();
        long between = ChronoUnit.MILLIS.between(start, Instant.now());
        String formatDuration = DurationFormatUtils.formatDuration(between, "H:mm:ss", true);
        updateMessage("Time Spent " + formatDuration);
        return task;
    }

    protected abstract String task();

    protected void updateAll(final long i, final long total) {
        updateTitle("Processed " + i + " of " + total + " items.");
        if (i > 0 && total > i) {

            long between = ChronoUnit.MILLIS.between(start, Instant.now());
            String formatDuration = DurationFormatUtils.formatDuration(between * (total - i) / i, "H:mm:ss", true);
            updateMessage("Time estimated: " + formatDuration);
        } else {
            updateMessage("Time estimated unknown");
        }
        updateProgress(i, total);
    }

}
