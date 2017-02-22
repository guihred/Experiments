package java8.exercise;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simplebuilder.ResourceFXUtils;

public final class Chapter2 {

	private static final Logger LOGGER = LoggerFactory.getLogger(Chapter2.class);

	private static final String TXT_FILE = "warAndPeace.txt";

	private Chapter2() {
	}

	private static long countConcurrentWithoutStreams() throws IOException, InterruptedException, ExecutionException {
		List<String> words = getWordsAsList();
		int cores = Runtime.getRuntime().availableProcessors();
		int chunkSize = words.size() / cores;
		List<List<String>> chunks = new LinkedList<>();
		for (int i = 0; i < words.size(); i += chunkSize) {
			chunks.add(words.subList(i, i + Math.min(chunkSize, words.size() - i)));
		}
		ExecutorService pool = Executors.newFixedThreadPool(cores);
		Set<Future<Long>> set = new HashSet<>();
		for (List<String> strings : chunks) {
			set.add(pool.submit(() -> {
				long chunkCount = 0;
				for (String string : strings) {
					if (string.length() > 12) {
						chunkCount++;
					}
				}
				return chunkCount;
			}));
		}
		long count = 0;
		for (Future<Long> future : set) {
			count += future.get();
		}
		pool.shutdown();
		return count;
	}

	/**
	 * Write a parallel version of the for loop in Section 2.1, From Iteration
	 * to Stream Operations, on page 22. Obtain the number of processors. Make
	 * that many separate threads, each working on a segment of the list, and
	 * total up the results as they come in. (You don't want the threads to
	 * update a single counter. Why?)
	 */
	public static void ex1() throws Exception {
		Pattern compile = Pattern.compile("[\\P{L}]+");
		System.out.println(Files.lines(Paths.get(TXT_FILE), StandardCharsets.UTF_8).parallel()
				.flatMap(compile::splitAsStream).filter(s -> s.length() > 12).count());
		System.out.println(countConcurrentWithoutStreams());
	}

	/**
	 * Write a call to reduce that can be used to compute the average of a
	 * Stream<Double>. Why can�t you simply compute the sum and divide by
	 * count()?
	 * 
	 * You can't compute sum and divide by count because both sum() and count()
	 * methods are terminal, meaning they can't be called one after the other on
	 * the same stream.
	 */
	public static void ex10() {
		Stream<Double> of2 = Stream.of(3D, 4D, 5D, 7D, 1D, 2D, 9D, 10D, 8D, 6D);
		// reduce knowing before handed the count of elements of the stream
		System.out.println(of2.reduce(0D, (a, b) -> a + b / 10));

		of2 = Stream.of(3D, 4D, 5D, 7D, 1D, 2D, 9D, 10D, 8D, 6D);
		// reduce using the standard combiner for reducing a Stream of Double
		double average = of2.reduce(new DoubleSummaryStatistics(), (a, b) -> {
			a.accept(b);
			return a;
		}, (c, d) -> {
			c.combine(d);
			return c;
		}).getAverage();
		System.out.println(average);

	}

	public static void ex11() {
		// I don't know
	}

	/**
	 * Count all short words in a parallel Stream<String>, as described in
	 * Section 2.13, �Parallel Streams,� on page 40, by updating an array of
	 * AtomicInteger. Use the atomic getAndIncrement method to safely increment
	 * each counter.
	 */
	public static void ex12() throws IOException {

		AtomicInteger[] array = Stream.generate(() -> new AtomicInteger(0)).limit(12).toArray(AtomicInteger[]::new);
		Stream<String> wordsAsList = getWordsAsList().stream();
		wordsAsList.forEach(s -> {
			if (s.length() <= 12) {
				array[s.length() - 1].getAndIncrement();
			}
		});
		Stream.of(array).forEach(System.out::println);
	}

