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
}
