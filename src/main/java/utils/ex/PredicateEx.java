package utils.ex;

import java.util.function.Predicate;

@FunctionalInterface
public interface PredicateEx<B> {
    boolean test(B b) throws Exception;

    static <B> Predicate<B> makeTest(PredicateEx<B> run) {
        return (B a) -> {
            try {
                return run.test(a);
            } catch (Throwable e) {
                HasLogging.log(1).trace("", e);
                return false;
            }
        };
    }

    static <B> boolean test(PredicateEx<B> run, B b) {
        return makeTest(run).test(b);
    }
}
