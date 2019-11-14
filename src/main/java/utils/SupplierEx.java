package utils;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.assertj.core.api.exception.RuntimeIOException;

@FunctionalInterface
public interface SupplierEx<T> {
    T get() throws Exception;

    static <A> A get(SupplierEx<A> run) {
        try {
            return run.get();
        } catch (Exception e) {
            HasLogging.log(1).error("", e);
            return null;
        }
    }

    static <A> A get(SupplierEx<A> run, A orElse) {
        try {
            A a = run.get();
            if (a == null) {
                return orElse;
            }
            return a;
        } catch (Throwable e) {
            HasLogging.log(1).info("", e);
            return orElse;
        }
    }

    static <A> A getIgnore(SupplierEx<A> run) {
        return getIgnore(run, null);
    }

    static <A> A getIgnore(SupplierEx<A> run, A orElse) {
        try {
            A a = run.get();
            if (a == null) {
                return orElse;
            }
            return a;
        } catch (Exception e) {
            HasLogging.log(1).trace("", e);
            return orElse;
        }
    }

    static <A> Supplier<A> makeSupplier(SupplierEx<A> run) {
        return makeSupplier(run::get, e -> HasLogging.log(1).error("", e));
    }

    static <A> Supplier<A> makeSupplier(SupplierEx<A> run, Consumer<Exception> onError) {
        return () -> {
            try {
                return run.get();
            } catch (Exception e) {
                HasLogging.log(1).trace("", e);
                onError.accept(e);
                return null;
            }
        };
    }

    static <A> A orElse(A a, SupplierEx<A> run) {

        if (a != null) {
            return a;
        }
        return SupplierEx.get(run);
    }

    static <A> A remap(SupplierEx<A> run, String message) {
        try {
            return run.get();
        } catch (Throwable e) {
            throw new RuntimeIOException(message, e);
        }
    }

}