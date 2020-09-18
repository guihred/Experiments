package utils.ex;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.assertj.core.api.exception.RuntimeIOException;

@FunctionalInterface
public interface RunnableEx {
    void run() throws Exception;

    static void ignore(RunnableEx run) {
        try {
            run.run();
        } catch (Throwable e) {
            HasLogging.log(1).trace("", e);
        }
    }

    static Runnable make(RunnableEx run) {
        return () -> {
            try {
                run.run();
            } catch (Exception e) {
                HasLogging.log(1).error("", e);
            }
        };
    }

    static Runnable make(RunnableEx run, ConsumerEx<Throwable> onError) {
        return () -> {
            try {
                run.run();
            } catch (Throwable e) {
                HasLogging.log(1).trace("", e);
                ConsumerEx.accept(onError, e);
            }
        };
    }

    static void measureTime(String name, RunnableEx runnable) {
        long currentTimeMillis = System.currentTimeMillis();
        run(runnable);
        long currentTimeMillis2 = System.currentTimeMillis();
        long arg2 = currentTimeMillis2 - currentTimeMillis;
        String formatDuration = DurationFormatUtils.formatDuration(arg2, "HHH:mm:ss.SSS");
        HasLogging.log(1).info("{} took {}", name, formatDuration);
    }

    static RuntimeIOException newException(String onError, Throwable e) {

        return new RuntimeIOException(onError, e);
    }

    static void remap(RunnableEx run, String onError) {
        rethrow(run, onError).run();
    }

    static Runnable rethrow(RunnableEx run, String onError) {
        return make(run, e -> {
            throw newException(onError, e);
        });
    }

    static void run(RunnableEx run) {
        try {
            run.run();
        } catch (Exception e) {
            HasLogging.log(1).error("", e);
        }
    }

    static <T> void runIf(T length, ConsumerEx<T> func) {
        if (length != null) {
            ConsumerEx.accept(func, length);
        }
    }

    static Thread runNewThread(RunnableEx run) {
        Thread thread = new Thread(make(run));
        thread.start();
        return thread;
    }

    static <A> Thread runNewThread(SupplierEx<A> run, ConsumerEx<A> onEnd) {

        return RunnableEx.runNewThread(() -> onEnd.accept(run.get()));
    }

    static void sleepSeconds(double seconds) {
        RunnableEx.run(() -> Thread.sleep((long) (seconds * 1000)));
    }
}
