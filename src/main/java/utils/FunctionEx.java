package utils;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface FunctionEx<T, R> {
    R apply(T t) throws Exception;

    static <A, B> B apply(FunctionEx<A, B> run, A a) {
        try {
            return run.apply(a);
        } catch (Exception e) {
            HasLogging.log(1).trace("", e);
            return null;
        }
    }

    static <A, B> Function<A, B> ignore(FunctionEx<A, B> run) {
        return (A a) -> {
            try {
                return run.apply(a);
            } catch (Exception e) {
                HasLogging.log(1).trace("", e);
                return null;
            }
        };
    }

    static <A, B> Function<A, B> makeFunction(FunctionEx<A, B> run) {
        return (A a) -> {
            try {
                return run.apply(a);
            } catch (Exception e) {
                HasLogging.log(1).error("", e);
                return null;
            }
        };
    }

    static <T, F> F mapIf(T length, Function<T, F> func) {
        return FunctionEx.mapIf(length, func, null);
    }

    static <T, F> F mapIf(T length, Function<T, F> func, F f) {
        T t = length;
        if (t != null && !Objects.toString(t, "").isEmpty()) {
            return func.apply(t);
        }
        return f;
    }

}