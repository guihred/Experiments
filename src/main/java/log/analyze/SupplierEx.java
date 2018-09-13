package log.analyze;

import java.util.function.Supplier;
import simplebuilder.HasLogging;

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