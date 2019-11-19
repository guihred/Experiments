package utils;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.assertj.core.api.exception.RuntimeIOException;

@FunctionalInterface
public interface SupplierEx<T> {
    T get() throws Exception;

    static <A> A get(SupplierEx<A> run) {
        return makeSupplier(run::get, e -> HasLogging.log(1).error("", e)).get();
    }

    static <A> A get(SupplierEx<A> run, A orElse) {
        return getHandle(run, orElse, e -> HasLogging.log(1).info("", e));
    }

    static <A> A getHandle(SupplierEx<A> run, A orElse, Consumer<Exception> onError) {
        try {
            A a = run.get();
            if (a == null) {
                return orElse;
            }
            return a;
        } catch (Exception e) {
            HasLogging.log(1).trace("", e);
            onError.accept(e);
            return orElse;
        }
    }

    static <A> A getIgnore(SupplierEx<A> run) {
        return getIgnore(run, null);
    }

    static <A> A getIgnore(SupplierEx<A> run, A orElse) {
        return getHandle(run, orElse, e -> HasLogging.log(1).trace("", e));
    }

    static <A> Supplier<A> makeSupplier(SupplierEx<A> run) {
        return makeSupplier(run::get, e -> HasLogging.log(1).error("", e));
    }

    static <A> Supplier<A> makeSupplier(SupplierEx<A> run, Consumer<Exception> onError) {
        return () -> getHandle(run, null, onError);
    }

    @SafeVarargs
    static <T> T nonNull(T... length) {
        for (T t : length) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    static <A> A orElse(A a, SupplierEx<A> run) {
        return a != null ? a : SupplierEx.getIgnore(run);
    }

    static <A> A remap(SupplierEx<A> run, String message) {
        return getHandle(run, null, e -> {
            throw new RuntimeIOException(message, e);
        });
    }

}