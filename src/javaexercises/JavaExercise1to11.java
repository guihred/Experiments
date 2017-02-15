package javaexercises;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class JavaExercise1to11 {

	private JavaExercise1to11() {
	}

	public static void main(String[] args) {
		fibonacciSeriesProblem();
		greenflyProblem();
		allPrimeLessThan600Problem();
		easter(2017);
		friday13thProblem();
		forwardBackwardCountProblem();
		accumulatingRoundingErrors();
		sqrtByIteration();
		recurringFractionProblem();
		sortingRecords();
	}

	/**
	 * 11. Sorting Records. The following is an outline Java program which
	 * incorporates some built-in data. The data consist of a array of 8 records
	 * , where each record is a person's name together with the associated age
	 * (in years) and height (in metres). The program should sort the data so
	 * that the records are in ascending order of ages.
	 */
	public static void sortingRecords() {
		Person[] p = { new Person("George", 34, 1.71F), new Person("Betty", 22, 1.76F),
				new Person("Charles", 24, 1.79F), new Person("Hanna", 29, 1.66F), new Person("Edward", 23, 1.82F),
				new Person("Frida", 28, 1.77F), new Person("Davina", 33, 1.69F), new Person("Andrew", 25, 1.67F) };
		sort(p);
	}

	private static void sort(Person[] p) {
		Arrays.sort(p, Comparator.comparing((Person pe) -> pe.age));

		for (Person i : p) {
			System.out.printf("%s%n", i);
		}
	}
	static class Person {
		private String name;
		private int age;
		private float height;

		public Person(String n, int a, float h) {
			name = n;
			age = a;
			height = h;
		}

		@Override
		public String toString() {
			return String.format("%-9s %3d %6.2F", name, age, height);
		}
	}

	/**
	 * 10. The Recurring Fraction Problem. Consider a function f(n) informally
	 * defined thus: f(0) = 2; f (1) = 1 + 1/(2 + 1); f(2) = 1 + 1/(2 + 1/(2 +
	 * 1)); f(3) = 1 + 1/(2 + 1/(2 + 1/(2 + 1))); f(4) = 1 + 1/(2 + 1/(2 + 1/(2
	 * + 1/(2 + 1)))). Note that n is the num ber of vincula in the recurring
	 * fraction. Write a method private static double f(int n) which returns the
	 * appropriate value. Incorporate the function into a complete program which
	 * will write out the values: f(0); f(1); ... f(10)
	 */

	public static void recurringFractionProblem() {
		for (int i = 0; i < 10; i++) {
			System.out.println(f(i));
		}
	}

	private static double f(int n) {
		if (n == 0) {
			return 2;
		}

		return 1 + 1 / (1 + f(n - 1));
	}

	/**
	 * 9. Determining a Square Root by Iteration. If x_i is an approximation to
	 * sqrt(5) then x_i+1 is a better approximation if: x_i+1 = 1/2*(x_i +
	 * 5/x_i). Prove this and write a program which uses type double to
	 * determine sqrt(5) to 10 signicant figures. In writing the program it is
	 * probably sensible to have two variables, x (which is the latest
	 * approximation to sqrt(5)) and oldx (the previous approximation to
	 * sqrt(5)). Each time round some loop (that is at each iteration ) consider
	 * the following condition: (Math.abs(x-oldx)>1.0e-10d) Note that Math.abs()
	 * determines the absolute value of its argument and the condition as a
	 * whole determines whether x and oldx differ by more than 10^-10(which,
	 * when expressed as a double constant in Java, is written as 1.0e-10d where
	 * e stands for 'times ten to the power of'). This is not strictly what the
	 * question requires but will be acceptable.
	 */
	public static void sqrtByIteration() {
		double oldx = 1;

		while (true) {
			double x = (oldx + 5 / oldx) / 2;
			if (Math.abs(x - oldx) < 1.0e-10D) {
				break;
			}
			oldx = x;
		}
		System.out.println(oldx);
		System.out.println(Math.sqrt(5));
	}

	/**
	 * 8. Accumulating Rounding Errors. Write a program which evaluates 2^n/100
	 * for each n = 1,2,... 16. Each value should be determined in two different
	 * ways. First evaluate (float)numerator/100.0F where numerator = 2^n; this
	 * gives a good result. The second way is very naïve: simply add
	 * (float)1/(float)100 to itself 2^n times!
	 */

	public static void accumulatingRoundingErrors() {
		float sum = 0;

		for (int i = 1; i <= 16; i++) {
			sum += (2 << i) / 100.0;
		}
		System.out.println(sum);
		sum = 0;

		for (int i = 1; i <= 16; i++) {
			for (int j = 0; j < 2 << i; j++) {
				sum += 1.0 / 100.0;
			}
		}
		System.out.println(sum);
	}

	/**
	 * 7. The Forward and Backward Count Problem. Write a program to sum the
	 * series sum infinite of 1 / (1000 * n + PI) and print the results. The
	 * program should compute successive terms starting at n = 0 and continue
	 * adding terms until the sum ceases to increase. Write out the number of
	 * terms computed and the sum of those terms. The program should then
	 * recompute the answer by summing the same terms but in reverse order. This
	 * new sum should also be written out.
	 */
	public static void forwardBackwardCountProblem() {
		double n = 0;
		double sum = 0;

		while (true) {
			double item = 1 / (1000 * n + Math.PI);
			sum += item;
			n++;
			if (item < 1.0e-10D) {
				break;
			}
		}
		System.out.println(n);
		System.out.println(sum);
		sum = 0;
		while (true) {
			double item = 1 / (1000 * n + Math.PI);
			sum += item;
			n--;
			if (n < 0) {
				break;
			}
		}
		System.out.println(sum);

	}

	/**
	 * 6. The Friday 13th Problem. Write a program to demonstrate that the 13th
	 * of a month is more likely to fall on a Friday than on any other day.
	 */
	public static void friday13thProblem() {
		LocalDate begin = LocalDate.of(1900, 1, 1);
		Map<DayOfWeek, Long> histogram = Stream.iterate(begin, d -> d.plusDays(1)).filter(d -> d.getDayOfMonth() == 13).limit(4800)
				.collect(Collectors.groupingBy(LocalDate::getDayOfWeek, Collectors.counting()));
		System.out.println(histogram);
	}

	/**
	 * 5. The Date of Easter Problem. A convenient algorithm for determining the
	 * date of Easter in a given year was devised in 1876 and first appeared in
	 * Butcher's Ecclesiastical Handbook . The algorithm is valid for all years
	 * in the Gregorian calendar. Write a method private static int easter(int
	 * y) which, when presented with a year y, returns the date of Easter in the
	 * form shown at step 12. Incorporate this method into a complete test
	 * program. Verify that the method gives the correct date of Easter for the
	 * current year
	 */
	public static void easter(int y) {
		int a = y % 19;
		int b = y / 100;
		int c = y % 100;
		int h = (19 * a + b - b / 4 - (b - (b + 8) / 25 + 1) / 3 + 15) % 30;
		int l = (32 + 2 * (b % 4) + 2 * (c / 4) - h - c % 4) % 7;
		int m = (a + 11 * h + 22 * l) / 451;
		int n = (h + l - 7 * m + 114) / 31;
		int p = (h + l - 7 * m + 114) % 31;
		System.out.println(String.format("%02d/%02d/%d", p + 1, n, y));
	}

	/**
	 * 3. All Prime Numbers less than 600. Write a program to print a table of
	 * all prime num bers less than 600. Use the sieve method; take the first
	 * 600 integers and cross out all those that are multiples of 2, 3, 5, etc.
	 * until only primes remain, then print out the table. The table must be
	 * organised so that there are ten prime num bers on eac h line.
	 */
	public static void allPrimeLessThan600Problem() {
		final int size = 600, sqrtSize = 25;
		boolean[] primes = Arrays.copyOf(new boolean[0], size);
		Arrays.fill(primes, 2, size, true);
		for (int i = 0; i < primes.length; i++) {
			for (int j = 2; j < sqrtSize; j++) {
				if (i % j == 0 && i != j) {
					primes[i] = false;
				}
			}
		}
		int j = 1;
		for (int i = 0; i < primes.length; i++) {
			if (primes[i]) {
				System.out.print(String.format("%03d ", i));
				if (j++ % 10 == 0) {
					System.out.println();
				}
			}

		}
	}

	/**
	 * 2. The Greenfly Problem. Greenfly can reproduce asexually . After one
	 * week of life a lone female can produce eight offspring a day. Starting at
	 * the beginning of day 1 with a single mature female, how many greenfly
	 * could there be by the end of day 28? It may be assumed that: - There are
	 * no deaths - All offspring are females. Note that at the end of day 1
	 * there will be 9 greenfly (original + 8 offspring ). At the end of day 7
	 * there will be 57 greenfly (original + 8 x 7 offspring). At the end of day
	 * 8 there will be 129 greenfly (original + 8 x 8 offspring + 64 offspring
	 * from the daughters produced on day 1).
	 */
	public static void greenflyProblem() {
		List<Greenfly> of = Arrays.asList(new Greenfly(-7));
		for (int i = 0; i < 28; i++) {
			int j = i;
			of = of.parallelStream().flatMap(g -> g.reproduce(j)).collect(Collectors.toList());
		}
		Greenfly.MAPA
				.forEach((day, count) -> System.out.println("At day " + (day + 1) + " = " + count + " greenflies"));

		System.out.println("Total = " + of.size());
	}

	public static class Greenfly {
		private static int count;
		protected static final Map<Integer, Integer> MAPA = new LinkedHashMap<>();
		private final int birthday;

		public Greenfly(int birthday) {
			this.birthday = birthday;
			if (birthday < 0) {
				++count;
			} else {
				count += 8;
			}
			MAPA.put(birthday, count);
		}

		public Stream<Greenfly> reproduce(int day) {
			if (day - birthday >= 7) {
				Greenfly greenfly = new Greenfly(day);
				return Stream.concat(Stream.of(this), Stream.generate(() -> greenfly).limit(8));
			}
			return Stream.of(this);
		}
	}

	/**
	 * 1. The Fibonacci Series Problem. Find the first term greater than 1000 in
	 * the sequence: 1 1 2 3 5 8 13 ... Also find the sum of all the values up
	 * to that term.
	 */

	public static void fibonacciSeriesProblem() {
		AtomicInteger j = new AtomicInteger(0);
		int firstTerm = IntStream.iterate(1, i -> {
			int k = i + j.get();
			j.set(i);
			return k;
		}).limit(1000).filter(i -> i > 1000).findFirst().orElse(0);
		System.out.println("firstTerm=" + firstTerm);
		j.set(0);
		int sum = IntStream.iterate(1, i -> {
			int k = i + j.get();
			j.set(i);
			return k;
		}).limit(1000).filter(i -> i < 1000).sum();
		System.out.println("sum=" + sum);
	}

}
