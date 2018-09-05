package log.analyze;

import java.util.function.Function;
import simplebuilder.HasLogging;

@FunctionalInterface
public interface FunctionEx<T, R> {
	R apply(T t) throws Exception;

    static <A, B> Function<A, B> makeFunction(FunctionEx<A, B> run) {
        return (A a) -> {
            try {
                return run.apply(a);
            } catch (Exception e) {
                HasLogging.log().error("", e);
                return null;
            }
        };
    }

}