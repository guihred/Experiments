package utils;

import java.util.function.Supplier;

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

}