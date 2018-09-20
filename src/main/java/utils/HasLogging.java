package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasLogging {

	default Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

    static Logger log(Class<?> cls) {
        return LoggerFactory.getLogger(cls);
    }

    default String printf(String s, Object... objects) {
        String format = String.format(s, objects);
        getLogger().info(format);
        return format;
    }

    static Logger log() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return LoggerFactory.getLogger(stackTrace[stackTrace.length - 1].getClassName());
    }
}
