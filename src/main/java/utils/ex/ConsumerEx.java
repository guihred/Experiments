package utils.ex;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface ConsumerEx<T> {
    void accept(T t) throws Exception;

    static <M> void accept(ConsumerEx<M> run, M t) {
        ConsumerEx.makeConsumer(run).accept(t);
    }

    static <M> void foreach(Collection<M> collection,ConsumerEx<M> run) {
        collection.forEach(ConsumerEx.makeConsumer(run));
    }

    static <M> Consumer<M> ignore(ConsumerEx<M> run) {
        return o -> {
            try {
                run.accept(o);
            } catch (Exception e) {
                HasLogging.log(1).trace("", e);
            }
        };
    }

    static <M> Consumer<M> make(ConsumerEx<M> run, BiConsumer<M, Throwable> onError) {
        return o -> {
            try {
                run.accept(o);
            } catch (Throwable e) {
                onError.accept(o, e);
            }
        };
    }
    static <M> Consumer<M> makeConsumer(ConsumerEx<M> run) {
        return o -> {
            try {
                run.accept(o);
            } catch (Exception e) {
                HasLogging.log(1).error("", e);
            }
        };
    }

}