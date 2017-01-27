package java8.exercise;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

public class Chapter8 {
	/*
	 * Write a program that adds, subtracts, divides, and compares numbers
	 * between 0 and 232 � 1, using int values and unsigned operations. Show why
	 * divideUnsigned and remainderUnsigned are necessary.
	 */
	public static void ex1() {
		System.out.print("Ordering signed: ");
		Stream.of(1, 2, 4, -1).sorted().map(Integer::toUnsignedString).forEach(i -> System.out.print(i + " "));
		System.out.print("\nOrdering unsigned: ");
		Stream.of(1, 2, 4, -1).sorted(Integer::compareUnsigned).map(Integer::toUnsignedString).forEach(i -> System.out.print(i + " "));

		System.out.println("\nSum of MAX_VALUE and MIN_VALUE signed:   " + (Integer.MAX_VALUE + Integer.MIN_VALUE));
		System.out.println("Sum of MAX_VALUE and MIN_VALUE unsigned: " + Integer.toUnsignedString(Integer.MAX_VALUE + Integer.MIN_VALUE));
		System.out.println("Sutraction of 1 and MIN_VALUE signed:	" + (1 - Integer.MIN_VALUE));
		System.out.println("Sutraction of 1 and MIN_VALUE unsigned:	" + Integer.toUnsignedString(1 - Integer.MIN_VALUE));

		System.out.println("Multiplication of 2 and MAX_VALUE signed:	" + 2 * Integer.MAX_VALUE);
		System.out.println("Multiplication of 2 and MAX_VALUE unsigned:	" + Integer.toUnsignedString(2 * Integer.MAX_VALUE));

		System.out.println("Division of 2 and 4294967295 signed:	" + -1 / 2);
		System.out.println("Division of 2 and 4294967295 unsigned:	" + Integer.toUnsignedString(Integer.divideUnsigned(-1, 2)));

		System.out.println("Remainder of 4294967295 and 2 signed:	" + -1 % 2);
		System.out.println("Remainder of 4294967295 and 2 unsigned:	" + Integer.toUnsignedString(Integer.remainderUnsigned(-1, 2)));

	}

	/*
	 * For which integer n does Math.negateExact(n) throw an exception? (Hint:
	 * There is only one.)
	 * 
	 * Answer: it throws the exception when the argument is
	 * MIN_VALUE(-2147483648 signed, 2147483648 unsigned) because it doesn't
	 * have a negative counterpart
	 */
	public static void ex2() {
		Math.negateExact(Integer.MIN_VALUE);
	}

	/*
	 * Euclid�s algorithm (which is over two thousand years old) computes the
	 * greatest common divisor of two numbers as gcd(a, b) = a if b is zero, and
	 * gcd(b, rem(a, b)) otherwise, where rem is the remainder. Clearly, the gcd
	 * should not be negative, even if a or b are (since its negation would then
	 * be a greater divisor). Implement the algorithm with %, floorMod, and a
	 * rem function that produces the mathematical (non-negative) remainder.
	 * Which of the three gives you the least hassle with negative values?
	 */
	public static void ex3() {
		System.out.println("gcd1 " + gcd1(20, 40));
		System.out.println("gcd2 " + gcd2(20, 40));
		System.out.println("gcd3 " + gcd3(20, 40));
		System.out.println("gcd1 " + gcd1(-20, 40));
		System.out.println("gcd2 " + gcd2(-20, 40));
		System.out.println("gcd3 " + gcd3(-20, 40));
		System.out.println("gcd1 " + gcd1(20, -40));
		System.out.println("gcd2 " + gcd2(20, -40));
		System.out.println("gcd3 " + gcd3(20, -40));
		System.out.println("gcd1 " + gcd1(-20, -40));
		System.out.println("gcd2 " + gcd2(-20, -40));
		System.out.println("gcd3 " + gcd3(-20, -40));
	}

