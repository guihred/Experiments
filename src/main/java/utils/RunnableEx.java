package utils;

@FunctionalInterface
public
interface RunnableEx extends HasLogging {
	void run() throws Exception;

    static Runnable makeRunnable(RunnableEx run) {
        return () -> {
            try {
                run.run();
            } catch (Exception e) {
				HasLogging.log(1).error("", e);
            }
        };
    }

}