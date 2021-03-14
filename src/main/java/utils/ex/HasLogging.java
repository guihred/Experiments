package utils.ex;

import java.util.function.Predicate;
import java.util.stream.Stream;
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

    static String getCurrentLine(int i) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = indexOf(stackTrace) + i;
        StackTraceElement stackTraceElement = stackTrace[Integer.min(index + 1, stackTrace.length - 1)];
        return stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber();
    }

    static String getStackMatch(Predicate<? super String> predicate) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Stream.of(stackTrace).map(t -> t.getClassName() + ":" + t.getLineNumber())
                .filter(s -> !s.startsWith("java.")).filter(s -> !s.startsWith("utils.ex")).filter(predicate)
                .findFirst()
                .orElse(null);
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



	static Logger log(int i) {
		String className = getCurrentClass(i);
        return LoggerFactory.getLogger(className);
    }
}
