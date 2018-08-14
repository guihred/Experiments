package java9.exercise;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import org.slf4j.Logger;

import simplebuilder.HasLogging;

public class Ch3 {
	/*
	 * 1. Provide an interface Measurable with a method double getMeasure() that
	 * measures an object in some way. Make Employee implement Measurable . Provide
	 * a method double average(Measurable[] objects) that computes the average
	 * measure. Use it to compute the average salary of an array of employees.
	 */
	static interface Measurable {
		double getMeasure();
	}

	public static class Employee implements Measurable {
		private static int i = 0;
        private static final List<String> NAMES = Arrays.asList("Michael", "Charlie", "Jonas", "Margareth", "Juliet",
                "Frank",
				"Harry");

		private String name;
		private double salary;

		public Employee(double salary) {
			this.salary = salary;
			name = NAMES.get(i++ % NAMES.size());
		}

		public Employee(String name, double salary) {
			this.name = name;
			this.salary = salary;
		}
		@Override
		public double getMeasure() {
			return getSalary();
		}
		public double getSalary() {
			return salary;
		}
		public void setSalary(double salary) {
			this.salary = salary;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name + " " + salary + "  ";
		}
	}

	static double average(List<? extends Measurable> objects) {
		return objects.stream().mapToDouble(Measurable::getMeasure).average().orElse(0);
	}



	/*
	 * 2. Continue with the preceding exercise and provide a method Measurable
	 * largest(Measurable[] objects) . Use it to find the name of the employee with
	 * the largest salary. Why do you need a cast?
	 */
	static <T extends Measurable> T largest(List<T> objects) {
		return objects.stream().max(Comparator.comparing(Measurable::getMeasure)).orElse(null);
	}

	/*
	 * 4. Implement a static of method of the IntSequence class that yields a
	 * sequence with the arguments. For example, IntSequence.of(3, 1, 4, 1, 5, 9)
	 * yields a sequence with six values. Extra credit if you return an instance of
	 * an anonymous inner class.
	 */
	/*
	 * 5. Add a static method with the name constant of the IntSequence class that
	 * yields an infinite constant sequence. For example, IntSequence.constant(1)
	 * yields values 1 1 1... , ad infinitum. Extra credit if you do this with a
	 * lambda expression.
	 */
	@FunctionalInterface
	public interface IntSequence {
		default boolean hasNext() {
			return true;
		}
		// By default, sequences are infinite
		int next();

		public static IntSequence constant(int seq) {
			return () -> seq;
		}

		default void foreach(IntConsumer e) {
			while (hasNext()) {
				e.accept(next());
			}
		}

		public static IntSequence of(int... seq) {
			return new IntSequence() {
				private int i = 0;

				@Override
				public int next() {
					return seq[i++];
				}

				@Override
				public boolean hasNext() {
					return i < seq.length;
				}
			};
		}
	}

	@FunctionalInterface
	public interface Sequence<T> {
		default boolean hasNext() {
			return true;
		}

		// By default, sequences are infinite
		T next();

		public static <T> Sequence<T> constant(T seq) {
			return () -> seq;
		}

		default void foreach(Consumer<T> e) {
			while (hasNext()) {
				e.accept(next());
			}
		}

		@SafeVarargs
		public static <T> Sequence<T> of(T... seq) {
			return new Sequence<T>() {
				private int i = 0;

				@Override
				public T next() {
					return seq[i++];
				}

				@Override
				public boolean hasNext() {
					return i < seq.length;
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
		private BigInteger i = BigInteger.ZERO;

		@Override
		public BigInteger next() {
			i = i.add(BigInteger.ONE);
			return i.multiply(i);
		}
	}

	/*
	 * 8. Implement the method void luckySort(ArrayList<String> strings,
	 * Comparator<String> comp) that keeps calling Collections.shuffle on the array
	 * list until the elements are in increasing order, as determined by the
	 * comparator.
	 */
	public static void luckySort(List<String> strings, Comparator<String> comp) {
		while (!isSorted(strings, comp)) {
			Collections.shuffle(strings);
		}

	}

	public static <T> boolean isSorted(List<T> a, Comparator<T> comp) {
		for (int i = 0; i < a.size() - 1; i++) {
			if (comp.compare(a.get(i), a.get(i + 1)) > 0) {
				return false;
			}
		}
		return true;
	}

	/*
	 * 10. Implement methods Click here to view code image public static void
	 * runTogether(Runnable... tasks) public static void runInOrder(Runnable...
	 * tasks) The first method should run each task in a separate thread and then
	 * return. The second method should run all methods in the current thread and
	 * return when the last one has completed.
	 */
	public static void runTogether(Runnable... tasks) {
		List<Thread> collect = Stream.of(tasks).parallel().map(Thread::new).collect(toList());
		collect.forEach(Thread::start);

		while (collect.stream().anyMatch(Thread::isAlive)) {
			// DOES NOTHING
		}
	}

	public static void runInOrder(Runnable... tasks) {
		for (Runnable runnable : tasks) {
			runnable.run();
		}

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
	 * 12. Using the list(FilenameFilter) method of the java.io.File class, write a
	 * method that returns all files in a given directory with a given extension.
	 * Use a lambda expression, not a FilenameFilter . Which variable from the
	 * enclosing scope does it capture?
	 */

	public static List<String> listByExtension(File file, String extension) {
		String[] listFiles = file.list((f, name) -> name.endsWith(extension));
		return Arrays.asList(listFiles);

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
	 * 14. Write a method that takes an array of Runnable instances and returns a
	 * Runnable whose run method executes them in order. Return a lambda expression.
	 */
	public static Runnable inOrder(Runnable... runnables) {
		return () -> runInOrder(runnables);
	}

	/*
	 * 15. Write a call to Arrays.sort that sorts employees by salary, breaking ties
	 * by name. Use Comparator.thenComparing . Then do this in reverse order.
	 * 
	 * 16. Implement the RandomSequence in Section 3.9.1 , “Local Classes ” (page
	 * 129) as a nested class, outside the randomInts method.
	 */
	public static void main(String[] args) {
		Random random = new Random();
		List<Employee> collect = random.ints(1, 11).map(e -> ++e * 500).limit(5).mapToObj(Employee::new)
				.collect(toList());

		log.info("{}",average(collect));
		log.info("{}",largest(collect));
        // IntSequence.of(1, 2, 3).foreach(e -> log.info("{}",e))
        // IntSequence.constant(1).foreach(e -> log.info("{}",e))
        // new SquareSequence().foreach(e -> log.info("{}",e))
        // log.info("{}",isSorted(Arrays.asList(1, 2, 2, 3), Integer::compareTo))
		//
        // List<String> asList = Arrays.asList("f", "f", "f", "f", "f", "g", "d", "e", "e")
        // log.info("{}",asList)
        // luckySort(asList, String::compareTo)
        // log.info("{}",asList)
        // log.info("{}",subdirectories(new File(".")))
		log.info("{}",sortFiles(new File(".").listFiles()));
	}

    static Logger log = HasLogging.log();
	public static void tasks() {
		Runnable[] tasks= new Runnable[] {
				()->log.info("{}","1"),
				()->log.info("{}","2"),
				()->log.info("{}","3"),
				()->log.info("{}","4"),
				()->log.info("{}","5"),
				()->log.info("{}","6"),
				()->log.info("{}","7"),
				()->log.info("{}","8"),
				()->log.info("{}","9"),
				()->log.info("{}","10"),
				()->log.info("{}","11"),
				
		};
		log.info("{}","In Order");
		runInOrder(tasks);
		log.info("{}","Together");
		runTogether(tasks);
	}
}
