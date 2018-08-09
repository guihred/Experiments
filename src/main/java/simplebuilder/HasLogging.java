package simplebuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasLogging {

	default Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

    default void logln(Object x) {
        System.out.println(x);
    }

    public static Logger log(Class<?> cls) {
        return LoggerFactory.getLogger(cls);
    }

    public static Logger log() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return LoggerFactory.getLogger(stackTrace[stackTrace.length - 1].getClassName());
    }
}
