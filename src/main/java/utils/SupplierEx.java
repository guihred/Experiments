package utils;

import java.util.function.Supplier;

@FunctionalInterface
public interface SupplierEx<T> {
    T get() throws Exception;

    static <A> Supplier<A> makeSupplier(SupplierEx<A> run) {
        return () -> {
            try {
                return run.get();
            } catch (Exception e) {
                HasLogging.log().error("", e);
                return null;
            }
        };
    }

}