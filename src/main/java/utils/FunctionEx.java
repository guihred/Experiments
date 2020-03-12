package utils;

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

@FunctionalInterface
public interface FunctionEx<T, R> {
    R apply(T t) throws Exception;

    static <A, B> B apply(FunctionEx<A, B> run, A a) {
        return apply(run, a, null);
    }

    static <A, B> B apply(FunctionEx<A, B> run, A a, B orElse) {
        try {
            return run.apply(a);
        } catch (Exception e) {
            HasLogging.log(1).trace("", e);
            return orElse;
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

    static <T, F> F mapIf(T length, FunctionEx<T, F> func) {
        return FunctionEx.mapIf(length, func, null);
    }

    static <T, F> F mapIf(T length, FunctionEx<T, F> func, F f) {
        T t = length;
        if (t != null && StringUtils.isNotEmpty(apply(Objects::toString, t, ""))) {
            F apply = apply(func, t);
            if (apply != null) {
                return apply;
            }
        }
        return f;
    }

}