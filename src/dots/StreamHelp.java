package dots;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

public class StreamHelp {

	@SuppressWarnings("unchecked")
	public static <E, T extends Collection<E>> T filter(T filter, Predicate<E> pred) {
		try {
			T newInstance = (T) filter.getClass().newInstance();
			for (E e : filter) {
				if (pred.test(e)) {
					newInstance.add(e);
				}
			}
			return newInstance;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static <E, T extends Collection<E>> List<E> toList(T filter) {
		List<E> a = new ArrayList<E>();
		for (E e : filter) {
			a.add(e);
		}
		return a;
	}

	public static <E, T extends Collection<E>, A, R> R collect(T filter, Collector<E, A, R> collector) {
		A a2 = collector.supplier().get();
		for (E e : filter) {
			collector.accumulator().accept(a2, e);
		}
		return collector.finisher().apply(a2);
	}

	public static <E, T extends Collection<E>> Set<E> toSet(T filter) {
		Set<E> a = new HashSet<E>();
		for (E e : filter) {
			a.add(e);
		}
		return a;
	}

	public static <E, X> Map<X, List<E>> groupBy(Collection<E> lista, Function<E, X> function) {
		Map<X, List<E>> hashMap = new HashMap<>();
		for (E e : lista) {
			X apply = function.apply(e);
			if (!hashMap.containsKey(apply)) {
				hashMap.put(apply, new ArrayList<E>());
			}
			hashMap.get(apply).add(e);
		}
		return hashMap;
	}

	public static double[] doubleArray(Double[] filter) {
		double[] a = new double[filter.length];
		for (int i = 0; i < filter.length; i++) {
			a[i] = filter[i];
		}
		return a;
	}

	public static int min(Iterable<Integer> a, int orElse) {
		int min = Integer.MAX_VALUE;

		for (Integer i : a) {
			min = i < min ? i : min;
		}

		return min == Integer.MAX_VALUE ? orElse : min;
	}

	public static <E, Z, T extends Collection<E>> List<Z> flatMap(T filter, Function<E, ? extends Collection<Z>> function) {
		List<Z> a = new ArrayList<>();
		for (E e : filter) {
			a.addAll(function.apply(e));
		}
		return a;
	}

	public static <E, Z, T extends Collection<E>> List<Z> map(T filter, Function<E, Z> function) {
		List<Z> a = new ArrayList<>();
		for (E e : filter) {
			a.add(function.apply(e));
		}
		return a;
	}

	public static <E, T extends Collection<E>> boolean anyMatch(T filter, Predicate<E> pred) {
		for (E e : filter) {
			if (pred.test(e)) {
				return true;
			}
		}

		return false;
	}

}
