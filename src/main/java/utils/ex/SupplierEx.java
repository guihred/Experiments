package utils.ex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.assertj.core.api.exception.RuntimeIOException;

@FunctionalInterface
public interface SupplierEx<T> {
    T get() throws Exception;

    static <A> A get(SupplierEx<A> run) {
        return makeSupplier(run, e -> HasLogging.log(1).error("", e)).get();
    }

    static <A> A get(SupplierEx<A> run, A orElse) {
        return getHandle(run, orElse, e -> HasLogging.log(1).info("", e));
    }

    @SafeVarargs
    static <A> A getFirst(SupplierEx<A>... run) {
        List<Throwable> exceptions = new ArrayList<>();
        for (SupplierEx<A> supplierEx : run) {
            A a = makeSupplier(supplierEx, exceptions::add).get();
            if (a != null) {
                return a;
            }
        }
        if (!exceptions.isEmpty()) {
            HasLogging.log(1).error("ERROR {} {}", HasLogging.getCurrentLine(1),
                    exceptions.stream().map(Throwable::getMessage).collect(Collectors.toList()));
        }
        return null;
    }

    static <A> A getHandle(SupplierEx<A> run, A orElse, ConsumerEx<Throwable> onError) {
        try {
            A a = run.get();
            return a != null ? a : orElse;
        } catch (Throwable e) {
            HasLogging.log(1).trace("", e);
            ConsumerEx.accept(onError, e);
            return orElse;
        }
    }

    static <A> A getIgnore(SupplierEx<A> run) {
        return getIgnore(run, null);
    }

    static <A> A getIgnore(SupplierEx<A> run, A orElse) {
        return getHandle(run, orElse, e -> HasLogging.log(1).trace("", e));
    }



    static <A> Supplier<A> makeSupplier(SupplierEx<A> run, ConsumerEx<Throwable> onError) {
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