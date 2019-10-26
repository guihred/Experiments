package utils;

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
        return () -> {
            try {
                return run.get();
            } catch (Exception e) {
                HasLogging.log(1).error("", e);
                return null;
            }
        };
    }

    static <A> A remap(SupplierEx<A> run,  String message) {
        try {
            return run.get();
        } catch (Throwable e) {
            throw new RuntimeIOException(message,e);
        }
    }

}