	/*
	 * The Math.nextDown(x) method returns the next smaller floating-point
	 * number than x, just in case some random process hit x exactly, and we
	 * promised a number < x. Can this really happen? Consider double r = 1 -
	 * generator. nextDouble(), where generator is an instance of
	 * java.util.Random. Can it ever yield 1? That is, can
	 * generator.nextDouble() ever yield 0? The documentation says it can yield
	 * any value between 0 inclusive and 1 exclusive. But, given that there are
	 * 253 such floating-point numbers, will you ever get a zero? Indeed, you
	 * do. The random number generator computes the next seed as next(s) = s � m
	 * + a % N, where m = 25214903917, a = 11, and N = 248. The inverse of m
	 * modulo N is v = 246154705703781, and therefore you can compute the
	 * predecessor of a seed as prev(s) = (s � a) � v % N. To make a double, two
	 * random numbers are generated, and the top 26 and 27 bits are taken each
	 * time. When s is 0, next(s) is 11, so that�s what we want to hit: two
	 * consecutive numbers whose top bits are zero. Now, working backwards,
	 * let�s start with s = prev(prev(prev(0))). Since the Random constructor
	 * sets s = (initialSeed ^ m) % N, offer it s = prev(prev(prev(0))) ^ m =
	 * 164311266871034, and you�ll get a zero after two calls to nextDouble. But
	 * that is still too obvious. Generate a million predecessors, using a
	 * stream of course, and pick the minimum seed. Hint: You will get a zero
	 * after 376050 calls to nextDouble.
	 */
	public static void ex4() {
		// Didn't get it
	}

	/*
	 * At the beginning of Chapter 2, we counted long words in a list as
	 * words.stream().filter(w -> w.length() > 12).count(). Do the same with a
	 * lambda expression, but without using streams. Which operation is faster
	 * for a long list?
	 */
	public static void ex5() throws IOException {

		List<String> wordsAsList = getWordsAsList(Paths.get("alice.txt"));

		Instant now = Instant.now();
		long size = wordsAsList.stream().filter(w -> w.length() <= 12).count();
		Instant end = Instant.now();
		System.out.println("size=" + size + " time " + Duration.between(now, end).toMillis() + " ms");

		wordsAsList = getWordsAsList(Paths.get("alice.txt"));
		now = Instant.now();
		wordsAsList.removeIf(w -> w.length() > 12);
		size = wordsAsList.size();
		end = Instant.now();

		System.out.println("size=" + size + " time " + Duration.between(now, end).toMillis() + " ms");

	}

	/*
	 * Using only methods of the Comparator class, define a comparator for
	 * Point2D which is a total ordering (that is, the comparator only returns
	 * zero for equal objects). Hint: First compare the x-coordinates, then the
	 * y-coordinates. Do the same for Rectangle2D.
	 */
	public static void ex6() {
		Random random = new Random();
		Stream.generate(() -> new Point2D(random.nextInt(30), random.nextInt(30))).limit(20)
				.sorted(comparing(Point2D::getX).thenComparing(Point2D::getY)).forEach(System.out::println);

		Stream.generate(() -> new Rectangle2D(random.nextInt(30), random.nextInt(30), random.nextInt(30), random.nextInt(30)))
				.limit(20)
				.sorted(comparing(Rectangle2D::getMinX).thenComparing(Rectangle2D::getMinY).thenComparing(Rectangle2D::getWidth)
						.thenComparing(Rectangle2D::getHeight)).forEach(System.out::println);

	}

	/*
	 * Express nullsFirst(naturalOrder()).reversed() without calling reversed.
	 */
	public static void ex7() {
		Random random = new Random();

		Stream<Integer> iterate = Stream.iterate(0, (Integer i) -> i == null ? random.nextInt(30) : null).limit(10);
		System.out.println("nullsFirst(naturalOrder()).reversed() ="
				+ iterate.sorted(nullsFirst(Comparator.<Integer> naturalOrder()).reversed()).collect(Collectors.toList()));
		iterate = Stream.iterate(0, (Integer i) -> i == null ? random.nextInt(30) : null).limit(10);
		System.out.println("nullsLast(reverseOrder()) ="
				+ iterate.sorted(nullsLast(Comparator.<Integer> reverseOrder())).collect(Collectors.toList()));
	}

	/*
	 * Write methods that turn a Scanner into a stream of words, lines,
	 * integers, or double values. Hint: Look at the source code for
	 * BufferedReader.lines.
	 */
	public static void ex9() throws FileNotFoundException {
		streamOfLines(new Scanner(new File("alice.txt"))).forEach(System.out::println);
		streamOfWords(new Scanner(new File("alice.txt"))).forEach(System.out::println);
		streamOfInteger(new Scanner(new File("alice.txt"))).forEach(System.out::println);
		streamOfDouble(new Scanner(new File("alice.txt"))).forEach(System.out::println);
	}

	public static int gcd1(int a, int b) {
		if (b == 0) {
			return a;
		}
		return gcd1(b, a % b);
	}

	public static int gcd2(int a, int b) {
		if (b == 0) {
			return a;
		}
		return gcd1(b, Math.floorMod(a, b));
	}

