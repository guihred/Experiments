package utils;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import utils.ex.FunctionEx;

public class SimpleSummary<T extends Comparable<T>> implements Collector<T, SimpleSummary<T>, SimpleSummary<T>> {
    private int count;
    private T max;
    private T min;

    @Override
    public BiConsumer<SimpleSummary<T>, T> accumulator() {
        return (sum, t) -> sum.accept(t);
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    @Override
    public BinaryOperator<SimpleSummary<T>> combiner() {
        return (sum0, sum1) -> {
            sum0.count += sum1.count;
            sum0.max = sum0.max(sum1.max);
            sum0.min = sum0.min(sum1.min);
            return sum0;
        };
    }

    @Override
    public Function<SimpleSummary<T>, SimpleSummary<T>> finisher() {
        return s -> s;
    }

    public String format(FunctionEx<T, String> f) {

        return String.format("(%s - %s) %d", FunctionEx.apply(f, min, ""), FunctionEx.apply(f, max, ""), count);
    }

    public int getCount() {
        return count;
    }

    public T getMax() {
        return max;
    }

    public T getMin() {
        return min;
    }

    @Override
    public Supplier<SimpleSummary<T>> supplier() {
        return SimpleSummary::new;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", count, min, max);
    }

    private void accept(T a) {
        max = max(a);
        min = min(a);
        count++;
    }

    private T max(T a) {
        if (max == null) {
            return a;
        }
        return max.compareTo(a) < 0 ? a : max;
    }

    private T min(T a) {
        if (min == null) {
            return a;
        }
        return min.compareTo(a) > 0 ? a : min;
    }

}