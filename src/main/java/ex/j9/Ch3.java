package ex.j9;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import utils.QuickSortML;

public class Ch3 {
    public static double average(Collection<? extends Measurable> objects) {
        return objects.stream().mapToDouble(Measurable::getMeasure).average().orElse(0);
    }

    /*
     * 14. Write a method that takes an array of Runnable instances and returns a
     * Runnable whose run method executes them in order. Return a lambda expression.
     */
    public static Runnable inOrder(Runnable... runnables) {
        return () -> runInOrder(runnables);
    }

    /*
     * 2. Continue with the preceding exercise and provide a method Measurable
     * largest(Measurable[] objects) . Use it to find the name of the employee with
     * the largest salary. Why do you need a cast?
     */
    public static <T extends Measurable> T largest(Collection<T> objects) {
        return objects.stream().max(Comparator.comparing(Measurable::getMeasure)).orElse(null);
    }

    /*
     * 12. Using the list(FilenameFilter) method of the java.io.File class, write a
     * method that returns all files in a given directory with a given extension.
     * Use a lambda expression, not a FilenameFilter. Which variable from the
     * enclosing scope does it capture?
     */
    public static List<String> listByExtension(File file, String extension) {
        String[] listFiles = file.list((f, name) -> name.endsWith(extension));
        return Arrays.asList(listFiles);
    }

    /*
     * 8. Implement the method void luckySort(ArrayList<String> strings,
     * Comparator<String> comp) that keeps calling Collections.shuffle on the array
     * list until the elements are in increasing order, as determined by the
     * comparator.
     */
    public static void luckySort(List<String> strings, Comparator<String> comp) {
        while (!QuickSortML.isSorted(strings, comp)) {
            Collections.shuffle(strings);
        }

    }

    /*
     * 15. Write a call to Arrays.sort that sorts employees by salary, breaking ties
     * by name. Use Comparator.thenComparing . Then do this in reverse order.
     * 
     * 16. Implement the RandomSequence in Section 3.9.1 , “Local Classes ” (page
     * 129) as a nested class, outside the randomInts method.
     */

    public static void runInOrder(Runnable... tasks) {
        for (Runnable runnable : tasks) {
            runnable.run();
        }

    }

    /*
     * 10. Implement methods Click here to view code image public static void
     * runTogether(Runnable... tasks) public static void runInOrder(Runnable...
     * tasks) The first method should run each task in a separate thread and then
     * return. The second method should run all methods in the current thread and
     * return when the last one has completed.
     */
    public static void runTogether(Runnable... tasks) {
        List<Thread> threadList = Stream.of(tasks).parallel().map(Thread::new).collect(toList());
        threadList.forEach(Thread::start);
        while (threadList.stream().anyMatch(Thread::isAlive)) {
            // DOES NOTHING
        }
    }

    /*
     * 13. Given an array of File objects, sort it so that directories come before
     * files, and within each group, elements are sorted by path name. Use a lambda
     * expression to specify the Comparator .
     */
    public static List<File> sortFiles(File[] file) {
        Arrays.sort(file, Comparator.comparing(File::isDirectory).reversed().thenComparing(File::getName));
        return Arrays.asList(file);
    }

    /*
     * 11. Using the listFiles(FileFilter) and isDirectory methods of the
     * java.io.File class, write a method that returns all subdirectories of a given
     * directory. Use a lambda expression instead of a FileFilter object. Repeat
     * with a method expression and an anonymous inner class.
     */
    public static List<File> subdirectories(File file) {
        File[] listFiles = file.listFiles(File::isDirectory);
        return Arrays.asList(listFiles);

    }




    /*
     * 4. Implement a static of method of the IntSequence class that yields a
     * sequence with the arguments. For example, IntSequence.of(3, 1, 4, 1, 5, 9)
     * yields a sequence with six values. Extra credit if you return an instance of
     * an anonymous inner class.
     */

    @FunctionalInterface
    public interface IntSequence {
        default void foreach(IntConsumer e) {
            while (hasNext()) {
                e.accept(next());
            }
        }

        default boolean hasNext() {
            return true;
        }

        // By default, sequences are infinite
        int next();

        /*
         * 5. Add a static method with the name constant of the IntSequence class that
         * yields an infinite constant sequence. For example, IntSequence.constant(1)
         * yields values 1 1 1... , ad infinitum. Extra credit if you do this with a
         * lambda expression.
         */
        static IntSequence constant(int seq) {
            return () -> seq;
        }

        static IntSequence of(int... seq) {
            return new IntSequence() {
                private int i;

                @Override
                public boolean hasNext() {
                    return i < seq.length;
                }

                @Override
                public int next() {
                    return seq[i++];
                }
            };
        }
    }

    @FunctionalInterface
    public interface Sequence<T> {
        default void foreach(Consumer<T> e) {
            while (hasNext()) {
                e.accept(next());
            }
        }

        default boolean hasNext() {
            return true;
        }

        // By default, sequences are infinite
        T next();

        static <T> Sequence<T> constant(T seq) {
            return () -> seq;
        }

        @SafeVarargs
        static <T> Sequence<T> of(T... seq) {
            return new Sequence<T>() {
                private int i;

                @Override
                public boolean hasNext() {
                    return i < seq.length;
                }

                @Override
                public T next() {
                    return seq[i++];
                }
            };
        }
    }

    /*
     * 6. The SquareSequence class doesn't actually deliver an infinite sequence of
     * squares due to integer overflow. Specifically, how does it behave? Fix the
     * problem by defining a Sequence<T> interface and a SquareSequence class that
     * implements Sequence<BigInteger> .
     */
    public static class SquareSequence implements Sequence<BigInteger> {
        private static final int EXAMPLE_LIMIT = 50;
        private BigInteger i = BigInteger.ZERO;
        private int c;
        private int limit = EXAMPLE_LIMIT;

        @Override
        public boolean hasNext() {
            return c < limit;
        }

        public SquareSequence limit(int limit1) {
            limit = limit1;
            return this;
        }

        @Override
        public BigInteger next() {
            c++;
            i = i.add(BigInteger.ONE);
            return i.multiply(i);
        }
    }

    /*
     * 1. Provide an interface Measurable with a method double getMeasure() that
     * measures an object in some way. Make Employee implement Measurable . Provide
     * a method double average(Measurable[] objects) that computes the average
     * measure. Use it to compute the average salary of an array of employees.
     */
    @FunctionalInterface
    interface Measurable {
        double getMeasure();
    }
}
