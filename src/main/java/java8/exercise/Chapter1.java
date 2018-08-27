package java8.exercise;


import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import simplebuilder.HasLogging;

public final class Chapter1 {


    public static final String DOCUMENTS_FOLDER = new JFileChooser().getFileSystemView().getDefaultDirectory()
            .getAbsolutePath()
    ;
    private static final Logger LOGGER = HasLogging.log(Chapter1.class);

    static interface Collection2<T> extends Collection<T> {

		default void forEachIf(Consumer<? super T> action, Predicate<? super T> p) {
			forEach(t -> {
				if (p.test(t)) {
					action.accept(t);
				}
			});
		}

	}

    static class Collection2Impl<E> extends ArrayList<E> implements Collection2<E> {
		private static final long serialVersionUID = 1L;
	}

	private Chapter1() {
	}

	/***
	 * Write a static method andThen that takes as parameters two Runnable
	 * instances and returns a Runnable that runs the first, then the second. In
	 * the main method, pass two lambda expressions into a call to andThen, and
	 * run the returned instance.
	 */
	public static Runnable andThen(Runnable a, Runnable b) {

		return () -> {
			a.run();
			b.run();
		};

	}

	/**
     * Is the comparator code in the Arrays.sort method called in the same
     * thread as the call to sort or a different thread?
     * 
     * A: in the same thread.
     * @return 
     */
    public static String ex1(Integer[] a) {

        Property<String> property = new SimpleObjectProperty<>();
		Arrays.sort(a, (o1, o2) -> {
            property.setValue(Thread.currentThread().getName());
			return Integer.compare(o1, o2);
		});
        return property.getValue();
	}

	/**
	 * Using the listFiles(FileFilter) and isDirectory methods of the
	 * java.io.File class, write a method that returns all subdirectories of a
	 * given directory. Use a lambda expression instead of a FileFilter object.
	 * Repeat with a method expression.
	 */
	public static void ex2(File directory) {

		Arrays.asList(directory.listFiles(File::isDirectory)).forEach(
                s -> LOGGER.trace("{}", s));
        Arrays.asList(directory.listFiles((File pathname) -> pathname.isDirectory()))
                .forEach(s -> LOGGER.trace("{}", s));

	}

	/**
	 * Using the list(FilenameFilter) method of the java.io.File class, write a
	 * method that returns all files in a given directory with a given
	 * extension. Use a lambda expression, not a FilenameFilter. Which variables
	 * from the enclosing scope does it capture?
	 */
	public static void ex3(File directory, String extension) {
        Arrays.asList(directory.listFiles((FilenameFilter) (dir, name) -> name.endsWith(extension)))
                .forEach(s -> LOGGER.trace("{}", s));

	}

	/**
	 * Given an array of File objects, sort it so that the directories come
	 * before the files, and within each group, elements are sorted by path
	 * name. Use a lambda expression, not a Comparator.
	 */
	public static void ex4(File[] listFiles) {
		List<File> asList = Arrays.asList(listFiles);
		Collections.shuffle(asList);

        String unsorted = asList.stream().map(File::getName).collect(Collectors.joining(", ", "Before: ", ""));
        LOGGER.trace(unsorted);
		asList.sort(Comparator.comparing(File::isFile).thenComparing(File::getName));
        String sorted = asList.stream().map(File::getName).collect(Collectors.joining(", ", "After: ", ""));
        LOGGER.trace(sorted);

	}

	public static void ex5() {
		// Too lazy to make some code and test how many lines I'd have saved
		// :-P.
	}

	/**
     * Didn't you always hate it that you had to deal with checked exceptions in
     * a Runnable? Write a method uncheck that catches all checked exceptions
     * and turns them into unchecked exceptions. For example, new
     * Thread(uncheck( () -> { LOGGER.trace("Zzz"); Thread.sleep(1000);
     * })).start(); // Look, no catch (InterruptedException)! Hint: Define an
     * interface RunnableEx whose run method may throw any exceptions. Then
     * implement public static Runnable uncheck(RunnableEx runner). Use a lambda
     * expression inside the uncheck function. Why can't you just use
     * Callable<Void> instead of RunnableEx?
     * 
     * A: Can't use Callable<Void> because it causes a compilation error if you
     * don't return a Void Object.
     */
	public static void ex6() {
		new Thread(unckeck(() -> {
            LOGGER.trace("Zzzz!!");
			Thread.sleep(1000);
            LOGGER.trace("!!!!");
		})).start();
	}

	public static void ex7() {
		andThen(
                () -> LOGGER.trace("first"), () -> LOGGER.trace("second")
		).run();
	}

	/**
     * What happens when a lambda expression captures values in an enhanced for
     * loop such as this one? String[] names = { "Peter", "Paul", "Mary" };
     * List<Runnable> runners = new ArrayList<>(); for (String name : names)
     * runners.add(() -> LOGGER.trace(name));
     * 
     * Is it legal? Does each lambda expression capture a different value, or do
     * they all get the last value? What happens if you use a traditional loop
     * for (int i = 0; i < names.length; i++)?
     * 
     * A: They all get a different value.
     */
	public static void ex8() {
		String[] names = { "Peter", "Paul", "Mary" };
		List<Runnable> runners = new ArrayList<>();
		for (String name : names) {
            runners.add(() -> LOGGER.trace(name));
		}

		runners.forEach(r -> new Thread(r).start());

		for (int i = 0; i < names.length; i++) {
			// This would give you a compilation error because 'i' is changeable
            // runners.add(() -> LOGGER.trace(names[i]));
			String name = names[i];
			// But this works perfectly fine
            runners.add(() -> LOGGER.trace(name));

		}

		runners.forEach(r -> new Thread(r).start());

	}

	public static void ex9() {
		Collection2<String> a = new Collection2Impl<>();
		a.add("a");
		a.add("b");
		a.add("");
		a.add("d");

        a.forEachIf(LOGGER::trace, s -> !s.isEmpty());

	}
	
	public static void main(String[] args) {
		ex1(new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
		ex2(new File("."));
        ex3(new File(DOCUMENTS_FOLDER), "log");
        ex4(new File(DOCUMENTS_FOLDER).listFiles());
		ex6();
		ex7();
		ex8();
		ex9();
	}

	public static Runnable unckeck(RunnableEx runner) {
		return () -> {
			try {
				runner.run();
			} catch (Exception ex) {
				LOGGER.error("",ex);
			}
		};
	}
}
