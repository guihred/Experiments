package java8.exercise;

import static simplebuilder.ResourceFXUtils.toFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Chapter6 {
	static class Matrix {
		private int[][] mat = { { 1, 1 }, { 1, 0 } };
		public Matrix() {
		}
		public Matrix(int[][] mat) {
			this.mat = mat;
		}
		public Matrix multiply(Matrix other) {
			Matrix matrix = new Matrix(new int[][] { { 0, 0 }, { 0, 0 } });
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					for (int k = 0; k < 2; k++) {
						matrix.mat[i][j] += mat[i][k] * other.mat[k][j];
					}
				}
			}
			return matrix;
		}
		@Override
		public String toString() {
			return Arrays.toString(mat[0]) + "\n" + Arrays.toString(mat[1]) + "\n";
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Chapter6.class);

	private Chapter6() {
	}

	/*
	 * Write a program that keeps track of the longest string that is observed
	 * by a number of threads. Use an AtomicReference and an appropriate
	 * accumulator.
	 */
	public static void ex1() throws InterruptedException {
		Object[] array = Stream.generate(() -> UUID.randomUUID().toString()).limit(10000).toArray();
		AtomicReference<Long> reference = new AtomicReference<>(0L);
		ExecutorService pool = Executors.newCachedThreadPool();
		for (int i = 0; i < 100; i++) {
			int f = i;
			pool.submit(() -> {
				for (int j = 0; j < 100; j++) {
					long count = array[f*100+j].toString().chars().filter(Character::isAlphabetic).count();
					reference.accumulateAndGet(count, Long::max);
				}
				
			});

		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
		System.out.println(reference.get());
	}

	/*
	 * Write a program that asks the user for a URL, then reads the web page at
	 * that URL, and then displays all the links. Use a CompletableFuture for
	 * each stage. Don�t call get. To prevent your program from terminating
	 * prematurely, call ForkJoinPool.commonPool().awaitQuiescence(10,
	 * TimeUnit.SECONDS);
	 */
	public static void ex10() {
		String url = "http://www.google.com";
		CompletableFuture.supplyAsync(() -> readPage(url)).thenApply(Chapter6::getLinks)
				.thenAccept(l -> l.forEach(System.out::println));
		ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);

	}

	/*
	 * Generate 1,000 threads, each of which increments a counter 100,000 times.
	 * Compare the performance of using AtomicLong versus LongAdder.
	 * 
	 * LongAdder tend to be faster in my computer
	 */
	public static void ex3() throws InterruptedException {
		Instant now;

		now = Instant.now();
		LongAdder b = new LongAdder();
		ExecutorService pool;
		pool = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			pool.submit(() -> {
				for (int j = 0; j < 100000; j++) {
					b.increment();
				}
			});

		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
		System.out.println(b.sum() + " time " + Duration.between(now, Instant.now()));

		now = Instant.now();
		AtomicLong a = new AtomicLong();
		pool = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			pool.submit(() -> {
				for (int j = 0; j < 100000; j++) {
					a.incrementAndGet();
				}
			});

		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
		System.out.println(a.get() + " time " + Duration.between(now, Instant.now()));

	}

	/*
	 * Write an application in which multiple threads read all words from a
	 * collection of files. Use a ConcurrentHashMap<String, Set<File>> to track
	 * in which files each word occurs. Use the merge method to update the map.
	 */
	public static void ex5() throws InterruptedException {

		ConcurrentHashMap<String, Set<File>> concurrentHashMap = new ConcurrentHashMap<>();
		ExecutorService pool;
		pool = Executors.newCachedThreadPool();
		Stream.of(toFile("alice.txt"), toFile("warAndPeace.txt")).forEach(u -> pool.submit(() -> {
			try {
				Stream<String> wordsAsList = getWords(u.toURI());
				wordsAsList.forEach(w -> {
					Set<File> value = ConcurrentHashMap.newKeySet();
					value.add(u);
					concurrentHashMap.merge(w, value, (a, b) -> {
						a.addAll(b);
						return a;
					});
				});

			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}));
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
		concurrentHashMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEach(u -> System.out.println("word=" + u.getKey() + " files=" + u.getValue()));
	}


	/*
	 * Repeat the preceding exercise, but use computeIfAbsent instead. What is
	 * the advantage of this approach?
	 * 
	 * Implementamtion is more straightforward using computeIfAbsent
	 */
	public static void ex6() throws InterruptedException {

		ConcurrentHashMap<String, Set<File>> concurrentHashMap = new ConcurrentHashMap<>();
		ExecutorService pool;
		pool = Executors.newCachedThreadPool();
		Stream.of(toFile("alice.txt"), toFile("warAndPeace.txt")).forEach(u -> pool.submit(() -> {
			try {
				getWords(u.toURI())
						.forEach(w -> concurrentHashMap.computeIfAbsent(w, t -> ConcurrentHashMap.newKeySet()).add(u));
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}));
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
		concurrentHashMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.forEach(u -> System.out.println("word=" + u.getKey() + " files=" + u.getValue()));
	}

	/*
	 * In a ConcurrentHashMap<String, Long>, find the key with maximum value
	 * (breaking ties arbitrarily). Hint: reduceEntries.
	 */
	public static void ex7() throws IOException {
		Map<String, Long> collect = getWords(toFile("alice.txt").toURI()).parallel()
				.collect(
				Collectors.groupingBy(w -> w, Collectors.counting()));

		Entry<String, Long> entries = new ConcurrentHashMap<>(collect).reduceEntries(4, (t, u) -> t.getValue() > u.getValue() ? t : u);
		System.out.println(" The word \"" + entries.getKey() + "\" appeared " + entries.getValue() + " times");

	}

	/*
	 * How large does an array have to be for Arrays.parallelSort to be faster
	 * than Arrays.sort on your computer?
	 * 
	 * In my computer, parallel sorting started to be faster when the size of
	 * the array reached 1.000.000 items;
	 */

	public static void ex8() {
		Random random = new Random();
		for (double i = 0; i < 7; i++) {
			long size = (long) Math.pow(10, i + 1);
			int[] array = random.ints(size).toArray();
			Instant now = Instant.now();
			Arrays.sort(array);
			long untilSort = now.until(Instant.now(), ChronoUnit.MILLIS);
			System.out.println("sort size " + size + " " + untilSort + " ms");

			int[] array2 = random.ints(size).toArray();
			now = Instant.now();
			Arrays.parallelSort(array2);
			long untilParallel = now.until(Instant.now(), ChronoUnit.MILLIS);
			System.out.println("parallelSort size " + size + " " + untilParallel + " ms");

			System.out.println(untilParallel > untilSort ? "Sequential Sort won" : "Parallel Sort won");

		}

	}

	/*
	 * You can use the parallelPrefix method to parallelize the computation of
	 * Fibonacci numbers. We use the fact that the nth Fibonacci number is the
	 * top left coefficient of Fn, where F = ( 1 1 1 0 ) . Make an array filled
	 * with 2 � 2 matrices. Define a Matrix class with a multiplication method,
	 * use parallelSetAll to make an array of matrices, and use parallelPrefix
	 * to multiply them.
	 */
	public static void ex9() {
		Matrix[] a = new Matrix[8];

		Arrays.parallelSetAll(a, i -> new Matrix());
		Arrays.parallelPrefix(a, (t, u) -> t.multiply(u));

		System.out.println(a[a.length - 1].mat[0][0]);
	}

	public static List<String> getLinks(String content) {
		List<String> links = new ArrayList<>();
		Pattern p = Pattern.compile("(?i)href=\"http://(.*?)\"");
		Matcher m = p.matcher(content);
		while (m.find()) {
			links.add(m.group(1));
		}
		return links;
	}

	private static Stream<String> getWords(URI txtFile) throws IOException {
		return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).parallel()
				.flatMap(m -> Stream.of(m.split("[\\P{L}]+")))
				.filter(s -> !s.isEmpty());
	}



	public static void main(String[] args) throws IOException {
		ex7();
	}

	public static String readPage(String urlString) {
		URL url;
		try {
			url = new URL(urlString);
			URLConnection conn = url.openConnection();
			StringBuilder content = new StringBuilder();
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				String inputLine;
				while ((inputLine = br.readLine()) != null) {
					content.append(inputLine);
				}
			}
			return content.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}