	public static int gcd3(int a, int b) {
		if (b == 0) {
			return a;
		}
		return gcd1(b, Integer.remainderUnsigned(a, b));
	}

	private static List<String> getWordsAsList(Path path) throws IOException {

		String contents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		return new ArrayList<>(Arrays.asList(contents.split("[\\P{L}]+")));
	}

	public static void main(String[] args) throws Exception {
		ex15();
	}

	private static Stream<String> streamOfLines(Scanner scanner) {

		Iterator<String> iter = new Iterator<String>() {

			@Override
			public boolean hasNext() {
				return scanner.hasNextLine();
			}

			@Override
			public String next() {
				if (hasNext()) {
					return scanner.nextLine();
				}
				throw new NoSuchElementException();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
	}

	private static Stream<Integer> streamOfInteger(Scanner scanner) {

		Iterator<Integer> iter = new Iterator<Integer>() {
			@Override
			public boolean hasNext() {
				return scanner.hasNextInt();
			}

			@Override
			public Integer next() {
				if (hasNext()) {
					return scanner.nextInt();
				}
				throw new NoSuchElementException();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
	}

	private static Stream<Double> streamOfDouble(Scanner scanner) {

		Iterator<Double> iter = new Iterator<Double>() {
			@Override
			public boolean hasNext() {
				return scanner.hasNextDouble();
			}

			@Override
			public Double next() {
				if (hasNext()) {
					return scanner.nextDouble();
				}
				throw new NoSuchElementException();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
	}

	private static Stream<String> streamOfWords(Scanner scanner) {

		Iterator<String> iter = new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return scanner.hasNextLine();
			}

			@Override
			public String next() {
				if (hasNext()) {
					return scanner.nextLine();
				}
				throw new NoSuchElementException();
			}
		};
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED | Spliterator.NONNULL), false).flatMap(
				(s) -> Stream.of(s.split("[\\P{L}]+")));
	}

	/*
	 * Unzip the src.zip file from the JDK. Using Files.walk, find all Java
	 * files that contain the keywords transient and volatile.
	 */
	public static void ex10() throws IOException {
		File original = new File("src2");
		Files.walk(original.toPath(), 20).map(Path::toFile).filter(( file) -> {
			try {
				if (file.canRead() && file.isFile()) {
					List<String> wordsAsList = getWordsAsList(file.toPath());
					return wordsAsList.contains("transient") && wordsAsList.contains("volatile");
				}

			} catch (Exception e) {
				System.out.println();
			}
			return true;
		}).filter(File::isFile).forEach(System.out::println);

	}

	/*
	 * Write a program that gets the contents of a password-protected web page.
	 * Call URLConnection connection = url.openConnection();. Form the string
	 * username: password and encode it in Base64. Then call
	 * connection.setRequestProperty( "Authorization", "Basic " + encoded
	 * string), followed by connection.connect() and
	 * connection.getInputStream().
	 */
	public static void ex11() throws IOException {
		URL url = new URL("https://www.quora.com/");
		URLConnection connection = url.openConnection();
		String str = "username:password";
		String encode = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
		System.out.println("Basic " + encode);
		connection.setRequestProperty("Authorization", "Basic " + encode);
		connection.connect();
		InputStream inputStream = connection.getInputStream();
		BufferedReader a = new BufferedReader(new InputStreamReader(inputStream));
		a.lines().forEach(System.out::print);

	}

	/*
	 * Demonstrate the use of the Objects.requireNonNull method and show how it
	 * leads to more useful error messages.
	 */
	public static void ex14() {
		Objects.requireNonNull(null, () -> "What are you doing man??");

	}

	/*
	 * Using Files.lines and Pattern.asPredicate, write a program that acts like
	 * the grep utility, printing all lines that contain a match for a regular
	 * expression.
	 */
	public static void ex15() throws IOException {
		// Lines that contain some number
		Files.lines(new File("alice.txt").toPath()).filter(Pattern.compile(".*\\d+.*$").asPredicate()).forEach(System.out::println);

	}

	/*
	 * Use a regular expression with named capturing groups to parse a line
	 * containing a city, state, and zip code. Accept both 5- and 9-digit zip
	 * codes.
	 */
	public static void ex16() throws IOException {
		// Lines that contain some number
		Files.lines(new File("alice.txt").toPath()).filter(Pattern.compile(".*\\d+.*$").asPredicate()).forEach(System.out::println);

	}
}
