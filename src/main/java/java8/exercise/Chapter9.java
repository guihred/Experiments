package java8.exercise;

public final class Chapter9 {

	private Chapter9() {
	}

	/**
	 * 1. Implement a code segment that constructs a Scanner and a PrintWriter
	 * at the end of Section 9.1.1, �The try-with-resources Statement,� on page
	 * 180, without the try-with-resources statement. Be sure to close both
	 * objects, provided they have been properly constructed. You need to
	 * consider the following conditions: -The Scanner constructor throws an
	 * exception. -The PrintWriter constructor throws an exception. -hasNext,
	 * next, or println throws an exception. - in.close() throws an exception. -
	 * out.close() throws an exception.
	 */
	public void ex1() {
	}

	/**
	 * 2 Improve on the preceding exercise by adding any exceptions thrown by
	 * in.close() or out.close() as suppressed exceptions to the original
	 * exception, if there was one.
	 */
	public void ex2() {
	}

	/**
	 * 3 When you rethrow an exception that you caught in a multi-catch clause,
	 * how do you declare its type in the throws declaration of the ambient
	 * method? For example, consider
	 * 
	 * public void process() throws ... { try { ... }catch
	 * (FileNotFoundException | UnknownHostException ex) {
	 * logger.log(Level.SEVERE, "...", ex); throw ex; } }
	 * 
	 */
	public void ex3() {
	}

	/**
	 * 4 In which other parts of the Java library did you encounter situations
	 * that would benefit from multi-catch or, even better, common exception
	 * superclasses? (Hint: XML parsing.)
	 */
	public void ex4() {
	}

	/**
	 * 5 Write a program that reads all characters of a file and writes them out
	 * in reverse order. Use Files.readAllBytes and Files.write. 196 Chapter 9
	 * Java 7 Features That You May Have Missed
	 */
	public void ex5() {
	}

	/**
	 * 6 Write a program that reads all lines of a file and writes them out in
	 * reverse order. Use Files.readAllLines and Files.write.
	 */
	public void ex6() {
	}

	/**
	 * 7 Write a program that reads the contents of a web page and saves it to a
	 * file. Use URL.openStream and Files.copy.
	 */
	public void ex7() {
	}

	/**
	 * 8 Implement the compareTo method of the Point class in Section 9.3.3,
	 * �Comparing Numeric Types,� on page 189,without using Integer.compareTo.
	 */
	public void ex8() {
	}
	
	/**
	 * 9. Given a class public class LabeledPoint { private String label;
	 * private int x; private int y; ... } implement the equals and hashCode
	 * methods.
	 */
	public void ex9() {
	}

	/**
	 * 10. Implement a compareTo method for the LabeledPoint class of the
	 * preceding exercise.
	 */
	public void ex10() {
	}

	/**
	 * 11. Using the ProcessBuilder class, write a program that calls grep -r to
	 * look for credit card numbers in all files in any subdirectory of the
	 * user�s home directory. Collect the numbers that you found in a file.
	 */
	public void ex11() {
	}

	/**
	 * 12. Turn the application of the preceding exercise into an applet or a
	 * Java Web Start implementation. Suppose you want to offer it to users as a
	 * security scan. Package it so that it will run on your JRE. What did you
	 * have to do? What would your users have to do to run it from your web
	 * site?
	 */
	public void ex12() {
	}

}
