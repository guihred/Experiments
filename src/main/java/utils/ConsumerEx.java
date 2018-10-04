package utils;

import java.util.function.Consumer;

@FunctionalInterface
public
interface ConsumerEx<T> extends HasLogging {
    void accept(T t) throws Exception;

    static <M> Consumer<M> makeConsumer(ConsumerEx<M> run) {
        return o -> {
            try {
                run.accept(o);
            } catch (Exception e) {
                HasLogging.log().error("", e);
            }
        };
    }

}