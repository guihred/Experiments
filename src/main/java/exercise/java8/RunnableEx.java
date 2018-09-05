package exercise.java8;

import simplebuilder.HasLogging;

@FunctionalInterface
public
interface RunnableEx extends HasLogging {
	void run() throws Exception;

    static Runnable makeRunnable(RunnableEx run) {
        return () -> {
            try {
                run.run();
            } catch (Exception e) {
                HasLogging.log().error("", e);
            }
        };
    }

}