	public static void ex13() {
		try {
			Stream<String> wordsAsList = getWordsAsList().stream();
			Map<Integer, Long> collect = wordsAsList.filter(s -> s.length() > 12)
					.collect(Collectors.groupingBy(String::length, Collectors.counting()));
			System.out.println(collect);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * Verify that asking for the first five long words does not call the filter
	 * method once the fifth long word has been found. Simply log each method
	 * call.
	 */
	public static void ex2() throws IOException {
		Pattern compile = Pattern.compile("[\\P{L}]+");
		System.out.println();
		Files.lines(ResourceFXUtils.toPath(TXT_FILE), StandardCharsets.UTF_8).parallel().flatMap(compile::splitAsStream)
				.filter(s -> {
			if (s.length() > 12) {
				System.out.printf("Long word %s%n", s);
			}
			return s.length() > 12;
		}).limit(5).forEach(System.out::println);
	}

	/**
	 * Measure the difference when counting long words with a parallelStream
	 * instead of a stream. Call System.currentTimeMillis before and after the
	 * call, and print the difference. Switch to a larger document (such as War
	 * and Peace) if you have a fast computer.
	 */
	public static void ex3() throws IOException {

		long tic = System.currentTimeMillis();
		getWordsAsList().parallelStream().filter(s -> s.length() > 12).count();
		System.out.println("Paralel:" + (System.currentTimeMillis() - tic) + "ms");
		tic = System.currentTimeMillis();
		getWordsAsList().stream().filter(s -> s.length() > 12).count();
		System.out.println("Sequential:" + (System.currentTimeMillis() - tic) + "ms");

	}

	/**
	 * Suppose you have an array int[] values = { 1, 4, 9, 16 }. What is
	 * Stream.of(values)? How do you get a stream of int instead?
	 * 
	 * A:Stream.of(values) is an Stream of int[], because an int alone isn't a
	 * object, it's just a primitive type.
	 */
	public static void ex4() {
		int[] values = { 1, 4, 9, 16 };
		Stream.of(values).forEach(System.out::println);
		// That's is how you make a Stream of int
		IntStream.of(values).forEach(System.out::println);
	}

	/**
	 * It should be possible to concurrently collect stream results in a single
	 * ArrayList, instead of merging multiple array lists, provided it has been
	 * constructed with the stream�s size, since concurrent set operations at
	 * disjoint positions are threadsafe. How can you achieve that?
	 */

	/**
	 * Using Stream.iterate, make an infinite stream of random numbers�not by
	 * calling Math.random but by directly implementing a linear congruential
	 * generator. In such a generator, you start with x0 = seed and then produce
	 * xn + 1 = (a xn + c) % m, for appropriate values of a, c, and m. You
	 * should implement a method with parameters a, c, m, and seed that yields a
	 * Stream<Long>. Try out a = 25214903917, c = 11, and m = 248.
	 */
	public static void ex5() {
		long a = 25214903917L;
		long c = 11;
		long m = 2L << 48;
		Stream<Long> iterate = Stream.iterate(System.currentTimeMillis(), t -> (a * t + c) % m);
		iterate.limit(10).forEach(System.out::println);
	}

	/**
	 * The characterStream method in Section 2.3, The filter, map, and flatMap
	 * Methods, on page 25, was a bit clumsy, first filling an array list and
	 * then turning it into a stream. Write a stream-based one-liner instead.
	 * One approach is to make a stream of integers from 0 to s.length() - 1 and
	 * map that with the s::charAt method reference.
	 */
	public static void ex6() {
		// public static Stream<Character> characterStream(String s) {
		// List<Character> result = new ArrayList<>();
		// for (char c : s.toCharArray()) result.add(c);
		// return result.stream();
		// }
		String s = "asdasdasdasd";
		Stream<Character> map = Stream.iterate(0, i -> i + 1).limit(s.length()).map(s::charAt);
		map.forEach(System.out::println);
	}

	/**
	 * Write a method public static <T> Stream<T> zip(Stream<T> first, Stream<T>
	 * second) that alternates elements from the streams first and second,
	 * stopping when one of them runs out of elements.
	 */
	public static void ex8() {
		zip(Stream.of(1, 2, 3), Stream.of(1, 2)).forEach(System.out::println);
	}

	/**
	 * Join all elements in a Stream<ArrayList<T>> to one ArrayList<T>. Show how
	 * to do this with the three forms of reduce.
	 */
	public static void ex9() {
		Stream<ArrayList<String>> of = Stream.of(new ArrayList<>(Arrays.asList("A", "B")), new ArrayList<>(Arrays.asList("D", "C")));
		System.out.println(of.reduce((a, b) -> {
			a.addAll(b);
			return a;
		}).get());
		Stream<ArrayList<String>> of2 = Stream.of(new ArrayList<>(Arrays.asList("A", "B")), new ArrayList<>(Arrays.asList("D", "C")));
		System.out.println(of2.reduce(new ArrayList<String>(), (a, b) -> {
			a.addAll(b);
			return a;
		}));
		Stream<ArrayList<String>> of3 = Stream.of(new ArrayList<>(Arrays.asList("A", "B")), new ArrayList<>(Arrays.asList("D", "C")));
		System.out.println(of3.reduce(new ArrayList<String>(), (a, b) -> {
			a.addAll(b);
			return a;
		}, (c, d) -> {
			c.addAll(d);
			return c;
		}));

	}
	private static List<String> getWordsAsList() throws IOException {
		String contents = new String(Files.readAllBytes(ResourceFXUtils.toPath(TXT_FILE)), StandardCharsets.UTF_8);
		return Arrays.asList(contents.split("[\\P{L}]+"));
	}

	public static void main(String[] args) {
		// ex1();
		// ex2();
		ex13();
	}

	private static <T> Stream<T> zip(Stream<T> first, Stream<T> second) {
		Iterator<T> iterator = second.iterator();

		return first.flatMap(t -> {
			if (iterator.hasNext()) {
				return Stream.of(t, iterator.next());
			}
			first.close();
			return null;
		});

	}



}
