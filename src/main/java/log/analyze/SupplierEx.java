package log.analyze;

import simplebuilder.HasLogging;

@FunctionalInterface
public interface SupplierEx<T> {
    T get() throws Exception;

    static <A> SupplierEx<A> makeSupplier(SupplierEx<A> run) {
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