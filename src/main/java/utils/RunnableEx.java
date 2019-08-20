package utils;

import java.util.function.Consumer;

@FunctionalInterface
public
interface RunnableEx extends HasLogging {
	void run() throws Exception;

    static void ignore(RunnableEx run) {
        try {
            run.run();
        } catch (Exception e) {
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
}
