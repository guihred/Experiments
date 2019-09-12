package utils;

import java.util.function.Consumer;
import org.assertj.core.api.exception.RuntimeIOException;

@FunctionalInterface
public
interface RunnableEx {
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
    static void remap(RunnableEx run, String onError) {
        make(run, e -> {
            throw new RuntimeIOException(onError, e);
        }).run();
    }

	static void run(RunnableEx run) {
        try {
            run.run();
        } catch (Exception e) {
            HasLogging.log(1).error("", e);
        }
    }
}
