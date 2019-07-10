package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasLogging {

	default Logger getLogger() {
		return LoggerFactory.getLogger(getClass());
	}

    default String printf(String s, Object... objects) {
        String format = String.format(s, objects);
        getLogger().info(format);
        return format;
    }

    static String getCurrentClass(int i) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int index = indexOf(stackTrace) + i;
		StackTraceElement stackTraceElement = stackTrace[Integer.min(index + 1, stackTrace.length - 1)];
        return stackTraceElement.getClassName();
    }

    static int indexOf(StackTraceElement[] stackTrace) {
		int index = stackTrace.length - 1;
		for (int i = 0; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().equals(HasLogging.class.getName())) {
				index = i;
			}
		}
		return index;
	}

	static Logger log() {
		return log(0);
	}

    static Logger log(Class<?> cls) {
        return LoggerFactory.getLogger(cls);
    }

	static Logger log(int i) {
		String className = getCurrentClass(i);
        return LoggerFactory.getLogger(className);
    }
}
