package ex.j8;

import java.io.File;
import java.io.IOException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.StringSigaUtils;
import utils.ex.HasLogging;
import utils.ex.SupplierEx;

public final class Chapter6 {
    private static final String ALICE_TXT = "alice.txt";

    private static final Logger LOGGER = HasLogging.log();

    /**
     * How large does an array have to be for Arrays.parallelSort to be faster than
     * Arrays.sort on your computer?
     * 
     * In my computer, parallel sorting started to be faster when the size of the
     * array reached 1.000.000 items;
     */
    private static final Random RANDOM = new Random();

    private Chapter6() {
    }

    /**
     * Write a program that keeps track of the longest string that is observed by a
     * number of threads. Use an AtomicReference and an appropriate accumulator.
     */
    public static void ex1() throws InterruptedException {
        final int maxSize = 10000;
        Object[] array = Stream.generate(() -> UUID.randomUUID().toString()).limit(maxSize).toArray();
        AtomicReference<Long> reference = new AtomicReference<>(0L);
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            int f = i;
            pool.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    long count = array[f * 100 + j].toString().chars().filter(Character::isAlphabetic).count();
                    reference.accumulateAndGet(count, Long::max);
                }

            });

        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);
        LOGGER.trace("{}", reference.get());
    }

    /**
     * Write a program that asks the user for a URL, then reads the web page at that
     * URL, and then displays all the links. Use a CompletableFuture for each stage.
     * Don't call get. To prevent your program from terminating prematurely, call
     * ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);
     */
    public static void ex10() {
        ExtractUtils.insertProxyConfig();
        String url = "http://www.google.com";
        CompletableFuture.supplyAsync(() -> readPage(url)).thenApply(StringSigaUtils::getLinks)
            .thenAccept(l -> l.forEach(LOGGER::info));
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);

    }

    /**
     * Generate 1,000 threads, each of which increments a counter 100,000 times.
     * Compare the performance of using AtomicLong versus LongAdder.
     * 
     * LongAdder tend to be faster in my computer
     */
    public static void ex3() throws InterruptedException {
        Instant now = Instant.now();
        LongAdder b = new LongAdder();
        ExecutorService pool;
        pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 100_000; j++) {
                    b.increment();
                }
            });

        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);
        LOGGER.trace("{} time {}", b.sum(), Duration.between(now, Instant.now()));

        now = Instant.now();
        AtomicLong a = new AtomicLong();
        pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 1000; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 100_000; j++) {
                    a.incrementAndGet();
                }
            });

        }
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.HOURS);
        LOGGER.trace("{} time {}", a.get(), Duration.between(now, Instant.now()));

    }

    /**
     * Write an application in which multiple threads read all words from a
     * collection of files. Use a ConcurrentHashMap<String, Set<File>> to track in
     * which files each word occurs. Use the merge method to update the map.
     */
    public static void ex5() throws InterruptedException {

        ConcurrentHashMap<String, Set<File>> concurrentHashMap = new ConcurrentHashMap<>();
        ExecutorService pool;
        pool = Executors.newCachedThreadPool();
        Stream.of(ResourceFXUtils.toFile(ALICE_TXT), ResourceFXUtils.toFile("warAndPeace.txt"))
            .forEach(u -> pool.submit(() -> {
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
            .forEach(u -> LOGGER.trace("{}", "word=" + u.getKey() + " files=" + u.getValue()));
    }

    /**
     * Repeat the preceding exercise, but use computeIfAbsent instead. What is the
     * advantage of this approach?
     * 
     * Implementamtion is more straightforward using computeIfAbsent
     */
    public static void ex6() throws InterruptedException {

        ConcurrentHashMap<String, Set<File>> concurrentHashMap = new ConcurrentHashMap<>();
        ExecutorService pool;
        pool = Executors.newCachedThreadPool();
        Stream.of(ResourceFXUtils.toFile(ALICE_TXT), ResourceFXUtils.toFile("warAndPeace.txt"))
            .forEach(u -> pool.submit(() -> {
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
            .forEach(u -> LOGGER.trace("{}", "word=" + u.getKey() + " files=" + u.getValue()));
    }

    /**
     * In a ConcurrentHashMap<String, Long>, find the key with maximum value
     * (breaking ties arbitrarily). Hint: reduceEntries.
     */
    public static void ex7() throws IOException {
        Map<String, Long> wordCount = getWords(ResourceFXUtils.toURI(ALICE_TXT)).parallel()
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));

        Entry<String, Long> entries = new ConcurrentHashMap<>(wordCount).reduceEntries(4,
            (t, u) -> t.getValue() > u.getValue() ? t : u);
        LOGGER.trace("{}", " The word \"" + entries.getKey() + "\" appeared " + entries.getValue() + " times");

    }

    public static void ex8() {
        for (double i = 0; i < 7; i++) {
            long size = (long) Math.pow(10, i + 1);
            int[] array = RANDOM.ints(size).toArray();
            Instant now = Instant.now();
            Arrays.sort(array);
            long untilSort = now.until(Instant.now(), ChronoUnit.MILLIS);
            LOGGER.trace("sort size {} {} ms", size, untilSort);

            int[] array2 = RANDOM.ints(size).toArray();
            now = Instant.now();
            Arrays.parallelSort(array2);
            long untilParallel = now.until(Instant.now(), ChronoUnit.MILLIS);
            String arg = "parallelSort size " + size + " " + untilParallel + " ms";
            LOGGER.trace("{}", arg);

            Object arg2 = untilParallel > untilSort ? "Sequential Sort won" : "Parallel Sort won";
            LOGGER.trace("{}", arg2);

        }

    }

    /**
     * You can use the parallelPrefix method to parallelize the computation of
     * Fibonacci numbers. We use the fact that the nth Fibonacci number is the top
     * left coefficient of Fn, where F = ( 1 1 1 0 ) . Make an array filled with 2 x
     * 2 matrices. Define a Matrix class with a multiplication method, use
     * parallelSetAll to make an array of matrices, and use parallelPrefix to
     * multiply them.
     */
    public static void ex9() {
        Matrix[] a = new Matrix[8];

        Arrays.parallelSetAll(a, i -> new Matrix());
        Arrays.parallelPrefix(a, (t, u) -> t.multiply(u));

        LOGGER.trace("{}", a[a.length - 1].mat[0][0]);
    }

    public static void main(String[] args) {
        try {
            ex10();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static Stream<String> getWords(URI txtFile) throws IOException {
        return Files.lines(Paths.get(txtFile), StandardCharsets.UTF_8).parallel()
            .flatMap((String m) -> Stream.of(m.split("[\\P{L}]+"))).filter(s -> !s.isEmpty());
    }

    private static String readPage(String urlString) {
        return SupplierEx.remap(() -> {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            return IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
        }, "ERROR Reading Page");
    }

    private static class Matrix {
        private int[][] mat = { { 1, 1 }, { 1, 0 } };

        public Matrix() {
        }

        private Matrix(int[][] mat) {
            this.mat = mat;
        }

        @Override
        public String toString() {
            return Arrays.toString(mat[0]) + "\n" + Arrays.toString(mat[1]) + "\n";
        }

        private Matrix multiply(Matrix other) {
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
    }

}
