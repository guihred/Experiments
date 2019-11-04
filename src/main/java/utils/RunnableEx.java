package utils;

import java.util.function.Consumer;
import org.assertj.core.api.exception.RuntimeIOException;

@FunctionalInterface
public interface RunnableEx {
    void run() throws Exception;

    static <T> void runIf(T length, ConsumerEx<T> func) {
        if (length != null) {
            ConsumerEx.makeConsumer(func).accept(length);
        }
    }

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

    static Runnable make(RunnableEx run, Consumer<Throwable> onError) {
        return () -> {
            try {
                run.run();
            } catch (Throwable e) {
                HasLogging.log(1).trace("", e);
                onError.accept(e);
            }
        };
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
}
